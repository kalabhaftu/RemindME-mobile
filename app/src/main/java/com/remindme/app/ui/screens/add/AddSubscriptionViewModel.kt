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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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

class AddSubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OfflineReminderRepository(ReminderRepository(SupabaseManager.client, application.applicationContext), application.applicationContext)
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
            val resolvedDomain = withContext(Dispatchers.IO) {
                try {
                    val url = "https://autocomplete.clearbit.com/v1/companies/suggest?query=${java.net.URLEncoder.encode(query, "UTF-8")}"
                    val response = java.net.URL(url).readText()
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        jsonArray.getJSONObject(0).optString("domain")
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            } ?: run {
                val clean = query.lowercase().replace(Regex("[^a-z0-9]"), "")
                if (clean.isNotEmpty()) "$clean.com" else null
            }

            if (resolvedDomain != null) {
                val logoUrl = resolveLogoUrl(resolvedDomain)
                _uiState.update { it.copy(logoUrl = logoUrl, logoDomain = resolvedDomain, isResolvingLogo = false) }
            } else {
                _uiState.update { it.copy(isResolvingLogo = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isResolvingLogo = false) }
        }
    }

    private suspend fun resolveLogoUrl(domain: String): String {
        val urls = listOf(
            "https://icon.horse/icon/$domain",
            "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        )
        for (url in urls) {
            try {
                val conn = withContext(Dispatchers.IO) {
                    val u = java.net.URL(url)
                    val c = u.openConnection() as java.net.HttpURLConnection
                    c.connectTimeout = 3000
                    c.readTimeout = 3000
                    c.requestMethod = "HEAD"
                    c
                }
                val contentType = conn.contentType
                conn.disconnect()
                if (contentType != null && contentType.startsWith("image")) return url
            } catch (_: Exception) {}
        }
        return urls.last()
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

                val subscriptionDetails = com.remindme.app.domain.models.SubscriptionDetails(
                    billingAmount = _uiState.value.amount.toDoubleOrNull(),
                    billingCurrency = _uiState.value.currency,
                    cycle = _uiState.value.cycle,
                    renewalDate = renewalDateStr,
                    logoUrl = _uiState.value.logoUrl
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

                repository.addReminder(item)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to save subscription") }
            }
        }
    }
}
