package com.remindme.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.remindme.app.ui.components.liquid.*
import com.remindme.app.ui.theme.*
import com.remindme.app.domain.models.OccurrenceStatus
import com.remindme.app.domain.models.ReminderOccurrence
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = viewModel(),
    onOpenReminder: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    val upcoming = viewModel.getUpcoming()
    val missed = viewModel.getMissed()
    val inAppUnreadCount = uiState.inAppNotifications.count { it.read_at == null }

    val tabs = listOf(
        "Upcoming (${upcoming.size})",
        if (inAppUnreadCount > 0) "In-app ($inAppUnreadCount)" else "In-app",
        "Missed (${missed.size})"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LiquidAppBar(
                    title = "Notifications",
                    actions = {
                        if (selectedTab == 1 && inAppUnreadCount > 0) {
                            TextButton(onClick = { viewModel.markAllRead() }) {
                                Text("Mark all read", color = Accent500)
                            }
                        }
                    },
                    bottom = {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Accent500
                                )
                            },
                            divider = { Divider(color = Color.Transparent) }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { 
                                        Text(
                                            title,
                                            color = if (selectedTab == index) TextPrimary else TextTertiary,
                                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                        ) 
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (uiState.isLoading && uiState.inAppNotifications.isEmpty() && uiState.allOccurrences.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LiquidSpinner()
                    }
                } else {
                    when (selectedTab) {
                        0 -> UpcomingTab(upcoming, onOpenReminder)
                        1 -> InAppTab(uiState.inAppNotifications, viewModel::markRead)
                        2 -> MissedTab(missed, onOpenReminder)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingTab(occurrences: List<ReminderOccurrence>, onOpenReminder: (String) -> Unit) {
    if (occurrences.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nothing upcoming.", color = TextSecondary)
        }
    } else {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val day3 = today.plusDays(3)

        val groups = mutableMapOf<String, MutableList<ReminderOccurrence>>()
        occurrences.forEach { occ ->
            val key = when {
                occ.date == today -> "Today"
                occ.date == tomorrow -> "Tomorrow"
                !occ.date.isAfter(day3) -> "Next 3 days"
                else -> "Next 7 days"
            }
            groups.getOrPut(key) { mutableListOf() }.add(occ)
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            groups.forEach { (key, list) ->
                item {
                    Text(
                        text = key.uppercase(),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(list) { occ ->
                    OccurrenceTile(occ = occ, onOpenReminder = onOpenReminder)
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun InAppTab(notifications: List<InAppNotification>, onMarkRead: (String) -> Unit) {
    if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No in-app notifications yet.", color = TextSecondary, fontSize = 16.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(notifications, key = { it.id }) { n ->
                val isUnread = n.read_at == null
                val created = try { Instant.parse(n.created_at) } catch (e: Exception) { Instant.now() }
                val formatter = DateTimeFormatter.ofPattern("MMM d, HH:mm").withZone(ZoneId.systemDefault())

                FloatingGlassContainer(
                    borderRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clickable(enabled = isUnread) { onMarkRead(n.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 10.dp)
                                    .size(8.dp)
                                    .background(Accent500, CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = n.title ?: "Reminder",
                                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Medium,
                                color = TextPrimary
                            )
                            if (n.body != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(n.body, fontSize = 13.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(formatter.format(created), fontSize = 11.sp, color = TextTertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissedTab(occurrences: List<ReminderOccurrence>, onOpenReminder: (String) -> Unit) {
    if (occurrences.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No missed reminders.", color = TextSecondary, fontSize = 16.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(occurrences) { occ ->
                OccurrenceTile(occ = occ, isMissed = true, onOpenReminder = onOpenReminder)
            }
        }
    }
}

@Composable
fun OccurrenceTile(occ: ReminderOccurrence, isMissed: Boolean = false, onOpenReminder: (String) -> Unit) {
    FloatingGlassContainer(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onOpenReminder(occ.item.id) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingGlassContainer(
                borderRadius = 10.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    LiquidIcon(
                        imageVector = if (isMissed) Icons.Rounded.Warning else Icons.Rounded.Notifications,
                        size = 18.dp,
                        color = if (isMissed) StateWarning else Accent500
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(occ.item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(3.dp))
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                Text("${occ.item.category} · ${occ.date.format(formatter)}", fontSize = 12.sp, color = TextSecondary)
            }
            if (!isMissed && occ.status == OccurrenceStatus.TODAY) {
                FloatingGlassContainer(
                    borderRadius = 20.dp,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("TODAY", fontSize = 10.sp, color = Accent400, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
            } else if (!isMissed) {
                Text(occ.date.format(DateTimeFormatter.ofPattern("EEE")), fontSize = 12.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            }
        }
    }
}
