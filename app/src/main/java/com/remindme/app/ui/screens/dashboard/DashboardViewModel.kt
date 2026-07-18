package com.remindme.app.ui.screens.dashboard
import com.remindme.app.domain.models.ReminderItem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.domain.models.ReminderOccurrence
import com.remindme.app.utils.OccurrenceCalculator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.RealtimeChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardUiState(
    val reminders: List<ReminderItem> = emptyList(),
    val occurrences: List<ReminderOccurrence> = emptyList(),
    val currentMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    init {
        fetchReminders()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("mobile_dashboard")
            val tables = listOf(
                "reminder_items", "person_details", "subscription_details",
                "task_details", "holiday_details", "escalation_state"
            )
            
            tables.forEach { table ->
                realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                    this.table = table
                }?.collect {
                    fetchReminders(showLoading = false)
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

    fun fetchReminders(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val parsed = repository.getReminders()
                val today = LocalDate.now()
                val startPeriod = _uiState.value.currentMonth
                val endPeriod = _uiState.value.currentMonth.plusMonths(1).minusDays(1)
                val occurrences = OccurrenceCalculator.generateOccurrences(parsed, startPeriod, endPeriod, today)

                val notificationScheduler = com.remindme.app.services.NotificationSchedulingService(getApplication())
                notificationScheduler.scheduleReminders(parsed)

                _uiState.update {
                    it.copy(
                        reminders = parsed,
                        occurrences = occurrences,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load reminders")
                }
            }
        }
    }

    fun onMonthChange(newMonth: LocalDate) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        fetchReminders(showLoading = false)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun clearSelectedDate() {
        _uiState.update { it.copy(selectedDate = null) }
    }

    fun markDone(id: String, dueAt: LocalDate) {
        viewModelScope.launch {
            try {
                val dateStr = String.format("%04d-%02d-%02d", dueAt.year, dueAt.monthValue, dueAt.dayOfMonth)
                repository.markTaskDone(id, dateStr)
                fetchReminders(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to mark as done") }
            }
        }
    }

    fun snooze(id: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                val dateStr = String.format("%04d-%02d-%02d", date.year, date.monthValue, date.dayOfMonth)
                repository.snoozeReminder(id, dateStr)
                fetchReminders(showLoading = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to snooze reminder") }
            }
        }
    }
}
