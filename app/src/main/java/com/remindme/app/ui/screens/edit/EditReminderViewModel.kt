package com.remindme.app.ui.screens.edit
import com.remindme.app.domain.models.ReminderItem

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.ui.components.ChannelPref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class EditReminderUiState(
    val reminder: ReminderItem? = null,
    val draftName: String = "",
    val draftNotes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class EditReminderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    
    private val _uiState = MutableStateFlow(EditReminderUiState())
    val uiState: StateFlow<EditReminderUiState> = _uiState.asStateFlow()

    fun loadReminder(reminderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val reminders = repository.getReminders()
                val reminder = reminders.find { it.id == reminderId }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reminder = reminder,
                        draftName = reminder?.name.orEmpty(),
                        draftNotes = reminder?.notes.orEmpty(),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load reminder") }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(draftName = value) }

    fun updateNotes(value: String) = _uiState.update { it.copy(draftNotes = value) }

    fun saveChanges() {
        val state = _uiState.value
        val reminder = state.reminder ?: return
        if (state.draftName.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        updateReminder(reminder.copy(name = state.draftName.trim(), notes = state.draftNotes.trim().ifEmpty { null }))
    }

    fun updateReminder(updatedReminder: ReminderItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.updateReminder(updatedReminder)
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to update reminder") }
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.deleteReminder(reminderId)
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to delete reminder") }
            }
        }
    }
}
