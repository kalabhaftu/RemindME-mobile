package com.remindme.app.ui.screens.tasks

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

data class TasksUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

class TasksViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _allTasks = MutableStateFlow<List<ReminderItem>>(emptyList())

    val sortedTasks: StateFlow<List<ReminderItem>> = combine(_allTasks, _uiState) { tasks, _ ->
        tasks.sortedWith(Comparator { a, b ->
            val da = a.task?.dueAt
            val db = b.task?.dueAt
            if (da == null || db == null) return@Comparator 0
            try {
                da.compareTo(db)
            } catch (e: Exception) {
                0
            }
        })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var realtimeChannel: RealtimeChannel? = null

    init {
        fetchTasks()
        setupRealtime()
    }

    private fun setupRealtime() {
        viewModelScope.launch {
            realtimeChannel = SupabaseManager.client.channel("mobile_tasks")
            val tables = listOf("reminder_items", "task_details")
            
            tables.forEach { table ->
                realtimeChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                    this.table = table
                }?.collect {
                    fetchTasks(showLoading = false)
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

    fun fetchTasks(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }
            try {
                val all = repository.getReminders()
                val tasks = all.filter { it.category == CategoryType.TASK }
                _allTasks.value = tasks
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun markTaskDone(task: ReminderItem) {
        val dueStr = task.task?.dueAt ?: return
        try {
            val due = java.time.LocalDateTime.parse(dueStr.replace("Z", ""))
            val dateStr = String.format("%04d-%02d-%02d", due.year, due.monthValue, due.dayOfMonth)
            viewModelScope.launch {
                try {
                    repository.markTaskDone(task.id, dateStr)
                    fetchTasks(showLoading = false)
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                _allTasks.update { list -> list.filter { it.id != id } }
                repository.deleteReminder(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
                fetchTasks(showLoading = false)
            }
        }
    }
}
