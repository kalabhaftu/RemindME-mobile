package com.remindme.app.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.liquid.*
import com.remindme.app.ui.theme.*
import java.time.LocalDateTime

val TASK_ICONS = listOf(
    Triple("trash", Icons.Default.Delete, "Take out trash"),
    Triple("water", Icons.Default.WaterDrop, "Drink water"),
    Triple("fitness", Icons.Default.DirectionsRun, "Exercise"),
    Triple("study", Icons.Default.Book, "Study"),
    Triple("rent", Icons.Default.Home, "Pay rent"),
    Triple("medication", Icons.Default.MedicalServices, "Medication")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: AddTaskViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBack()
        }
    }

    LiquidScaffold(
        appBar = {
            LiquidAppBar(title = "Add Task")
        },
        snackbarHost = {
            LiquidSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            LiquidTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Task name *"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Icon",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                TASK_ICONS.forEach { icon ->
                    val selected = uiState.iconKey == icon.first
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        FloatingGlassContainer(
                            borderRadius = 12.dp,
                            padding = 12.dp
                        ) {
                            Box(
                                modifier = Modifier.clickable { viewModel.updateIconKey(icon.first) }
                            ) {
                                LiquidIcon(
                                    imageVector = icon.second,
                                    size = 22.dp,
                                    color = if (selected) Accent500 else TextTertiary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LiquidDateTile(
                label = "Due Date & Time *",
                value = uiState.dueAt,
                placeholder = "Select date and time",
                onTap = {
                    // TODO Date/Time Picker dialogs
                    viewModel.updateDueAt(LocalDateTime.now())
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LiquidTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = "Notes"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (uiState.notificationPrefs.isNotEmpty()) {
                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                NotificationPrefsForm(
                    matrix = uiState.notificationPrefs,
                    onChanged = { viewModel.updateNotificationPrefs(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LiquidButton(
                onClick = { viewModel.saveTask() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    LiquidSpinner(size = 20.dp)
                } else {
                    Text("Add Task", color = TextPrimary)
                }
            }
        }
    }
}
