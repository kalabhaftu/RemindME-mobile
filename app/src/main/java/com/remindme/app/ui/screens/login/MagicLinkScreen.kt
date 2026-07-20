package com.remindme.app.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.*
import com.remindme.app.ui.theme.*

@Composable
fun MagicLinkScreen(
    viewModel: MagicLinkViewModel = viewModel(),
    onNavigateHome: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateHome()
    }

    AppScaffold(
        appBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(12.dp))
                TopBar(
                    title = "Magic Link",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState.step) {
                MagicLinkStep.INPUT -> InputStep(uiState, viewModel)
                MagicLinkStep.SENDING -> SendingStep()
                MagicLinkStep.SENT -> SentStep()
                MagicLinkStep.NOT_FOUND -> NotFoundStep(uiState, viewModel, onBack)
                MagicLinkStep.ERROR -> ErrorStep(uiState, viewModel)
            }
        }
    }
}

@Composable
private fun InputStep(uiState: MagicLinkUiState, viewModel: MagicLinkViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sign in with Magic Link",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We'll send you a magic link to sign in instantly.",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            placeholder = "you@example.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(uiState.error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = viewModel::sendMagicLink,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            tint = Accent500
        ) {
            Text("Send Magic Link", fontSize = 14.sp)
        }
    }
}

@Composable
private fun SendingStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Spinner(size = 32.dp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Sending magic link...",
            fontSize = 16.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun SentStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        AppIcon(
            imageVector = Icons.Outlined.MailOutline,
            modifier = Modifier.size(56.dp),
            color = Accent500
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Email sent!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Check your inbox and tap the magic link to sign in.",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Spinner(size = 28.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Waiting for you to tap the link...",
            fontSize = 13.sp,
            color = TextTertiary
        )
    }
}

@Composable
private fun NotFoundStep(uiState: MagicLinkUiState, viewModel: MagicLinkViewModel, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        AppIcon(
            imageVector = Icons.Outlined.PersonSearch,
            modifier = Modifier.size(56.dp),
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "No account found",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "There's no account with that email. Would you like to sign up instead?",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = { viewModel.retry() },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Try a different email", fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        AppButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            tint = Accent500
        ) {
            Text("Sign up instead", fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorStep(uiState: MagicLinkUiState, viewModel: MagicLinkViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        AppIcon(
            imageVector = Icons.Outlined.Warning,
            modifier = Modifier.size(56.dp),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Something went wrong",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            uiState.error ?: "An unexpected error occurred.",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = { viewModel.retry() },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Try again", fontSize = 14.sp)
        }
    }
}
