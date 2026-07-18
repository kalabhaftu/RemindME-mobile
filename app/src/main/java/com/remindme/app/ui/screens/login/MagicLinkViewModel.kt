package com.remindme.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.BuildConfig
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class MagicLinkStep {
    INPUT, CHECKING, SENDING, SENT, NOT_FOUND, ERROR
}

data class MagicLinkUiState(
    val email: String = "",
    val step: MagicLinkStep = MagicLinkStep.INPUT,
    val providers: List<String> = emptyList(),
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

sealed class EmailCheckResult {
    data class Found(val providers: List<String>) : EmailCheckResult()
    data object NotFound : EmailCheckResult()
    data object NetworkError : EmailCheckResult()
    data object ServerError : EmailCheckResult()
}

class MagicLinkViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MagicLinkUiState())
    val uiState: StateFlow<MagicLinkUiState> = _uiState.asStateFlow()

    private var statusJob: Job? = null

    init {
        statusJob = viewModelScope.launch {
            SupabaseManager.client.auth.sessionStatus.collect { status ->
                if (status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                    _uiState.update { it.copy(isAuthenticated = true) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        statusJob?.cancel()
    }

    fun updateEmail(e: String) = _uiState.update { it.copy(email = e, error = null) }

    fun sendMagicLink() {
        val email = _uiState.value.email.trim().lowercase()
        if (!email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(step = MagicLinkStep.CHECKING, error = null) }

            when (val result = checkEmailExists(email)) {
                is EmailCheckResult.NetworkError -> {
                    _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Could not verify email. Check your connection and try again.") }
                }
                is EmailCheckResult.ServerError -> {
                    _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Something went wrong. Please try again later.") }
                }
                is EmailCheckResult.NotFound -> {
                    _uiState.update { it.copy(step = MagicLinkStep.NOT_FOUND) }
                }
                is EmailCheckResult.Found -> {
                    _uiState.update { it.copy(step = MagicLinkStep.SENDING) }
                    try {
                        SupabaseManager.client.auth.signInWith(OTP) {
                            this.email = email
                            createUser = false
                        }
                        _uiState.update { it.copy(step = MagicLinkStep.SENT) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Failed to send magic link. Please try again.") }
                    }
                }
            }
        }
    }

    fun retry() = _uiState.update { it.copy(step = MagicLinkStep.INPUT, error = null, providers = emptyList()) }

    private suspend fun checkEmailExists(email: String): EmailCheckResult {
        return try {
            val url = java.net.URL("${BuildConfig.WEB_API_URL}/api/auth/check-email")
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val body = JSONObject(mapOf("email" to email)).toString()
            conn.outputStream.write(body.toByteArray())

            when (conn.responseCode) {
                200 -> {
                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    val exists = json.optBoolean("exists", false)
                    val providers = if (json.has("providers")) {
                        val arr = json.getJSONArray("providers")
                        (0 until arr.length()).map { arr.getString(it) }
                    } else emptyList()
                    if (exists) EmailCheckResult.Found(providers)
                    else EmailCheckResult.NotFound
                }
                429 -> EmailCheckResult.ServerError
                else -> EmailCheckResult.ServerError
            }
        } catch (e: java.net.ConnectException) {
            EmailCheckResult.NetworkError
        } catch (e: java.net.SocketTimeoutException) {
            EmailCheckResult.NetworkError
        } catch (e: java.net.UnknownHostException) {
            EmailCheckResult.NetworkError
        } catch (e: Exception) {
            EmailCheckResult.ServerError
        }
    }
}
