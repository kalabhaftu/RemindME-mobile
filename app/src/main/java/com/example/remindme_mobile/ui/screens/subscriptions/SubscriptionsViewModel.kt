package com.example.remindme_mobile.ui.screens.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.remote.SupabaseManager
import com.example.remindme_mobile.data.repository.ReminderRepository
import com.example.remindme_mobile.domain.models.CategoryType
import com.example.remindme_mobile.domain.models.ReminderItem
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

data class SubscriptionsUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

class SubscriptionsViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    private val _allSubscriptions = MutableStateFlow<List<ReminderItem>>(emptyList())

    val sortedSubscriptions: StateFlow<List<ReminderItem>> = combine(_allSubscriptions, _uiState) { subs, _ ->
        subs.sortedWith(Comparator { a, b ->
            val ra = a.subscriptionDetails?.get("renewal_date")
            val rb = b.subscriptionDetails?.get("renewal_date")
            if (ra.isNullOrBlank() || rb.isNullOrBlank()) return@Comparator 0
            try {
                val da = LocalDate.parse(ra)
                val db = LocalDate.parse(rb)
                da.compareTo(db)
            } catch (e: DateTimeParseException) {
                0
            }
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var realtimeChannel: RealtimeChannel? = null

    init {
        fetchSubscriptions()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("mobile_subscriptions")
            val tables = listOf("reminder_items", "subscription_details")
            
            tables.forEach { table ->
                realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                    this.table = table
                }?.collect {
                    fetchSubscriptions(showLoading = false)
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

    fun fetchSubscriptions(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val all = repository.getReminders()
                val subscriptions = all.filter { it.category == CategoryType.SUBSCRIPTION }
                _allSubscriptions.value = subscriptions
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteSubscription(id: String) {
        viewModelScope.launch {
            try {
                _allSubscriptions.update { list -> list.filter { it.id != id } }
                repository.deleteReminder(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                fetchSubscriptions(showLoading = false)
            }
        }
    }
}
