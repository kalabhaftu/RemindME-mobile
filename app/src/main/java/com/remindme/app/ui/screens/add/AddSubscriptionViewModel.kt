package com.remindme.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.ui.components.liquid.ChannelPref
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class AddSubscriptionUiState(
    val name: String = "",
    val amount: String = "",
    val currency: String = "USD",
    val cycle: String = "monthly",
    val renewalDate: LocalDateTime? = null,
    val notes: String = "",
    val logoUrl: String? = null,
    val logoDomain: String? = null,
    val isResolvingLogo: Boolean = false,
    val notificationPrefs: Map<String, ChannelPref> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AddSubscriptionViewModel : ViewModel() {
    private val repository = ReminderRepository(SupabaseManager.client)
    private val _uiState = MutableStateFlow(AddSubscriptionUiState())
    val uiState: StateFlow<AddSubscriptionUiState> = _uiState.asStateFlow()
    
    private var resolveLogoJob: Job? = null

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

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
        resolveLogoJob?.cancel()
        resolveLogoJob = viewModelScope.launch {
            delay(800)
            resolveLogo(name)
        }
    }

    fun updateAmount(amount: String) = _uiState.update { it.copy(amount = amount) }
    fun updateCurrency(currency: String) = _uiState.update { it.copy(currency = currency) }
    fun updateCycle(cycle: String) = _uiState.update { it.copy(cycle = cycle) }
    fun updateRenewalDate(date: LocalDateTime?) = _uiState.update { it.copy(renewalDate = date) }
    fun updateNotes(notes: String) = _uiState.update { it.copy(notes = notes) }
    fun updateNotificationPrefs(prefs: Map<String, ChannelPref>) = _uiState.update { it.copy(notificationPrefs = prefs) }
    
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun setError(error: String) = _uiState.update { it.copy(error = error) }

    private suspend fun resolveLogo(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isResolvingLogo = true) }
        try {
            // TODO: Call backend logo resolver
            // For now, let's pretend it resolved something generic if we wanted to
            delay(500)
            _uiState.update { it.copy(isResolvingLogo = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isResolvingLogo = false) }
        }
    }

    fun saveSubscription() {
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(error = "Service name is required") }
            return
        }
        if (_uiState.value.renewalDate == null) {
            _uiState.update { it.copy(error = "Please select a renewal date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // TODO: repository.createReminder()
                delay(1000)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
