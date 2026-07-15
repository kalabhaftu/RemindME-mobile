package com.remindme.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.ui.components.liquid.ChannelPref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class AddPersonUiState(
    val name: String = "",
    val notes: String = "",
    val birthdate: LocalDateTime? = null,
    val gender: String = "unspecified",
    val relationship: String = "friend",
    val customRelationship: String = "",
    val avatarUrl: String? = null,
    val notificationPrefs: Map<String, ChannelPref> = emptyMap(),
    val isUploadingAvatar: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AddPersonViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(AddPersonUiState())
    val uiState: StateFlow<AddPersonUiState> = _uiState.asStateFlow()

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
    fun updateBirthdate(date: LocalDateTime?) = _uiState.update { it.copy(birthdate = date) }
    fun updateGender(gender: String) = _uiState.update { it.copy(gender = gender) }
    fun updateRelationship(rel: String) = _uiState.update { it.copy(relationship = rel) }
    fun updateCustomRelationship(rel: String) = _uiState.update { it.copy(customRelationship = rel) }
    fun updateAvatarUrl(url: String?) = _uiState.update { it.copy(avatarUrl = url) }
    fun updateNotificationPrefs(prefs: Map<String, ChannelPref>) = _uiState.update { it.copy(notificationPrefs = prefs) }
    
    fun setAvatarUploading(uploading: Boolean) = _uiState.update { it.copy(isUploadingAvatar = uploading) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun setError(error: String) = _uiState.update { it.copy(error = error) }

    fun savePerson() {
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }
        if (_uiState.value.birthdate == null) {
            _uiState.update { it.copy(error = "Please select a birthdate") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // TODO: Actually save the Person using ReminderRepository
                // For now, simulate success.
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
