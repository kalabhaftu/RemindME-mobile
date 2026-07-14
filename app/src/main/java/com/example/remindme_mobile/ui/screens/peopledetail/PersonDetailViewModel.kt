package com.example.remindme_mobile.ui.screens.peopledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.models.ReminderItem
import com.example.remindme_mobile.data.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonDetailUiState(
    val person: ReminderItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isDeleted: Boolean = false
)

class PersonDetailViewModel(
    private val personId: String,
    private val repository: ReminderRepository = ReminderRepository()
) : ViewModel() {

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
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun deletePerson() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            repository.deleteReminder(personId)
            _uiState.update { it.copy(isDeleted = true, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
