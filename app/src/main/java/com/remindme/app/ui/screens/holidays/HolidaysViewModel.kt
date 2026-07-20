package com.remindme.app.ui.screens.holidays
import com.remindme.app.domain.models.CategoryType

import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.domain.models.HolidayDetails
import com.remindme.app.domain.models.RecurrenceRules
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.time.LocalDate

data class Country(val countryCode: String, val name: String)

data class PublicHoliday(
    val date: String,
    val localName: String,
    val name: String,
    val countryCode: String
) {
    val holidayKey: String get() = "$countryCode-$date-$name"
}

data class HolidaysUiState(
    val holidays: List<PublicHoliday> = emptyList(),
    val countries: List<Country> = emptyList(),
    val selectedCountry: String = "US",
    val subscribedKeys: Set<String> = emptySet(),
    val isLoadingCountries: Boolean = true,
    val isLoadingHolidays: Boolean = false,
    val togglingKey: String? = null,
    val error: String? = null,
    val subscribedItems: List<ReminderItem> = emptyList()
)

class HolidaysViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    private val _uiState = MutableStateFlow(HolidaysUiState())
    val uiState: StateFlow<HolidaysUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    init {
        loadCountries()
        fetchSubscribed()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("mobile_holidays")
            val tables = listOf("reminder_items", "holiday_details")
            
            tables.forEach { table ->
                launch {
                    realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                        this.table = table
                    }?.collect {
                        fetchSubscribed()
                    }
                }
            }
            realtimeChannel?.subscribe()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            realtimeChannel?.unsubscribe()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCountries = true) }
            try {
                val jsonStr = withContext(Dispatchers.IO) {
                    URL("https://date.nager.at/api/v3/AvailableCountries").readText()
                }
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<Country>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(Country(obj.getString("countryCode"), obj.getString("name")))
                }
                list.sortBy { it.name }
                _uiState.update { it.copy(countries = list, isLoadingCountries = false) }
                loadHolidays(_uiState.value.selectedCountry)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingCountries = false, error = "Failed to load countries") }
            }
        }
    }

    fun selectCountry(code: String) {
        _uiState.update { it.copy(selectedCountry = code) }
        loadHolidays(code)
    }

    fun refresh() {
        loadCountries()
        fetchSubscribed()
    }

    private fun loadHolidays(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHolidays = true) }
            try {
                val year = LocalDate.now().year
                val jsonStr = withContext(Dispatchers.IO) {
                    URL("https://date.nager.at/api/v3/PublicHolidays/$year/$code").readText()
                }
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<PublicHoliday>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(PublicHoliday(
                        date = obj.getString("date"),
                        localName = obj.optString("localName", obj.getString("name")),
                        name = obj.getString("name"),
                        countryCode = obj.getString("countryCode")
                    ))
                }
                _uiState.update { it.copy(holidays = list.distinctBy { it.holidayKey }, isLoadingHolidays = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingHolidays = false, error = "Failed to load holidays") }
            }
        }
    }

    private fun fetchSubscribed() {
        viewModelScope.launch {
            try {
                val all = repository.getReminders()
                val custom = all.filter { it.category == CategoryType.CUSTOM_HOLIDAY }
                val keys = custom.mapNotNull { it.holiday?.holidayKey }.toSet()
                _uiState.update { it.copy(subscribedKeys = keys, subscribedItems = custom) }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun toggleHoliday(holiday: PublicHoliday) {
        val key = holiday.holidayKey
        _uiState.update { it.copy(togglingKey = key) }
        viewModelScope.launch {
            try {
                val isSubscribed = _uiState.value.subscribedKeys.contains(key)
                if (isSubscribed) {
                    val item = _uiState.value.subscribedItems.firstOrNull { 
                        it.holiday?.holidayKey == key 
                    }
                    if (item != null) {
                        repository.deleteReminder(item.id)
                        _uiState.update { state -> 
                            state.copy(subscribedKeys = state.subscribedKeys - key) 
                        }
                    }
                } else {
                    // We need user id. 
                    val user = SupabaseManager.client.auth.currentUserOrNull()
                    if (user != null) {
                        // holiday.date is a bare "YYYY-MM-DD" from the public
                        // holidays API. Route it through OccurrenceScheduler
                        // (same as person/subscription/task) for correct
                        // year-rollover, time-of-day, and UTC conversion.
                        val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                            category = "custom_holiday",
                            holidayDate = "${holiday.date}T00:00:00"
                        )
                        val newItem = ReminderItem(
                            // id = "" was being sent as-is to a `uuid primary
                            // key` column -- Postgres rejects an empty string
                            // as invalid UUID syntax, so every holiday
                            // subscription was failing outright.
                            id = java.util.UUID.randomUUID().toString(),
                            userId = user.id,
                            category = CategoryType.CUSTOM_HOLIDAY,
                            name = holiday.localName,
                            createdAt = java.time.LocalDateTime.now(),
                            updatedAt = java.time.LocalDateTime.now(),
                            holidayDetails = HolidayDetails(
                                countryCode = holiday.countryCode,
                                holidayKey = key,
                                holidayDate = holiday.date,
                                isCustom = false
                            ),
                            recurrenceRules = RecurrenceRules(
                                frequency = "yearly",
                                intervalCount = 1,
                                ends = "never",
                                nextOccurrenceAt = nextOccurrence
                            )
                        )
                        // Save through repository

                        repository.addReminder(newItem)
                        _uiState.update { state -> 
                            state.copy(subscribedKeys = state.subscribedKeys + key) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update subscription") }
            } finally {
                _uiState.update { it.copy(togglingKey = null) }
                fetchSubscribed()
            }
        }
    }

    fun removeSubscribed(key: String) {
        viewModelScope.launch {
            val item = _uiState.value.subscribedItems.firstOrNull { 
                it.holiday?.holidayKey == key 
            }
            if (item != null) {
                try {
                    repository.deleteReminder(item.id)
                    _uiState.update { state -> 
                        state.copy(subscribedKeys = state.subscribedKeys - key) 
                    }
                    fetchSubscribed()
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Failed to remove subscription") }
                }
            }
        }
    }
}
