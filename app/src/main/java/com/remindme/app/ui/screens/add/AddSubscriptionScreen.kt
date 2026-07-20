package com.remindme.app.ui.screens.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.BottomSheetPickerItem
import com.remindme.app.ui.components.PickerField
import com.remindme.app.ui.components.*
import com.remindme.app.ui.theme.*
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    subscriptionId: String? = null,
    viewModel: AddSubscriptionViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionId) {
        if (subscriptionId == null) {
            viewModel.resetForNewSubscription()
        } else {
            viewModel.loadSubscription(subscriptionId)
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
                    title = if (subscriptionId == null) "Add Subscription" else "Edit Subscription",
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
                placeholder = "Service name *"
            )

            if (uiState.isResolvingLogo) {
                Text(
                    text = "Fetching logo...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (uiState.logoUrl != null) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ResilientBrandImage(
                        name = uiState.name,
                        imageUrl = uiState.logoUrl,
                        modifier = Modifier.size(40.dp),
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logo preview",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DateTile(
                label = "Renewal Date *",
                value = uiState.renewalDate,
                placeholder = "Select renewal date",
                formatter = { it.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) },
                onTap = { showDatePicker = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row {
                Box(modifier = Modifier.weight(2f)) {
                    AppTextField(
                        value = uiState.amount,
                        onValueChange = { viewModel.updateAmount(it) },
                        placeholder = "Amount",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    AppCard(borderRadius = 16.dp) {
                        PickerField(
                            label = "Currency",
                            value = uiState.currency,
                            displayValue = { it },
                            title = "Select currency",
                            items = listOf("USD", "EUR", "GBP").map { BottomSheetPickerItem(it, it) },
                            onChanged = { viewModel.updateCurrency(it) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppCard(borderRadius = 16.dp) {
                PickerField(
                    label = "Cycle",
                    value = uiState.cycle,
                    displayValue = { it.replaceFirstChar { char -> char.uppercaseChar() } },
                    title = "Select billing cycle",
                    items = listOf(
                        BottomSheetPickerItem("weekly", "Weekly"),
                        BottomSheetPickerItem("monthly", "Monthly"),
                        BottomSheetPickerItem("yearly", "Yearly")
                    ),
                    onChanged = { viewModel.updateCycle(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                placeholder = "Notes"
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Notification Preferences",
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NotificationPrefsForm(
                matrix = uiState.notificationPrefs,
                onChanged = { viewModel.updateNotificationPrefs(it) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AppButton(
                onClick = { viewModel.saveSubscription() },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    Spinner(size = 20.dp)
                } else {
                    Text(if (subscriptionId == null) "Save" else "Save Subscription", color = TextPrimary)
                }
            }
        }
        
        if (showDatePicker) {
            DateTimePickerDialog(
                initialDate = uiState.renewalDate,
                onDismissRequest = { showDatePicker = false },
                onDateTimeSelected = {
                    viewModel.updateRenewalDate(it)
                    showDatePicker = false
                },
                dateOnly = true
            )
        }
    }
}
