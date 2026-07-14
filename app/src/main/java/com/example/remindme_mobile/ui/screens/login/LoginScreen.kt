package com.example.remindme_mobile.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme_mobile.ui.components.GoogleLogo
import com.example.remindme_mobile.ui.components.liquid.LiquidButton
import com.example.remindme_mobile.ui.components.liquid.LiquidSpinner
import com.example.remindme_mobile.ui.components.liquid.LiquidTextField
import com.example.remindme_mobile.ui.theme.*
import com.kyant.backdrop.rememberBackdrop

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = rememberBackdrop()

    if (uiState.toastMessage != null) {
        // Show toast or snackbar
        LaunchedEffect(uiState.toastMessage) {
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RemindME",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.isSignUpMode) "Create your account" else "Welcome back",
                fontSize = 18.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(48.dp))

            LiquidTextField(
                value = uiState.email,
                onValueChange = viewModel::updateEmail,
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LiquidTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                obscureText = true,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(24.dp))

            LiquidButton(
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    LiquidSpinner(size = 20.dp, color = Color.White)
                } else {
                    Text(
                        text = if (uiState.isSignUpMode) "Create Account" else "Sign In",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { if (!uiState.isLoading) viewModel.toggleMode() }
            ) {
                Text(
                    text = if (uiState.isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
                Text(
                    text = "OR",
                    color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LiquidButton(
                onClick = viewModel::googleSignIn,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleLogo(size = 24.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Continue with Google", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
