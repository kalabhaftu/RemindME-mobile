package com.remindme.app.ui.screens.add
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.ui.components.ChannelPref
import com.remindme.app.ui.components.NotificationPrefsStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import io.github.jan.supabase.auth.auth
import com.remindme.app.services.LogoResolver

data class AddSubscriptionUiState(
    val name: String = "",
    val amount: String = "",
    val currency: String = "USD",
    val cycle: String = "monthly",
    val renewalDate: LocalDateTime? = null,
    val notes: String = "",
    val logoUrl: String? = null,
    val logoDomain: String? = null,
    val logoLoaded: Boolean = false,
    val isResolvingLogo: Boolean = false,
    val notificationPrefs: Map<String, ChannelPref> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val existingSubscriptionId: String? = null
)

class AddSubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
    private val _uiState = MutableStateFlow(AddSubscriptionUiState())
    val uiState: StateFlow<AddSubscriptionUiState> = _uiState.asStateFlow()
    
    private var resolveLogoJob: Job? = null

    init {
        _uiState.update { it.copy(notificationPrefs = NotificationPrefsStore.load(application)) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, logoUrl = null, logoDomain = null, logoLoaded = false) }
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

    fun resetForNewSubscription() {
        _uiState.update {
            it.copy(
                name = "",
                amount = "",
                currency = "USD",
                cycle = "monthly",
                renewalDate = null,
                notes = "",
                logoUrl = null,
                logoDomain = null,
                logoLoaded = false,
                isResolvingLogo = false,
                isLoading = false,
                error = null,
                isSuccess = false,
                existingSubscriptionId = null
            )
        }
    }
    
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun setError(error: String) = _uiState.update { it.copy(error = error) }

    fun loadSubscription(subscriptionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isSuccess = false, existingSubscriptionId = subscriptionId) }
            try {
                val item = repository.getReminder(subscriptionId)
                if (item?.category != com.remindme.app.domain.models.CategoryType.SUBSCRIPTION) {
                    _uiState.update { it.copy(isLoading = false, error = "Subscription not found") }
                    return@launch
                }
                val subscription = item.subscription
                _uiState.update {
                    it.copy(
                        name = item.name,
                        amount = subscription?.billingAmount?.toString() ?: "",
                        currency = subscription?.billingCurrency ?: "USD",
                        cycle = subscription?.cycle ?: "monthly",
                        renewalDate = subscription?.renewalDate?.let { value ->
                            runCatching { java.time.LocalDate.parse(value.take(10)).atStartOfDay() }.getOrNull()
                        },
                        notes = item.notes ?: "",
                        logoUrl = subscription?.logoUrl,
                        logoDomain = subscription?.logoDomain,
                        logoLoaded = false,
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
                _uiState.update { it.copy(isLoading = false, error = "Failed to load subscription: ${e.message ?: "Unknown error"}") }
            }
        }
    }

    private suspend fun resolveLogo(query: String) {
        if (query.isBlank()) return
        _uiState.update { it.copy(isResolvingLogo = true) }
        try {
            val resolution = LogoResolver.resolve(query)
            if (resolution != null) {
                _uiState.update { it.copy(logoUrl = resolution.logoUrl, logoDomain = resolution.domain, logoLoaded = false, isResolvingLogo = false) }
            } else {
                _uiState.update { it.copy(isResolvingLogo = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isResolvingLogo = false) }
        }
    }

    fun markLogoLoaded() = _uiState.update { it.copy(logoLoaded = true) }

    fun markLogoFailed() = _uiState.update { it.copy(logoUrl = null, logoLoaded = false) }

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
                val id = _uiState.value.existingSubscriptionId ?: java.util.UUID.randomUUID().toString()
                val now = LocalDateTime.now()
                val renewalDateStr = currentRenewalDate.toLocalDate().toString()
                
                val nextOccurrence = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                    category = "subscription",
                    renewalDate = renewalDateStr,
                    cycle = _uiState.value.cycle
                )

                val subscriptionDetails = com.remindme.app.domain.models.SubscriptionDetails(
                    billingAmount = _uiState.value.amount.toDoubleOrNull(),
                    billingCurrency = _uiState.value.currency,
                    cycle = _uiState.value.cycle,
                    renewalDate = renewalDateStr,
                    logoUrl = _uiState.value.logoUrl,
                    logoDomain = _uiState.value.logoDomain
                )
                
                val recurrenceRules = com.remindme.app.domain.models.RecurrenceRules(
                    frequency = _uiState.value.cycle,
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
                    category = com.remindme.app.domain.models.CategoryType.SUBSCRIPTION,
                    name = currentName,
                    notes = _uiState.value.notes,
                    iconKey = null,
                    createdAt = now,
                    updatedAt = now,
                    subscriptionDetails = subscriptionDetails,
                    recurrenceRules = recurrenceRules,
                    notificationPreferences = notificationPrefs
                )

                if (_uiState.value.existingSubscriptionId != null) {
                    repository.updateReminder(item)
                } else {
                    repository.addReminder(item)
                }
                NotificationPrefsStore.save(getApplication(), _uiState.value.notificationPrefs)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save subscription: ${e.message ?: "Unknown error"}") }
            }
        }
    }
}
