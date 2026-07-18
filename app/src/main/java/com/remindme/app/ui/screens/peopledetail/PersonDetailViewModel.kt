package com.remindme.app.ui.screens.peopledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PersonDetailUiState(
    val person: ReminderItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class PersonDetailViewModel(
    private val personId: String,
    application: Application,
    private val repository: OfflineReminderRepository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    init {
        loadPerson()
    }

    fun loadPerson() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val all = repository.getReminders()
            val person = all.firstOrNull { it.id == personId }
            _uiState.update { it.copy(person = person, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to load person details", isLoading = false) }
        }
    }

    fun deletePerson() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            repository.deleteReminder(personId)
            _uiState.update { it.copy(isDeleted = true, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to delete person", isLoading = false) }
        }
    }
}
