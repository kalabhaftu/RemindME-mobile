package com.example.remindme_mobile.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remindme_mobile.data.remote.SupabaseManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignUpMode: Boolean = false,
    val error: String? = null,
    val showGoogleConflictDialog: Boolean = false,
    val showGoogleConflictSignUp: Boolean = false,
    val showSetPasswordDialog: Boolean = false,
    val toastMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(e: String) = _uiState.update { it.copy(email = e) }
    fun updatePassword(p: String) = _uiState.update { it.copy(password = p) }
    fun toggleMode() = _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearToast() = _uiState.update { it.copy(toastMessage = null) }
    fun dismissConflictDialog() = _uiState.update { it.copy(showGoogleConflictDialog = false) }
    fun dismissSetPasswordDialog() = _uiState.update { it.copy(showSetPasswordDialog = false) }

    fun submit() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (_uiState.value.isSignUpMode) {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    _uiState.update { it.copy(toastMessage = "Account created! Check your email to verify before signing in.", isSignUpMode = false) }
                } else {
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                }
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                val isSignUp = _uiState.value.isSignUpMode
                if (isSignUp && (msg.contains("user already registered") || msg.contains("already in use") || msg.contains("exists"))) {
                    _uiState.update { it.copy(showGoogleConflictDialog = true, showGoogleConflictSignUp = true) }
                } else if (!isSignUp && (msg.contains("invalid login credentials") || msg.contains("invalid password"))) {
                    _uiState.update { it.copy(toastMessage = "Sign-in failed. If you previously used Google, try \"Continue with Google\" below.") }
                } else {
                    _uiState.update { it.copy(error = e.message) }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun googleSignIn() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                SupabaseManager.client.auth.signInWith(Google)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Google sign-in failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
