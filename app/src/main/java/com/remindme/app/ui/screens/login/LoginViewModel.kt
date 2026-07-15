package com.remindme.app.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
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
                } else {
                    _uiState.update { it.copy(error = "Unexpected credential type") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Google sign-in failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun sendMagicLink() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter email for magic link") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                SupabaseManager.client.auth.signInWith(OTP) {
                    this.email = email
                    createUser = true
                }
                _uiState.update { it.copy(toastMessage = "Magic link sent! Check your email to sign in.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to send magic link: ${e.message}") }
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
                _uiState.update { it.copy(toastMessage = "Password set! You can now sign in with email too.", showSetPasswordDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(toastMessage = "Failed to set password: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
