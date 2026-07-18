package com.remindme.app.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.*
import com.remindme.app.ui.theme.*
import java.time.LocalDateTime

val TASK_ICONS = listOf(
    Triple("trash", Icons.Default.Delete, "Take out trash"),
    Triple("water", Icons.Default.WaterDrop, "Drink water"),
    Triple("fitness", Icons.AutoMirrored.Filled.DirectionsRun, "Exercise"),
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
    var showDatePicker by remember { mutableStateOf(false) }

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
                    title = "Add Task",
                    statusBarsPadding = false,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            AppTextField(
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
                        AppCard(
                            borderRadius = 12.dp,
                            padding = 12.dp
                        ) {
                            Box(
                                modifier = Modifier.clickable { viewModel.updateIconKey(icon.first) }
                            ) {
                                AppIcon(
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
            
            DateTile(
                label = "Due Date & Time *",
                value = uiState.dueAt,
                placeholder = "Select date and time",
                onTap = { showDatePicker = true }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            AppTextField(
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
            
            AppButton(
                onClick = { viewModel.saveTask() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    Spinner(size = 20.dp)
                } else {
                    Text("Add Task", color = TextPrimary)
                }
            }
        }
        
        if (showDatePicker) {
            DateTimePickerDialog(
                initialDate = uiState.dueAt,
                onDismissRequest = { showDatePicker = false },
                onDateTimeSelected = {
                    viewModel.updateDueAt(it)
                    showDatePicker = false
                }
            )
        }
    }
}
