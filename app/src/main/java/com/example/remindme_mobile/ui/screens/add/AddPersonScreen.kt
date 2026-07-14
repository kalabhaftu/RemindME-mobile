package com.example.remindme_mobile.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remindme_mobile.ui.components.liquid.*
import com.example.remindme_mobile.ui.theme.AppColors
import com.example.remindme_mobile.ui.theme.StateDanger
import com.example.remindme_mobile.ui.theme.TextPrimary
import com.example.remindme_mobile.utils.AppConstants
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    viewModel: AddPersonViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar state for errors
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
            LiquidAppBar(
                title = "Add Person",
                actions = {
                    // if editing, show delete
                }
            )
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Placeholder
            FloatingGlassContainer(
                borderRadius = 40.dp,
                padding = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            // TODO Image Picker
                        },
                    contentAlignment = Alignment.Center
                ) {
                    LiquidIcon(
                        imageVector = Icons.Default.CameraAlt,
                        tint = AppColors.textTertiary,
                        size = 28.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LiquidTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Full name *"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LiquidDateTile(
                label = "Birthdate & Time *",
                value = uiState.birthdate,
                placeholder = "Select date and time",
                onTap = {
                    // TODO Date/Time Picker dialogs
                    viewModel.updateBirthdate(LocalDateTime.now())
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PickerField(
                label = "Gender",
                value = uiState.gender,
                displayValue = { AppConstants.GENDER_LABELS[it] ?: it },
                title = "Select gender",
                items = AppConstants.GENDER_LABELS.map { BottomSheetPickerItem(it.key, it.value) },
                onChanged = { viewModel.updateGender(it) }
            )
            
            PickerField(
                label = "Relationship",
                value = uiState.relationship,
                displayValue = {
                    val entry = AppConstants.RELATIONSHIP_LABELS[it]
                    if (entry != null) "${entry.second} ${entry.first}" else it
                },
                title = "Select relationship",
                items = AppConstants.RELATIONSHIP_LABELS.map { 
                    BottomSheetPickerItem(it.key, "${it.value.second} ${it.value.first}")
                },
                onChanged = { viewModel.updateRelationship(it) }
            )
            
            if (uiState.relationship == "other") {
                LiquidTextField(
                    value = uiState.customRelationship,
                    onValueChange = { viewModel.updateCustomRelationship(it) },
                    placeholder = "Custom relationship (e.g. Mentor)"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LiquidTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = "Notes"
                // maxLines = 3 // To be supported by LiquidTextField
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Notification preferences",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NotificationPrefsForm(
                matrix = uiState.notificationPrefs,
                onChanged = { viewModel.updateNotificationPrefs(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LiquidButton(
                onClick = { viewModel.savePerson() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    LiquidSpinner(size = 20.dp)
                } else {
                    Text("Add Person", color = TextPrimary)
                }
            }
        }
    }
}
