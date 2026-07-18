package com.remindme.app.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.remindme.app.BuildConfig
import com.remindme.app.data.remote.SupabaseManager
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

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isLoadingTelegram: Boolean = false,
    
    val emailEnabled: Boolean = true,
    val pushEnabled: Boolean = false,
    val telegramEnabled: Boolean = false,
    val inAppEnabled: Boolean = true,
    val defaultLeadTime: String = "morning_of",
    val defaultCustomTime: String = "09:00",
    val nudgeDelayHours: Int = 4,
    val timezone: String = "UTC",

    val hasTelegramToken: Boolean = false,
    val maskedTelegramToken: String = "",
    val botUsername: String? = null,
    val hasChatId: Boolean = false,
    val maskedChatId: String = "",

    val deliveryLogs: List<DeliveryLog> = emptyList(),

    val error: String? = null,
    val message: String? = null
)

@Serializable
data class DeliveryLog(
    val id: String? = null,
    val user_id: String? = null,
    val channel: String? = null,
    val status: String? = null,
    val scheduled_for: String? = null
)

class SettingsViewModel : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val supabase = SupabaseManager.client
    private val webApiUrl = BuildConfig.WEB_API_URL

    init {
        loadPreferences()
        loadTelegramStatus()
        loadDeliveryLogs()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
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
                    val customTime = response["default_custom_time"]?.toString()?.removeSurrounding("\"") ?: "09:00"

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
            _uiState.update { it.copy(error = e.message) }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadTelegramStatus() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(isLoadingTelegram = true) }
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/settings/telegram")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "GET"
            
            if (conn.responseCode == 200) {
                val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseBody)
                if (json.optBoolean("hasToken", false)) {
                    _uiState.update {
                        it.copy(
                            hasTelegramToken = true,
                            maskedTelegramToken = json.optString("maskedToken"),
                            botUsername = json.optString("botUsername").takeIf { u -> u.isNotEmpty() },
                            hasChatId = json.optBoolean("hasChatId", false),
                            maskedChatId = json.optString("maskedChatId")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _uiState.update { it.copy(isLoadingTelegram = false) }
        }
    }

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
            _uiState.update { it.copy(error = "Sync Failed: ${e.message}") }
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
            } else {
                _uiState.update { it.copy(error = "Failed to save token") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
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
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
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
                _uiState.update { it.copy(error = json.optString("error", "Detection failed")) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
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
            _uiState.update { it.copy(error = e.message) }
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
                _uiState.update { it.copy(error = json.optString("error", "Failed to send test")) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun deleteAccount(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: return@launch
            val url = URL("$webApiUrl/api/account")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "DELETE"
            
            if (conn.responseCode == 200) {
                supabase.auth.signOut()
                withContext(Dispatchers.Main) { onComplete() }
            } else {
                _uiState.update { it.copy(error = "Failed to delete account") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun signOut(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun signOutAllDevices(onComplete: () -> Unit) = viewModelScope.launch(Dispatchers.IO) {
        try {
            supabase.auth.signOut(io.github.jan.supabase.auth.SignOutScope.GLOBAL)
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }

    fun exportData(context: android.content.Context) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val token = supabase.auth.currentSessionOrNull()?.accessToken ?: throw Exception("Not logged in")
            val url = URL("$webApiUrl/api/account/export")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("RemindME Export", json))
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(message = "Data exported and copied to clipboard!") }
                }
            } else {
                _uiState.update { it.copy(error = "Export failed: HTTP ${conn.responseCode}") }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
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
            _uiState.update { it.copy(error = e.message) }
        }
    }
}
