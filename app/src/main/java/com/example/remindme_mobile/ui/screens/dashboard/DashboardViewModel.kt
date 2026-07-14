package com.example.remindme_mobile.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.remote.SupabaseManager
import com.example.remindme_mobile.data.repository.ReminderRepository
import com.example.remindme_mobile.domain.models.ReminderItem
import com.example.remindme_mobile.domain.models.ReminderOccurrence
import com.example.remindme_mobile.domain.utils.OccurrenceCalculator
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

class DashboardViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    init {
        fetchReminders()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("public:dashboard_mobile")
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

                _uiState.update {
                    it.copy(
                        reminders = parsed,
                        occurrences = occurrences,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun onMonthChange(newMonth: LocalDate) {
        _uiState.update { it.copy(currentMonth = newMonth) }
        fetchReminders(showLoading = false)
    }

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun markDone(id: String, dueAt: LocalDate) {
        // To be implemented in repository
    }

    fun snooze(id: String, date: LocalDate) {
        // To be implemented in repository
    }
}
