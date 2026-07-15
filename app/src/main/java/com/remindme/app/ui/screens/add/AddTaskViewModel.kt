package com.remindme.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.ui.components.liquid.ChannelPref
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import io.github.jan.supabase.auth.auth

data class AddTaskUiState(
    val name: String = "",
    val notes: String = "",
    val dueAt: LocalDateTime? = null,
    val iconKey: String = "trash",
    val notificationPrefs: Map<String, ChannelPref> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AddTaskViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    init {
        // Load default notification prefs
        val defaultPrefs = mapOf(
            "email" to ChannelPref(enabled = true),
            "push" to ChannelPref(enabled = false),
            "telegram" to ChannelPref(enabled = false),
            "in_app" to ChannelPref(enabled = true)
        )
        _uiState.update { it.copy(notificationPrefs = defaultPrefs) }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun updateDueAt(date: LocalDateTime?) = _uiState.update { it.copy(dueAt = date) }
    fun updateIconKey(key: String) = _uiState.update { it.copy(iconKey = key) }
    fun updateNotificationPrefs(prefs: Map<String, ChannelPref>) = _uiState.update { it.copy(notificationPrefs = prefs) }
    
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun setError(error: String) = _uiState.update { it.copy(error = error) }

    fun saveTask() {
        val currentName = _uiState.value.name
        val currentDueAt = _uiState.value.dueAt
        if (currentName.isBlank()) {
            _uiState.update { it.copy(error = "Task name is required") }
            return
        }
        if (currentDueAt == null) {
            _uiState.update { it.copy(error = "Please select a due date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("Not logged in")
                val id = java.util.UUID.randomUUID().toString()
                val now = LocalDateTime.now()
                val dueAtStr = currentDueAt.toString()
                
                val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                    category = "task",
                    dueAt = dueAtStr
                )

                val taskDetails = mapOf(
                    "due_at" to dueAtStr,
                    "is_completed" to "false"
                )
                
                val recurrenceRules = mapOf(
                    "frequency" to "none",
                    "next_occurrence" to (nextOccurrence ?: "")
                )

                val item = com.remindme.app.domain.models.ReminderItem(
                    id = id,
                    userId = userId,
                    category = com.remindme.app.domain.models.CategoryType.TASK,
                    name = currentName,
                    notes = _uiState.value.notes,
                    iconKey = _uiState.value.iconKey,
                    createdAt = now,
                    updatedAt = now,
                    taskDetails = taskDetails,
                    recurrenceRules = recurrenceRules
                )

                repository.addReminder(item)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
