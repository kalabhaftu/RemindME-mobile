package com.remindme.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSignUpMode: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val messageIsError: Boolean = false,
    val showGoogleConflictDialog: Boolean = false,
    val showGoogleConflictSignUp: Boolean = false,
    val showSetPasswordDialog: Boolean = false,
    val unverifiedEmail: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateHome = MutableSharedFlow<Unit>()
    val navigateHome: SharedFlow<Unit> = _navigateHome.asSharedFlow()

    fun updateEmail(e: String) = _uiState.update { it.copy(email = e, error = null) }
    fun updatePassword(p: String) = _uiState.update { it.copy(password = p, error = null) }
    fun toggleMode() = _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null, message = null, unverifiedEmail = null) }
    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
    fun dismissConflictDialog() = _uiState.update { it.copy(showGoogleConflictDialog = false) }
    fun dismissSetPasswordDialog() {
        _uiState.update { it.copy(showSetPasswordDialog = false) }
        viewModelScope.launch { _navigateHome.emit(Unit) }
    }

    fun resetDialogs() = _uiState.update {
        it.copy(showGoogleConflictDialog = false, showGoogleConflictSignUp = false, showSetPasswordDialog = false, message = null)
    }

    fun resendVerification() {
        val email = _uiState.value.email.trim()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                SupabaseManager.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = _uiState.value.password
                }
                _uiState.update { it.copy(message = "Verification email sent! Check your inbox.", messageIsError = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to resend verification. Try again.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun submit() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }

        val isSignUp = _uiState.value.isSignUpMode
        if (isSignUp && password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, message = null, unverifiedEmail = null) }
            try {
                if (isSignUp) {
                    SupabaseManager.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    _uiState.update { it.copy(message = "Account created! Check your email to verify before signing in.", messageIsError = false, isSignUpMode = false) }
                } else {
                    SupabaseManager.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                    _navigateHome.emit(Unit)
                }
            } catch (e: Exception) {
                val msg = e.message?.lowercase() ?: ""
                if (isSignUp && (msg.contains("user already registered") || msg.contains("already in use") || msg.contains("exists"))) {
                    _uiState.update { it.copy(showGoogleConflictDialog = true, showGoogleConflictSignUp = true) }
                } else if (!isSignUp && msg.contains("email not confirmed")) {
                    _uiState.update { it.copy(message = "Email not yet verified. Check your inbox or request a new verification email.", messageIsError = true, unverifiedEmail = email) }
                } else if (!isSignUp && (msg.contains("invalid login credentials") || msg.contains("invalid password"))) {
                    _uiState.update { it.copy(message = "Incorrect email or password. If you previously used Google, try \"Continue with Google\".", messageIsError = true) }
                } else {
                    _uiState.update { it.copy(error = "Authentication failed. Please try again.") }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun googleSignIn(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val credentialManager = androidx.credentials.CredentialManager.create(context)
                val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(com.remindme.app.BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = androidx.credentials.GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    SupabaseManager.client.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.IDToken) {
                        this.provider = io.github.jan.supabase.auth.providers.Google
                        this.idToken = idToken
                    }

                    _uiState.update { it.copy(showSetPasswordDialog = true) }
                } else {
                    _uiState.update { it.copy(error = "Unexpected credential type") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Google sign-in failed. Please try again.") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun savePassword(password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                SupabaseManager.client.auth.updateUser {
                    this.password = password
                }
                _uiState.update { it.copy(message = "Password set! You can now sign in with email too.", messageIsError = false, showSetPasswordDialog = false) }
                _navigateHome.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Failed to set password. Please try again.", messageIsError = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
