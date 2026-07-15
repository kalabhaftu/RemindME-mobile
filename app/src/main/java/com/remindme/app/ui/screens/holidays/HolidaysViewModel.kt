package com.remindme.app.ui.screens.holidays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.ReminderItem
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

class HolidaysViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
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
                realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                    this.table = table
                }?.collect {
                    fetchSubscribed()
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
                _uiState.update { it.copy(isLoadingCountries = false, error = e.message) }
            }
        }
    }

    fun selectCountry(code: String) {
        _uiState.update { it.copy(selectedCountry = code) }
        loadHolidays(code)
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
                _uiState.update { it.copy(holidays = list, isLoadingHolidays = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingHolidays = false, error = e.message) }
            }
        }
    }

    private fun fetchSubscribed() {
        viewModelScope.launch {
            try {
                val all = repository.getReminders()
                val custom = all.filter { it.category == CategoryType.CUSTOM_HOLIDAY }
                val keys = custom.mapNotNull { it.holidayDetails?.get("holiday_key") as? String }.toSet()
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
                        it.holidayDetails?.get("holiday_key") == key 
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
                        val newItem = ReminderItem(
                            id = "",
                            userId = user.id,
                            category = CategoryType.CUSTOM_HOLIDAY,
                            name = holiday.localName,
                            createdAt = java.time.LocalDateTime.now(),
                            updatedAt = java.time.LocalDateTime.now(),
                            holidayDetails = mapOf(
                                "country_code" to holiday.countryCode,
                                "holiday_key" to key,
                                "holiday_date" to holiday.date,
                                "is_custom" to false
                            ),
                            recurrenceRules = mapOf(
                                "frequency" to "yearly",
                                "interval_count" to 1,
                                "ends" to "never",
                                "next_occurrence_at" to holiday.date // approximation
                            )
                        )
                        // Actually repository.createReminder doesn't exist, we must add it or use an insert call.
                        // Let's add it to ReminderRepository in a moment.
                        SupabaseManager.client.postgrest["reminder_items"].insert(newItem)
                        _uiState.update { state -> 
                            state.copy(subscribedKeys = state.subscribedKeys + key) 
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                _uiState.update { it.copy(togglingKey = null) }
                fetchSubscribed()
            }
        }
    }

    fun removeSubscribed(key: String) {
        viewModelScope.launch {
            val item = _uiState.value.subscribedItems.firstOrNull { 
                it.holidayDetails?.get("holiday_key") == key 
            }
            if (item != null) {
                try {
                    repository.deleteReminder(item.id)
                    _uiState.update { state -> 
                        state.copy(subscribedKeys = state.subscribedKeys - key) 
                    }
                    fetchSubscribed()
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }
        }
    }
}
