package com.remindme.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.remindme.app.ui.components.*
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
    onOpenReminder: (String) -> Unit,
    onBack: () -> Unit = {}
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

    AppScaffold(
        snackbarHost = {},
        appBar = {
            Column {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircledBackButton(onClick = onBack)
                    Spacer(modifier = Modifier.width(12.dp))
                    TopBar(
                        title = "Notifications",
                        statusBarsPadding = false,
                        modifier = Modifier.weight(1f),
                        actions = {
                            if (selectedTab == 1 && inAppUnreadCount > 0) {
                                TextButton(onClick = { viewModel.markAllRead() }) {
                                    Text("Mark all read", color = Accent500)
                                }
                            }
                        }
                    )
                }
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Accent500
                        )
                    },
                    divider = { HorizontalDivider(color = Color.Transparent) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index) TextPrimary else TextTertiary,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AppPullToRefresh(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.loadData() }
            ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                when (selectedTab) {
                    0 -> UpcomingTab(upcoming, onOpenReminder)
                    1 -> InAppTab(uiState.inAppNotifications, viewModel::markRead, onOpenReminder)
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
fun InAppTab(
    notifications: List<InAppNotification>,
    onMarkRead: (String) -> Unit,
    onOpenReminder: (String) -> Unit
) {
    if (notifications.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
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

                AppCard(
                    borderRadius = 16.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clickable {
                            n.reminder_item_id?.let { onOpenReminder(it) }
                            if (isUnread) onMarkRead(n.id)
                        }
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
                Icon(Icons.Outlined.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = TextTertiary)
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
    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onOpenReminder(occ.item.id) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCard(
                borderRadius = 10.dp,
                modifier = Modifier.wrapContentSize()
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    AppIcon(
                        imageVector = if (isMissed) Icons.Outlined.Warning else Icons.Outlined.Notifications,
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
                AppCard(
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
