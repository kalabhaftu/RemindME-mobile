package com.remindme.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.BuildConfig
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

enum class MagicLinkStep {
    INPUT, SENDING, SENT, NOT_FOUND, ERROR
}

data class MagicLinkUiState(
    val email: String = "",
    val step: MagicLinkStep = MagicLinkStep.INPUT,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

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
            _uiState.update { it.copy(step = MagicLinkStep.SENDING, error = null) }
            try {
                val (success, statusCode) = sendOtpRequest(email)
                when {
                    success -> _uiState.update { it.copy(step = MagicLinkStep.SENT) }
                    statusCode == 429 -> {
                        delay(30000)
                        _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Too many requests. Please wait a moment and try again.") }
                    }
                    statusCode == 422 -> _uiState.update { it.copy(step = MagicLinkStep.NOT_FOUND) }
                    else -> _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Failed to send magic link. Please try again.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Failed to send magic link. Please try again.") }
            }
        }
    }

    private suspend fun sendOtpRequest(email: String): Pair<Boolean, Int> {
        val json = JSONObject().apply {
            put("email", email)
            put("create_user", true)
            put("redirect_to", "remindme://login-callback")
        }

        val url = URL("${BuildConfig.SUPABASE_URL}/auth/v1/otp")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("apikey", BuildConfig.SUPABASE_ANON_KEY)
        conn.connectTimeout = 10000
        conn.readTimeout = 10000

        return try {
            conn.outputStream.write(json.toString().toByteArray())
            val code = conn.responseCode
            Pair(code == 200, code)
        } finally {
            conn.disconnect()
        }
    }

    fun retry() = _uiState.update { it.copy(step = MagicLinkStep.INPUT, error = null) }
}
