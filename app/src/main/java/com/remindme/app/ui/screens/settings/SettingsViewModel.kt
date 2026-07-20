package com.remindme.app.ui.screens.settings

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.remindme.app.BuildConfig
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.data.repository.ReminderRepository
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.ui.components.NotificationPrefsStore
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.Serializable
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isLoadingTelegram: Boolean = false,
    
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = false,
    val telegramEnabled: Boolean = false,
    val inAppEnabled: Boolean = true,
    val defaultLeadTime: String = "morning_of",
    val defaultCustomTime: String = "",
    val nudgeDelayHours: Int = 4,
    val timezone: String = "UTC",

    val hasTelegramToken: Boolean = false,
    val maskedTelegramToken: String = "",
    val botUsername: String? = null,
    val hasChatId: Boolean = false,
    val maskedChatId: String = "",

    val deliveryLogs: List<DeliveryLog> = emptyList(),

    val calendarWebcalUrl: String? = null,
    val calendarHttpsUrl: String? = null,
    val isLoadingCalendar: Boolean = false,
    val isImportingContacts: Boolean = false,

    val error: String? = null,
    val message: String? = null
)

@Serializable
data class DeliveryLog(
    val id: String? = null,
    val user_id: String? = null,
    val channel: String? = null,
    val status: String? = null,
    val scheduled_for: String? = null,
    val sent_at: String? = null,
    val error_message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsPrefs = application.getSharedPreferences("remindme_settings_cache", Context.MODE_PRIVATE)
    private val telegramCacheTtlMs = 5 * 60 * 1000L
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseManager.client
    private val webApiUrl = BuildConfig.WEB_API_URL
    private val repository = OfflineReminderRepository(
        ReminderRepository(SupabaseManager.client, application.applicationContext),
        application.applicationContext
    )

    init {
        loadPreferences()
        loadTelegramStatus()
        loadDeliveryLogs()
        loadCalendarFeed()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    private fun loadPreferences() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                val response = supabase.postgrest["user_settings"]
                    .select { filter { eq("user_id", user.id) } }
                    .decodeSingleOrNull<Map<String, kotlinx.serialization.json.JsonElement>>()

                if (response != null) {
                    val tz = response["timezone"]?.let { it.toString().removeSurrounding("\"") } ?: "UTC"
                    val nudge = response["nudge_delay_hours"]?.toString()?.toIntOrNull() ?: 4
                    val leadTime = response["default_lead_time"]?.toString()?.removeSurrounding("\"") ?: "morning_of"
                    val customTime = response["default_custom_time"]?.toString()?.removeSurrounding("\"") ?: ""

                    var em = true
                    var pu = false
                    var tg = false
                    var ia = true

                    val channels = response["default_channels"] as? kotlinx.serialization.json.JsonObject
                    if (channels != null) {
                        em = channels["email"]?.toString()?.toBooleanStrictOrNull() ?: em
                        pu = channels["push"]?.toString()?.toBooleanStrictOrNull() ?: pu
                        tg = channels["telegram"]?.toString()?.toBooleanStrictOrNull() ?: tg
                        ia = channels["in_app"]?.toString()?.toBooleanStrictOrNull() ?: ia
                    }

                    _uiState.update {
                        it.copy(
                            timezone = tz,
                            nudgeDelayHours = nudge,
                            defaultLeadTime = leadTime,
                            defaultCustomTime = customTime,
                            emailEnabled = em,
                            pushEnabled = pu,
                            telegramEnabled = tg,
                            inAppEnabled = ia
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to load preferences") }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadTelegramStatus(force: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        val cachedAt = settingsPrefs.getLong("telegram_cached_at", 0L)
        val cached = settingsPrefs.getString("telegram_status", null)
        if (!force && cached != null) {
            applyTelegramStatus(JSONObject(cached))
            if (System.currentTimeMillis() - cachedAt < telegramCacheTtlMs) return@launch
        }
        _uiState.update { it.copy(isLoadingTelegram = cached == null) }
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "GET"
            
            if (conn.responseCode == 200) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseBody)
                settingsPrefs.edit().putString("telegram_status", json.toString()).putLong("telegram_cached_at", System.currentTimeMillis()).apply()
                applyTelegramStatus(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _uiState.update { it.copy(isLoadingTelegram = false) }
        }
    }

    private fun applyTelegramStatus(json: JSONObject) {
        _uiState.update {
            it.copy(
                hasTelegramToken = json.optBoolean("hasToken", false),
                maskedTelegramToken = json.optString("maskedToken"),
                botUsername = json.optString("botUsername").takeIf(String::isNotEmpty),
                hasChatId = json.optBoolean("hasChatId", false),
                maskedChatId = json.optString("maskedChatId")
            )
        }
    }

    fun refreshTelegramStatus() { loadTelegramStatus(force = true) }

    private fun loadDeliveryLogs() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            val logs = supabase.postgrest["delivery_log"]
                .select {
                    filter { eq("user_id", user.id) }
                    order("scheduled_for", Order.DESCENDING)
                    limit(10)
                }
                .decodeList<DeliveryLog>()
            _uiState.update { it.copy(deliveryLogs = logs) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCalendarFeed() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoadingCalendar = true) }
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val conn = (URL("$webApiUrl/api/calendar/feed-url").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Authorization", "Bearer $token")
            }
            val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (conn.responseCode !in 200..299) {
                throw Exception(JSONObject(body).optString("error", "Calendar link unavailable"))
            }
            val json = JSONObject(body)
            _uiState.update {
                it.copy(
                    calendarWebcalUrl = json.optString("webcalUrl").takeIf(String::isNotBlank),
                    calendarHttpsUrl = json.optString("httpsUrl").takeIf(String::isNotBlank)
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Could not load calendar link") }
        } finally {
            _uiState.update { it.copy(isLoadingCalendar = false) }
        }
    }

    fun rotateCalendarFeed() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoadingCalendar = true) }
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val conn = (URL("$webApiUrl/api/calendar/feed-url").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $token")
            }
            val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (conn.responseCode !in 200..299) {
                throw Exception(JSONObject(body).optString("error", "Could not rotate calendar link"))
            }
            val json = JSONObject(body)
            _uiState.update {
                it.copy(
                    calendarWebcalUrl = json.optString("webcalUrl").takeIf(String::isNotBlank),
                    calendarHttpsUrl = json.optString("httpsUrl").takeIf(String::isNotBlank),
                    message = "Calendar link regenerated"
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Could not regenerate calendar link") }
        } finally {
            _uiState.update { it.copy(isLoadingCalendar = false) }
        }
    }

    fun importAllContacts(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isImportingContacts = true, error = null) }
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("Not logged in")
            val resolver = context.contentResolver
            val existing = runCatching { repository.getReminders() }.getOrElse { repository.cachedSnapshot() }
                .filter { it.category == CategoryType.PERSON }
                .mapTo(mutableSetOf()) { it.name.trim().lowercase() }
            val defaults = NotificationPrefsStore.load(context)
            var imported = 0
            var skipped = 0
            var withBirthday = 0
            var withoutBirthday = 0
            val seenContactIds = mutableSetOf<String>()

            resolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Event.START_DATE
                ),
                "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?",
                arrayOf(
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
                ),
                "${ContactsContract.Data.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                val birthdayIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                while (cursor.moveToNext()) {
                    val contactId = cursor.getString(idIndex).orEmpty()
                    if (!seenContactIds.add(contactId)) continue
                    val name = cursor.getString(nameIndex)?.trim().orEmpty()
                    if (name.isBlank()) continue
                    val birthdate = parseContactBirthday(cursor.getString(birthdayIndex))
                    if (birthdate == null) {
                        withoutBirthday++
                        continue
                    }
                    val key = name.lowercase()
                    if (!existing.add(key)) {
                        skipped++
                        continue
                    }
                    val now = LocalDateTime.now()
                    val item = com.remindme.app.domain.models.ReminderItem(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        category = CategoryType.PERSON,
                        name = name,
                        createdAt = now,
                        updatedAt = now,
                        personDetails = com.remindme.app.domain.models.PersonDetails(
                            birthdate = birthdate,
                            relationship = "friend",
                            gender = "unspecified"
                        ),
                        recurrenceRules = com.remindme.app.domain.models.RecurrenceRules(
                            frequency = "yearly",
                            ends = "never",
                            nextOccurrenceAt = com.remindme.app.utils.OccurrenceScheduler.computeInitialNextOccurrence(
                                category = "person",
                                birthdate = birthdate
                            )
                        ),
                        notificationPreferences = defaults.map { (channel, pref) ->
                            com.remindme.app.domain.models.NotificationPreference(
                                channel = channel,
                                enabled = pref.enabled,
                                leadTime = pref.leadTime,
                                customTime = pref.customTime,
                                offsetDays = pref.offsetDays
                            )
                        }
                    )
                    repository.addReminder(item)
                    imported++
                    withBirthday++
                }
            } ?: throw Exception("Unable to read contacts")

            _uiState.update {
                it.copy(
                    message = "Imported $imported contacts. Birthdays found: $withBirthday; missing: $withoutBirthday; duplicates skipped: $skipped",
                    isImportingContacts = false
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Contact import failed", isImportingContacts = false) }
        }
    }

    private fun parseContactBirthday(raw: String?): String? {
        val value = raw?.trim().orEmpty()
        if (value.isBlank() || value.startsWith("--")) return null
        return runCatching {
            when {
                value.matches(Regex("\\d{8}")) -> LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE).toString()
                value.length >= 10 -> LocalDate.parse(value.take(10), DateTimeFormatter.ISO_LOCAL_DATE).toString()
                else -> null
            }
        }.getOrNull()
    }

    fun updatePreference(key: String, value: Any) {
        _uiState.update {
            when (key) {
                "default_channel_email" -> it.copy(emailEnabled = value as Boolean)
                "default_channel_push" -> it.copy(pushEnabled = value as Boolean)
                "default_channel_telegram" -> it.copy(telegramEnabled = value as Boolean)
                "default_channel_in_app" -> it.copy(inAppEnabled = value as Boolean)
                "default_lead_time" -> it.copy(defaultLeadTime = value as String)
                "default_custom_time" -> it.copy(defaultCustomTime = value as String)
                "timezone" -> it.copy(timezone = value as String)
                "nudge_delay_hours" -> it.copy(nudgeDelayHours = value as Int)
                else -> it
            }
        }
        syncPreferencesToSupabase()
    }

    private fun syncPreferencesToSupabase() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val user = supabase.auth.currentUserOrNull() ?: return@launch
            val state = _uiState.value
            
            val payload = buildJsonObject {
                put("user_id", JsonPrimitive(user.id))
                put("timezone", JsonPrimitive(state.timezone))
                put("default_channels", buildJsonObject {
                    put("email", JsonPrimitive(state.emailEnabled))
                    put("push", JsonPrimitive(state.pushEnabled))
                    put("telegram", JsonPrimitive(state.telegramEnabled))
                    put("in_app", JsonPrimitive(state.inAppEnabled))
                })
                put("default_lead_time", JsonPrimitive(state.defaultLeadTime))
                put("default_custom_time", JsonPrimitive(state.defaultCustomTime))
                put("nudge_delay_hours", JsonPrimitive(state.nudgeDelayHours))
            }
            
            supabase.postgrest["user_settings"].upsert(payload)
            android.util.Log.d("SettingsViewModel", "Successfully synced preferences to Supabase: $payload")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Failed to sync preferences to Supabase", e)
            _uiState.update { it.copy(error = "Failed to sync preferences. Please try again.") }
        }
    }

    fun saveTelegramToken(tokenStr: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = "POST"
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { it.write(JSONObject(mapOf("token" to tokenStr)).toString()) }

            if (conn.responseCode == 200) {
                val res = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(res)
                _uiState.update {
                    it.copy(
                        botUsername = json.optString("botUsername"),
                        hasTelegramToken = true,
                        maskedTelegramToken = tokenStr.replaceRange(8, tokenStr.length, "********"),
                        message = "Telegram token saved"
                    )
                }
                settingsPrefs.edit().remove("telegram_status").remove("telegram_cached_at").apply()
            } else {
                _uiState.update { it.copy(error = "Failed to save token") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to save Telegram token") }
        }
    }

    fun deleteTelegramToken() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "DELETE"
            
            if (conn.responseCode == 200) {
                _uiState.update {
                    it.copy(
                        hasTelegramToken = false,
                        maskedTelegramToken = "",
                        botUsername = null,
                        hasChatId = false,
                        maskedChatId = "",
                        message = "Telegram token deleted"
                    )
                }
                settingsPrefs.edit().remove("telegram_status").remove("telegram_cached_at").apply()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to delete Telegram token") }
        }
    }

    fun detectChatId() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram/chat-id")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "PUT"
            
            val responseBody = if (conn.responseCode == 200) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream.bufferedReader().use { it.readText() }
            }
            val json = JSONObject(responseBody)
            
            if (conn.responseCode == 200) {
                val chatId = json.optString("chatId")
                _uiState.update {
                    it.copy(
                        hasChatId = true,
                        maskedChatId = if (chatId.length > 4) "***${chatId.substring(chatId.length - 4)}" else "****",
                        message = "Chat ID detected and saved"
                    )
                }
            } else {
                _uiState.update { it.copy(error = "Failed to detect chat ID") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to detect chat ID") }
        }
    }

    fun saveChatId(chatIdStr: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram/chat-id")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = "POST"
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { it.write(JSONObject(mapOf("chatId" to chatIdStr)).toString()) }

            if (conn.responseCode == 200) {
                _uiState.update {
                    it.copy(
                        hasChatId = true,
                        maskedChatId = if (chatIdStr.length > 4) "***${chatIdStr.substring(chatIdStr.length - 4)}" else "****",
                        message = "Chat ID saved"
                    )
                }
            } else {
                _uiState.update { it.copy(error = "Failed to save") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to save chat ID") }
        }
    }

    fun testChannel(channel: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/channels/$channel/test")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "POST"
            
            if (conn.responseCode == 200) {
                _uiState.update { it.copy(message = "Test notification sent via $channel") }
            } else {
                val errorStr = conn.errorStream.bufferedReader().use { it.readText() }
                val json = JSONObject(errorStr)
                _uiState.update { it.copy(error = "Failed to send test notification") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to send test notification") }
        }
    }

    fun deleteAccount(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/account")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "DELETE"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.outputStream.use { it.write("{\"confirmation\":\"DELETE\"}".toByteArray()) }
            
            if (conn.responseCode == 200) {
                supabase.auth.signOut()
                withContext(Dispatchers.Main) { onComplete() }
            } else {
                _uiState.update { it.copy(error = "Failed to delete account") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to delete account") }
        }
    }

    fun signOut(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to sign out") }
        }
    }

    fun signOutAllDevices(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.auth.signOut(io.github.jan.supabase.auth.SignOutScope.GLOBAL)
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to sign out all devices") }
        }
    }

    fun exportData(context: android.content.Context, destination: android.net.Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: throw Exception("Not logged in")
            val url = URL("$webApiUrl/api/account/export")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                if (JSONObject(json).optJSONArray("reminders")?.length() != null && JSONObject(json).optJSONArray("reminders")?.length() == 0) {
                    withContext(Dispatchers.Main) { _uiState.update { it.copy(message = "There are no reminders to export yet") } }
                    return@launch
                }
                val bytes = json.toByteArray(Charsets.UTF_8)
                context.contentResolver.openOutputStream(destination, "wt")?.use { output ->
                    output.write(bytes)
                    output.flush()
                }
                    ?: throw Exception("Unable to open export destination")
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(message = "Export saved (${bytes.size} bytes) as ${destination.lastPathSegment ?: "JSON file"}") }
                }
            } else {
                _uiState.update { it.copy(error = "Export failed: HTTP ${conn.responseCode}") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Failed to export data") }
        }
    }

    fun importData(context: android.content.Context, source: android.net.Uri) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: throw Exception("Not logged in")
            val json = context.contentResolver.openInputStream(source)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Unable to read import file")
            if (json.length > 2_000_000 || !json.trimStart().startsWith("{")) throw Exception("Invalid JSON export")
            val conn = (URL("$webApiUrl/api/account/import").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
            }
            conn.outputStream.use { it.write(json.toByteArray()) }
            val body = (if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream)
                .bufferedReader().use { it.readText() }
            if (conn.responseCode !in 200..299) throw Exception(JSONObject(body).optString("error", "Import failed"))
            val result = JSONObject(body)
            val imported = result.optInt("imported")
            val skipped = result.optInt("skipped")
            _uiState.update {
                it.copy(message = if (imported == 0 && skipped == 0) {
                    "This JSON file contains no reminders"
                } else {
                    "Imported $imported reminders; skipped $skipped duplicates"
                })
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Failed to import data") }
        }
    }

    fun checkForUpdate(context: android.content.Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val info = com.remindme.app.services.UpdateService.checkForUpdate(
                context.packageManager, context.packageName
            )
            if (info != null && info.updateAvailable) {
                _uiState.update { it.copy(
                    message = "Update available: v${info.latestVersion} (current: v${info.currentVersion})"
                ) }
            } else if (info != null) {
                _uiState.update { it.copy(message = "You're up to date (v${info.currentVersion})!") }
            } else {
                _uiState.update { it.copy(error = "Could not check for updates.") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to check for update") }
        }
    }
}
