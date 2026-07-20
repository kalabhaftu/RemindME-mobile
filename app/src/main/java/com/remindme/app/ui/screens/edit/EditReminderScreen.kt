package com.remindme.app.ui.screens.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.AppButton
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.AppIcons
import com.remindme.app.ui.components.AppScaffold
import com.remindme.app.ui.components.AppTextField
import com.remindme.app.ui.components.CircledBackButton
import com.remindme.app.ui.components.Spinner
import com.remindme.app.ui.components.TopBar
import com.remindme.app.ui.components.appSurfaceColor
import com.remindme.app.ui.theme.AppColors
import com.remindme.app.ui.theme.StateDanger
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary

@Composable
fun EditReminderScreen(
    reminderId: String,
    viewModel: EditReminderViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(reminderId) { viewModel.loadReminder(reminderId) }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onBack()
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        appBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircledBackButton(onClick = onBack)
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                TopBar(title = "Edit reminder", statusBarsPadding = false, modifier = Modifier.weight(1f))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.reminder == null) {
            Column(Modifier.fillMaxSize().padding(paddingValues), horizontalAlignment = Alignment.CenterHorizontally) {
                Spinner()
            }
        } else if (uiState.reminder != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppTextField(
                    value = uiState.draftName,
                    onValueChange = viewModel::updateName,
                    placeholder = "Name"
                )
                AppTextField(
                    value = uiState.draftNotes,
                    onValueChange = viewModel::updateNotes,
                    placeholder = "Notes",
                    singleLine = false,
                    maxLines = 5
                )
                Text(
                    text = uiState.reminder?.category?.name.orEmpty().lowercase().replace("_", " "),
                    color = TextSecondary
                )
                AppButton(
                    onClick = viewModel::saveChanges,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    tint = AppColors.accent500
                ) {
                    if (uiState.isSaving) Spinner(size = 20.dp) else Text("Save changes", color = TextPrimary)
                }
                AppButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    surfaceColor = appSurfaceColor()
                ) {
                    AppIcon(iconRes = AppIcons.Delete, color = StateDanger)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text("Delete reminder", color = StateDanger)
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = appSurfaceColor(elevated = true),
            title = { Text("Delete reminder?", color = TextPrimary) },
            text = { Text("This reminder and its schedule will be removed.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    viewModel.deleteReminder(reminderId)
                }) { Text("Delete", color = StateDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}
