package com.remindme.app.ui.screens.add
import com.remindme.app.domain.models.CategoryType

import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import java.util.UUID

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
    val isSuccess: Boolean = false,
    val existingPersonId: String? = null
)

class AddPersonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    private val _uiState = MutableStateFlow(AddPersonUiState())
    val uiState: StateFlow<AddPersonUiState> = _uiState.asStateFlow()

    init {
        // Load default notification prefs
        val defaultPrefs = mapOf(
            "email" to ChannelPref(),
            "push" to ChannelPref(),
            "telegram" to ChannelPref(),
            "in_app" to ChannelPref()
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

    fun loadPerson(personId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, existingPersonId = personId) }
            try {
                val item = repository.getReminder(personId)
                if (item != null) {
                    val personDetails = item.person
                    _uiState.update { state ->
                        state.copy(
                            name = item.name,
                            notes = item.notes ?: "",
                            birthdate = personDetails?.birthdate?.let { 
                                try { LocalDateTime.parse(it) } catch (e: Exception) { null }
                            },
                            gender = personDetails?.gender ?: "unspecified",
                            relationship = personDetails?.relationship ?: "friend",
                            avatarUrl = item.iconKey,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Person not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load person") }
            }
        }
    }

    fun savePerson() {
        val currentName = _uiState.value.name
        val currentBirthdate = _uiState.value.birthdate
        if (currentName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a name") }
            return
        }
        if (currentBirthdate == null) {
            _uiState.update { it.copy(error = "Please select a birthdate") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("Not logged in")
                val id = _uiState.value.existingPersonId ?: java.util.UUID.randomUUID().toString()
                val now = LocalDateTime.now()
                val birthdateStr = currentBirthdate.toString()
                
                val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                    category = "person",
                    birthdate = birthdateStr
                )

                val personDetails = com.remindme.app.domain.models.PersonDetails(
                    birthdate = birthdateStr,
                    gender = _uiState.value.gender,
                    relationship = _uiState.value.relationship,
                    customRelationship = _uiState.value.customRelationship
                )
                
                val recurrenceRules = com.remindme.app.domain.models.RecurrenceRules(
                    frequency = "yearly",
                    ends = "never",
                    nextOccurrenceAt = nextOccurrence
                )

                val notificationPrefs = _uiState.value.notificationPrefs.map { (channel, pref) ->
                    com.remindme.app.domain.models.NotificationPreference(
                        channel = channel,
                        enabled = pref.enabled,
                        leadTime = pref.leadTime,
                        customTime = pref.customTime,
                        offsetDays = pref.offsetDays
                    )
                }

                val item = com.remindme.app.domain.models.ReminderItem(
                    id = id,
                    userId = userId,
                    category = com.remindme.app.domain.models.CategoryType.PERSON,
                    name = currentName,
                    notes = _uiState.value.notes,
                    iconKey = _uiState.value.avatarUrl,
                    createdAt = now,
                    updatedAt = now,
                    personDetails = personDetails,
                    recurrenceRules = recurrenceRules,
                    notificationPreferences = notificationPrefs
                )

                if (_uiState.value.existingPersonId != null) {
                    repository.updateReminder(item)
                } else {
                    repository.addReminder(item)
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save person") }
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray, extension: String) {
        viewModelScope.launch {
            setAvatarUploading(true)
            setError("")
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("Not logged in")
                val filename = "${UUID.randomUUID()}.$extension"
                val path = "$userId/$filename"
                
                // Upload to the "avatars" bucket
                val bucket = SupabaseManager.client.storage.from("avatars")
                bucket.upload(path, bytes) {
                    upsert = false
                }
                
                val publicUrl = bucket.publicUrl(path)
                updateAvatarUrl(publicUrl)
            } catch (e: Exception) {
                setError("Failed to upload avatar")
            } finally {
                setAvatarUploading(false)
            }
        }
    }
}
