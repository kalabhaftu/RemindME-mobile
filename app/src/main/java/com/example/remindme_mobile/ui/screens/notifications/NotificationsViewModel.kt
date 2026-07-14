package com.example.remindme_mobile.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.remote.SupabaseManager
import com.example.remindme_mobile.data.repository.ReminderRepository
import com.example.remindme_mobile.utils.OccurrenceCalculator
import com.example.remindme_mobile.utils.OccurrenceStatus
import com.example.remindme_mobile.utils.ReminderOccurrence
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate

@Serializable
data class InAppNotification(
    val id: String,
    val user_id: String,
    val title: String? = null,
    val body: String? = null,
    val created_at: String,
    val read_at: String? = null
)

data class NotificationsUiState(
    val inAppNotifications: List<InAppNotification> = emptyList(),
    val allOccurrences: List<ReminderOccurrence> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseManager.client
    private val repository = ReminderRepository()

    init {
        loadData()
        setupRealtime()
    }

    private fun setupRealtime() {
        val channel = supabase.channel("mobile_in_app_notifications")
        val inAppFlow = channel.postgresChangeFlow<PostgresAction>("public") { table = "in_app_notifications" }
        inAppFlow.onEach { loadData() }.launchIn(viewModelScope)
        
        val itemsFlow = channel.postgresChangeFlow<PostgresAction>("public") { table = "reminder_items" }
        itemsFlow.onEach { loadData() }.launchIn(viewModelScope)
        
        viewModelScope.launch {
            channel.subscribe()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            
            val inAppList = supabase.postgrest["in_app_notifications"]
                .select {
                    filter { eq("user_id", user.id) }
                    order("created_at", Order.DESCENDING)
                    limit(50)
                }
                .decodeList<InAppNotification>()

            val reminders = repository.getReminders()
            val today = LocalDate.now()
            val end = today.plusDays(30)
            val occurrences = OccurrenceCalculator.generateOccurrences(reminders, today, end, today)

            _uiState.update { 
                it.copy(
                    inAppNotifications = inAppList,
                    allOccurrences = occurrences,
                    isLoading = false
                ) 
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun markRead(id: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.postgrest["in_app_notifications"].update(
                {
                    set("read_at", Instant.now().toString())
                }
            ) {
                filter { eq("id", id) }
            }
            loadData()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun markAllRead() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            supabase.postgrest["in_app_notifications"].update(
                {
                    set("read_at", Instant.now().toString())
                }
            ) {
                filter { 
                    eq("user_id", user.id) 
                    filterNot { filter { eq("read_at", "null") } } // wait, this might be tricky with Postgrest query syntax for IS NULL. 
                    // Let's just update all where user_id matches and read_at is null
                    isFilter("read_at", null)
                }
            }
            loadData()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun getUpcoming(): List<ReminderOccurrence> {
        val today = LocalDate.now()
        val day7 = today.plusDays(7)
        return _uiState.value.allOccurrences.filter {
            (it.status == OccurrenceStatus.TODAY || it.status == OccurrenceStatus.UPCOMING) &&
            !it.date.isAfter(day7)
        }
    }

    fun getMissed(): List<ReminderOccurrence> {
        return _uiState.value.allOccurrences.filter { it.status == OccurrenceStatus.MISSED_PAST }
    }
}
