package com.remindme.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.R
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.ui.components.EmptyState
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.Spinner
import com.remindme.app.ui.components.SwipeDeleteBackground
import com.remindme.app.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = viewModel(),
    onNavigateToEdit: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.sortedTasks.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp).align(Alignment.TopCenter),
                action = { TextButton(onClick = { viewModel.fetchTasks() }) { Text("Retry") } }
            ) { Text(error) }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {
        if (uiState.isLoading) {
            item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Accent500) }
        }
        if (tasks.isEmpty() && !uiState.isLoading) {
            item {
                Box(modifier = Modifier.padding(top = 120.dp)) {
                    EmptyState(
                        iconRes = R.drawable.empty_tasks,
                        message = "No tasks yet. Tap + to add one."
                    )
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteTask(task.id)
                            true
                        } else {
                            false
                        }
                    }
                )
                val isSwiping = dismissState.currentValue != SwipeToDismissBoxValue.Settled ||
                    dismissState.targetValue != SwipeToDismissBoxValue.Settled

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        SwipeDeleteBackground(
                            dismissState = dismissState,
                            cornerRadius = 16.dp,
                            bottomPadding = 10.dp,
                            endPadding = 20.dp
                        )
                    },
                    content = {
                        TaskRow(
                            item = task,
                            onClick = { onNavigateToEdit(task.id) },
                            onMarkDone = { viewModel.markTaskDone(task) }
                        )
                    }
                )
            }
        }
    }
    }
}

@Composable
fun TaskRow(item: ReminderItem, onClick: () -> Unit, onMarkDone: () -> Unit) {
    val dueStr = item.task?.dueAt

    fun getIconForKey(key: String?): ImageVector {
        return when (key) {
            "water" -> Icons.Rounded.WaterDrop
            "trash" -> Icons.Rounded.Delete
            "fitness" -> Icons.Rounded.FitnessCenter
            "study" -> Icons.Rounded.MenuBook
            "rent" -> Icons.Rounded.Home
            "medication" -> Icons.Rounded.Medication
            else -> Icons.Rounded.List
        }
    }

    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSurface3)
            ) {
                Box(modifier = Modifier.padding(10.dp)) {
                    AppIcon(
                        imageVector = getIconForKey(item.iconKey),
                        color = Accent500,
                        size = 22.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (dueStr != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            imageVector = Icons.Rounded.AccessTime,
                            color = TextTertiary,
                            size = 12.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val formattedDate = try {
                            val dt = LocalDateTime.parse(dueStr.replace("Z", ""))
                            dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy · HH:mm"))
                        } catch (e: Exception) {
                            dueStr
                        }
                        Text(
                            text = formattedDate,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgSurface3)
                    .clickable { onMarkDone() }
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    AppIcon(
                        imageVector = Icons.Rounded.CheckCircle,
                        color = StateSuccess,
                        size = 24.dp
                    )
                }
            }
        }
    }
}
