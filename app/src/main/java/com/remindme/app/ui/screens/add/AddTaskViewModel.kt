package com.remindme.app.ui.screens.add
import androidx.lifecycle.viewModelScope
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.ui.components.ChannelPref
import com.remindme.app.ui.components.NotificationPrefsStore
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
    val isSuccess: Boolean = false,
    val existingTaskId: String? = null
)

class AddTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    private val _uiState = MutableStateFlow(AddTaskUiState())
    val uiState: StateFlow<AddTaskUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(notificationPrefs = NotificationPrefsStore.load(application)) }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun updateDueAt(date: LocalDateTime?) = _uiState.update { it.copy(dueAt = date) }
    fun updateIconKey(key: String) = _uiState.update { it.copy(iconKey = key) }
    fun updateNotificationPrefs(prefs: Map<String, ChannelPref>) = _uiState.update { it.copy(notificationPrefs = prefs) }

    fun resetForNewTask() {
        _uiState.update {
            it.copy(
                name = "",
                notes = "",
                dueAt = null,
                iconKey = "trash",
                isLoading = false,
                error = null,
                isSuccess = false,
                existingTaskId = null
            )
        }
    }
    
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun setError(error: String) = _uiState.update { it.copy(error = error) }

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false, existingTaskId = taskId) }
            try {
                val item = repository.getReminder(taskId)
                if (item?.category != com.remindme.app.domain.models.CategoryType.TASK) {
                    _uiState.update { it.copy(isLoading = false, error = "Task not found") }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        name = item.name,
                        notes = item.notes ?: "",
                        dueAt = item.task?.dueAt?.let { value ->
                            runCatching {
                                java.time.OffsetDateTime.parse(value).toLocalDateTime()
                            }.getOrElse {
                                java.time.LocalDateTime.parse(value.removeSuffix("Z"))
                            }
                        },
                        iconKey = item.iconKey ?: "trash",
                        notificationPrefs = item.notificationPreferences
                            ?.associate { pref ->
                                pref.channel to ChannelPref(
                                    enabled = pref.enabled,
                                    leadTime = pref.leadTime,
                                    customTime = pref.customTime,
                                    offsetDays = pref.offsetDays
                                )
                            }
                            ?: it.notificationPrefs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load task: ${e.message ?: "Unknown error"}") }
            }
        }
    }

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
                val id = _uiState.value.existingTaskId ?: java.util.UUID.randomUUID().toString()
                val now = LocalDateTime.now()
                val dueAtStr = com.remindme.app.utils.OccurrenceScheduler.toUtcIso(currentDueAt)
                
                val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                    category = "task",
                    dueAt = dueAtStr
                )

                val taskDetails = com.remindme.app.domain.models.TaskDetails(
                    dueAt = dueAtStr
                )
                
                val recurrenceRules = com.remindme.app.domain.models.RecurrenceRules(
                    frequency = "none",
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
                    category = com.remindme.app.domain.models.CategoryType.TASK,
                    name = currentName,
                    notes = _uiState.value.notes,
                    iconKey = _uiState.value.iconKey,
                    createdAt = now,
                    updatedAt = now,
                    taskDetails = taskDetails,
                    recurrenceRules = recurrenceRules,
                    notificationPreferences = notificationPrefs
                )

                if (_uiState.value.existingTaskId != null) {
                    repository.updateReminder(item)
                } else {
                    repository.addReminder(item)
                }
                NotificationPrefsStore.save(getApplication(), _uiState.value.notificationPrefs)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save task: ${e.message ?: "Unknown error"}") }
            }
        }
    }
}
