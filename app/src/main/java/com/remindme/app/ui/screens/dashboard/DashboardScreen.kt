package com.remindme.app.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.ui.components.liquid.FloatingGlassContainer
import com.remindme.app.ui.components.liquid.LiquidIcon
import com.remindme.app.ui.components.liquid.LiquidSpinner
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToHolidays: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    if (uiState.isLoading && uiState.reminders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            LiquidSpinner()
        }
        return
    }

    val peopleCount = uiState.reminders.count { it.category == CategoryType.PERSON }
    val subsCount = uiState.reminders.count { it.category == CategoryType.SUBSCRIPTION }
    val tasksCount = uiState.reminders.count { it.category == CategoryType.TASK }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(modifier = Modifier.weight(1f), label = "People", count = peopleCount)
            StatCard(modifier = Modifier.weight(1f), label = "Subscriptions", count = subsCount)
            StatCard(modifier = Modifier.weight(1f), label = "Tasks", count = tasksCount)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAddTile(modifier = Modifier.weight(1f), label = "Person", icon = Icons.Default.Person, onTap = onNavigateToAddPerson)
            QuickAddTile(modifier = Modifier.weight(1f), label = "Subscription", icon = Icons.Default.CreditCard, onTap = onNavigateToAddSubscription)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAddTile(modifier = Modifier.weight(1f), label = "Task", icon = Icons.Default.Add, onTap = onNavigateToAddTask)
            QuickAddTile(modifier = Modifier.weight(1f), label = "Holiday", icon = Icons.Default.Event, onTap = onNavigateToHolidays)
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        com.remindme.app.ui.components.CalendarGrid(
            currentMonth = uiState.currentMonth,
            occurrences = uiState.occurrences,
            selectedDate = uiState.selectedDate,
            onSelectDate = { viewModel.onDateSelected(it) },
            onMonthChange = { viewModel.onMonthChange(it) }
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        com.remindme.app.ui.components.UpcomingPanel(
            occurrences = uiState.occurrences,
            onMarkDone = { id, date ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.markDone(id, date)
            },
            onSnooze = { id, date -> viewModel.snooze(id, date) },
            onEdit = { /* TODO */ }
        )

        uiState.selectedDate?.let { date ->
            SelectedDaySheet(
                date = date,
                occurrences = uiState.occurrences.filter { it.date == date },
                onMarkDone = { id, occDate ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.markDone(id, occDate)
                },
                onDismiss = { viewModel.clearSelectedDate() }
            )
        }
    }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, count: Int) {
    FloatingGlassContainer(
        modifier = modifier,
        borderRadius = 16.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Accent500,
                // fontFamily = FontFamily.Monospace // Can add later
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun QuickAddTile(modifier: Modifier = Modifier, label: String, icon: ImageVector, onTap: () -> Unit) {
    FloatingGlassContainer(
        modifier = modifier.clickable { onTap() },
        borderRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            LiquidIcon(imageVector = icon, size = 20.dp, color = TextPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}
