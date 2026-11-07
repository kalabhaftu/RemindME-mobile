package com.remindme.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.TopBar
import com.remindme.app.ui.components.AppScaffold
import com.remindme.app.ui.components.AppIcons
import com.remindme.app.ui.theme.*

@Composable
fun NotificationHelpScreen(onBack: () -> Unit) {
    AppScaffold(
        snackbarHost = {},
        appBar = {
            TopBar(
                title = "Notification Guide",
                leading = {
                    androidx.compose.material3.IconButton(onClick = onBack) {
                        com.remindme.app.ui.components.AppIcon(
                            iconRes = AppIcons.ArrowBack,
                            color = TextPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp, start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            item {
                Text(
                    text = "Event Time vs. Notification Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Event Date & Time (Date Picker)",
                    description = "Defines when the event itself occurs or when the task is due (e.g., November 10 at 4:30 PM). For all-day events like Birthdays and Subscription renewal dates, only the calendar date is selected.",
                    icon = AppIcons.CalendarToday
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Notification Preferences (When Alerts Fire)",
                    description = "Controls when and how RemindME alerts you relative to the event date. You can configure separate alert times and advance notice for Push notifications, Telegram, and Email.",
                    icon = AppIcons.Notifications
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Delivery Times",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "At time of event",
                    description = "Fires at 12:00 PM for all-day events, or at the exact event hour for timed tasks.",
                    icon = AppIcons.Timer
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Morning of",
                    description = "Fires at 09:00 AM in your timezone on the target date.",
                    icon = AppIcons.WbSunny
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Noon of",
                    description = "Fires at 12:00 PM in your timezone on the target date.",
                    icon = AppIcons.AccessTime
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Evening of",
                    description = "Fires at 18:00 (6:00 PM) in your timezone on the target date.",
                    icon = AppIcons.Snooze
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Custom",
                    description = "Fires at any exact hour and minute you choose (e.g., 15:30).",
                    icon = AppIcons.Settings
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Advance Notice (Offsets)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Same day",
                    description = "Alerts you on the day of the event at your chosen notification time.",
                    icon = AppIcons.Event
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "1 day before",
                    description = "Alerts you 1 day prior to the event (for example, the evening before a friend's birthday so you have time to prepare).",
                    icon = AppIcons.DateRange
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "2 days before",
                    description = "Alerts you 2 days prior to the event date.",
                    icon = AppIcons.DateRange
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "1 week before",
                    description = "Alerts you 7 days in advance for long-term preparation.",
                    icon = AppIcons.CalendarViewWeek
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Delivery Channels",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "In-App & Push",
                    description = "Instant native notifications delivered directly to your Android device with quick actions.",
                    icon = AppIcons.Smartphone
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Telegram",
                    description = "Direct messages sent to your Telegram chat via the RemindME bot.",
                    icon = AppIcons.Send
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                HelpCard(
                    title = "Email",
                    description = "Full reminder summaries delivered directly to your email inbox.",
                    icon = AppIcons.Email
                )
            }
        }
    }
}

@Composable
fun HelpCard(title: String, description: String, icon: Int) {
    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .background(Accent500.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                com.remindme.app.ui.components.AppIcon(iconRes = icon, contentDescription = null, tint = Accent500, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
