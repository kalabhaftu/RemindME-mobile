package com.remindme.app.ui.screens.login

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
import androidx.compose.ui.platform.LocalContext
import com.remindme.app.ui.components.GoogleLogo
import com.remindme.app.ui.components.liquid.LiquidButton
import com.remindme.app.ui.components.liquid.LiquidSpinner
import com.remindme.app.ui.components.liquid.LiquidTextField
import com.remindme.app.ui.theme.*


@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    com.remindme.app.ui.components.liquid.LiquidScaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            LiquidTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = "Password",
                obscureText = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
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
                onClick = { viewModel.googleSignIn(context) },
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
            
            Spacer(modifier = Modifier.height(16.dp))

            LiquidButton(
                onClick = viewModel::sendMagicLink,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(text = "Send Magic Link", color = Color.White, fontSize = 16.sp)
            }
        }
        
        if (uiState.toastMessage != null) {
            LaunchedEffect(uiState.toastMessage) {
                android.widget.Toast.makeText(context, uiState.toastMessage, android.widget.Toast.LENGTH_LONG).show()
                viewModel.clearToast()
            }
        }

        if (uiState.showGoogleConflictDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissConflictDialog,
                title = { Text("Account uses Google Sign-In", color = TextPrimary) },
                text = {
                    Text(
                        if (uiState.showGoogleConflictSignUp) "This email is already linked to a Google account. Continue with Google instead."
                        else "This email is linked to a Google account. You can continue with Google, or set a password to also use email sign-in.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.dismissConflictDialog()
                        viewModel.googleSignIn(context)
                    }) {
                        Text("Continue with Google", color = Accent500)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.dismissConflictDialog()
                    }) {
                        Text("Set Password", color = Accent500)
                    }
                },
                containerColor = BgSurface2,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }

        if (uiState.showSetPasswordDialog) {
            var password by remember { mutableStateOf("") }
            var confirm by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = viewModel::dismissSetPasswordDialog,
                title = { Text("Set Password", color = TextPrimary) },
                text = {
                    Column {
                        Text("You signed in with Google. Set a password to also sign in with email.", color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        LiquidTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "New Password",
                            obscureText = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            label = "Confirm Password",
                            obscureText = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (password == confirm && password.length >= 8) {
                            viewModel.savePassword(password)
                        }
                    }) {
                        Text("Save", color = Accent500)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissSetPasswordDialog) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = BgSurface2,
                titleContentColor = TextPrimary,
                textContentColor = TextSecondary
            )
        }
    }
}
}
