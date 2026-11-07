package com.remindme.app.ui.screens.preview

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.AppIcons
import com.remindme.app.ui.components.AppScaffold
import com.remindme.app.ui.components.CircledBackButton
import com.remindme.app.ui.components.Spinner
import com.remindme.app.ui.components.TopBar
import com.remindme.app.ui.screens.edit.EditReminderViewModel
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.remindme.app.ui.components.AppButton
import com.remindme.app.ui.components.appSurfaceColor
import com.remindme.app.ui.theme.StateDanger

@Composable
fun ReminderPreviewScreen(
    reminderId: String,
    onBack: () -> Unit,
    onEdit: (ReminderItem) -> Unit = {}
) {
    val context = LocalContext.current
    val vm: EditReminderViewModel = viewModel(
        key = "preview-reminder-$reminderId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EditReminderViewModel(context.applicationContext as Application) as T
        }
    )
    val state by vm.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    LaunchedEffect(reminderId) { vm.loadReminder(reminderId) }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onBack()
    }

    AppScaffold(appBar = {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircledBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            TopBar(
                title = state.reminder?.name ?: "Reminder",
                statusBarsPadding = false,
                modifier = Modifier.weight(1f),
                actions = {
                    state.reminder?.let { reminder ->
                        IconButton(onClick = { onEdit(reminder) }) {
                            AppIcon(iconRes = AppIcons.Edit, color = TextSecondary)
                        }
                    }
                }
            )
        }
    }) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Spinner()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                state.reminder?.let { reminder ->
                    Text(reminder.name, color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(reminder.category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, color = TextSecondary)
                    PreviewBlock("Details") {
                        reminder.notes?.takeIf { it.isNotBlank() }?.let { PreviewValue("Notes", it) }
                        reminder.person?.birthdate?.let { PreviewValue("Birthday", it) }
                        reminder.subscription?.let { sub ->
                            sub.renewalDate?.let { PreviewValue("Next Renewal", it) }
                            if (sub.billingAmount != null) {
                                val amountStr = "${sub.billingCurrency ?: "USD"} ${sub.billingAmount}"
                                PreviewValue("Billing Amount", amountStr)
                            }
                            sub.cycle?.let { PreviewValue("Billing Cycle", it.replaceFirstChar { c -> c.uppercase() }) }
                        }
                        reminder.task?.let { task ->
                            task.dueAt?.let { PreviewValue("Due Date", it) }
                            reminder.recurrence?.let { rec ->
                                if (rec.frequency != "none") {
                                    PreviewValue("Repeating", rec.frequency.replaceFirstChar { c -> c.uppercase() })
                                }
                            }
                        }
                        reminder.holiday?.holidayDate?.let { PreviewValue("Date", it) }
                    }
                    PreviewBlock("Notifications") {
                        val enabledPrefs = reminder.notificationPreferences.orEmpty().filter { it.enabled }
                        if (enabledPrefs.isEmpty()) {
                            Text("No active notifications", color = TextSecondary, fontSize = 14.sp)
                        } else {
                            enabledPrefs.forEach {
                                val offsetStr = when (it.offsetDays) {
                                    0 -> "Same day"
                                    1 -> "1 day before"
                                    7 -> "1 week before"
                                    14 -> "2 weeks before"
                                    else -> "${it.offsetDays} days before"
                                }
                                val leadStr = it.leadTime.replace('_', ' ')
                                val timeStr = if (it.leadTime == "custom" && it.customTime.isNotBlank()) " (${it.customTime})" else ""
                                PreviewValue(
                                    label = it.channel.replace('_', ' ').replaceFirstChar { c -> c.uppercase() },
                                    value = "$offsetStr · $leadStr$timeStr"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.fillMaxWidth(),
                        surfaceColor = appSurfaceColor()
                    ) {
                        AppIcon(iconRes = AppIcons.Delete, color = StateDanger)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete reminder", color = StateDanger)
                    }
                } ?: Text("This reminder is no longer available.", color = TextSecondary)
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = appSurfaceColor(elevated = true),
            title = { Text("Delete reminder?", color = TextPrimary) },
            text = { Text("This will permanently remove this reminder and its notifications.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    vm.deleteReminder(reminderId)
                }) { Text("Delete", color = StateDanger) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun PreviewBlock(title: String, content: @Composable () -> Unit) {
    AppCard(borderRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
private fun PreviewValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 15.sp)
    }
}
