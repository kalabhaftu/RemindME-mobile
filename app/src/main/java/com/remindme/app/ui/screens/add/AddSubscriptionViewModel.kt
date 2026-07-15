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
import io.github.jan.supabase.auth.auth

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
            "email" to ChannelPref(),
            "push" to ChannelPref(),
            "telegram" to ChannelPref(),
            "in_app" to ChannelPref()
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
        val currentName = _uiState.value.name
        val currentRenewalDate = _uiState.value.renewalDate
        if (currentName.isBlank()) {
            _uiState.update { it.copy(error = "Service name is required") }
            return
        }
        if (currentRenewalDate == null) {
            _uiState.update { it.copy(error = "Please select a renewal date") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id ?: throw Exception("Not logged in")
                val id = java.util.UUID.randomUUID().toString()
                val now = LocalDateTime.now()
                val renewalDateStr = currentRenewalDate.toString()
                
                val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                    category = "subscription",
                    renewalDate = renewalDateStr,
                    cycle = _uiState.value.cycle
                )

                val subscriptionDetails = mapOf(
                    "cost" to _uiState.value.amount,
                    "currency" to _uiState.value.currency,
                    "cycle" to _uiState.value.cycle,
                    "renewal_date" to renewalDateStr,
                    "logo_url" to (_uiState.value.logoUrl ?: "")
                )
                
                val recurrenceRules = mapOf(
                    "frequency" to _uiState.value.cycle,
                    "next_occurrence" to (nextOccurrence ?: "")
                )

                val item = com.remindme.app.domain.models.ReminderItem(
                    id = id,
                    userId = userId,
                    category = com.remindme.app.domain.models.CategoryType.SUBSCRIPTION,
                    name = currentName,
                    notes = _uiState.value.notes,
                    iconKey = null,
                    createdAt = now,
                    updatedAt = now,
                    subscriptionDetails = subscriptionDetails,
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
