package com.remindme.app.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.remindme.app.ui.components.BottomSheetPickerItem
import com.remindme.app.ui.components.PickerField
import com.remindme.app.ui.components.*
import com.remindme.app.ui.theme.*
import com.remindme.app.utils.AppConstants
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    personId: String? = null,
    viewModel: AddPersonViewModel = viewModel(key = personId?.let { "edit-person-$it" } ?: "add-person"),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Snackbar state for errors
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(personId) {
        if (personId != null) {
            viewModel.loadPerson(personId)
        } else {
            viewModel.resetForNewPerson()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.readBytes()
            if (bytes != null) {
                val type = context.contentResolver.getType(it) ?: "image/jpeg"
                val extension = type.substringAfterLast("/")
                viewModel.uploadAvatar(bytes, extension)
            }
        }
    }

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
                    title = if (personId != null) "Edit Person" else "Add Person",
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Placeholder
            AppCard(
                borderRadius = 40.dp,
                padding = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (!uiState.isUploadingAvatar) {
                                imagePickerLauncher.launch("image/*")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isUploadingAvatar) {
                        Spinner(size = 28.dp)
                    } else if (uiState.avatarUrl != null) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        AppIcon(
                            iconRes = AppIcons.CameraAlt,
                            color = TextTertiary,
                            size = 28.dp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AppTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                placeholder = "Full name *"
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            DateTile(
                label = "Birthdate *",
                value = uiState.birthdate,
                placeholder = "Select birthdate",
                formatter = { it.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) },
                onTap = {
                    showDatePicker = true
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppCard(borderRadius = 16.dp) {
                PickerField(
                    label = "Gender",
                    value = uiState.gender,
                    displayValue = { AppConstants.GENDER_LABELS[it] ?: it },
                    title = "Select gender",
                    items = AppConstants.GENDER_LABELS.map { BottomSheetPickerItem(it.key, it.value) },
                    onChanged = { viewModel.updateGender(it) }
                )
            }
            
            AppCard(borderRadius = 16.dp) {
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
            }
            
            if (uiState.relationship == "other") {
                AppTextField(
                    value = uiState.customRelationship,
                    onValueChange = { viewModel.updateCustomRelationship(it) },
                    placeholder = "Custom relationship (e.g. Mentor)"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            AppTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = "Notes"
                // maxLines = 3 // To be supported by AppTextField
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
            
            AppButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.savePerson()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    Spinner(size = 20.dp)
                } else {
                    Text("Add Person", color = TextPrimary)
                }
            }
        }
        
        if (showDatePicker) {
            DateTimePickerDialog(
                initialDate = uiState.birthdate,
                onDismissRequest = { showDatePicker = false },
                onDateTimeSelected = {
                    viewModel.updateBirthdate(it)
                    showDatePicker = false
                },
                dateOnly = true
            )
        }
    }
}
