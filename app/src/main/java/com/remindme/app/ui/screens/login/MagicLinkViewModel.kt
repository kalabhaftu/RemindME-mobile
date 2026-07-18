package com.remindme.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
                SupabaseManager.client.auth.signInWith(OTP) {
                    this.email = email
                    createUser = true
                    this.redirectTo = "remindme://login-callback"
                }
                _uiState.update { it.copy(step = MagicLinkStep.SENT) }
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                if (msg.contains("not found") || msg.contains("invalid")) {
                    _uiState.update { it.copy(step = MagicLinkStep.NOT_FOUND) }
                } else {
                    _uiState.update { it.copy(step = MagicLinkStep.ERROR, error = "Failed to send magic link. Please try again.") }
                }
            }
        }
    }

    fun retry() = _uiState.update { it.copy(step = MagicLinkStep.INPUT, error = null) }
}
