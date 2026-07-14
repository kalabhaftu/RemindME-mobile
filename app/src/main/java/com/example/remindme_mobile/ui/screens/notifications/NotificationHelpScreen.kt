package com.example.remindme_mobile.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme_mobile.ui.components.liquid.FloatingGlassContainer
import com.example.remindme_mobile.ui.components.liquid.LiquidAppBar
import com.example.remindme_mobile.ui.theme.*

@Composable
fun NotificationHelpScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LiquidAppBar(
                    title = "Notification Help",
                    showBackButton = true,
                    onBackClick = onBack
                )
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp, start = 16.dp, end = 16.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                item {
                    Text(
                        text = "How Notifications Work",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    HelpCard(
                        title = "At time of event",
                        description = "The notification is sent exactly at the time the event happens.",
                        icon = Icons.Rounded.Timer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    HelpCard(
                        title = "Morning of",
                        description = "The notification is sent in the morning on the day of the event. By default, this is 9:00 AM, but you can change it in Settings -> Custom Default Time.",
                        icon = Icons.Rounded.WbSunny
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    HelpCard(
                        title = "Same day",
                        description = "The notification is sent on the same day as the event, but you specify the exact time.",
                        icon = Icons.Rounded.CalendarToday
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    HelpCard(
                        title = "1 day before",
                        description = "The notification is sent exactly 24 hours before the event.",
                        icon = Icons.Rounded.Event
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    HelpCard(
                        title = "2 days before",
                        description = "The notification is sent exactly 48 hours before the event.",
                        icon = Icons.Rounded.DateRange
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    HelpCard(
                        title = "1 week before",
                        description = "The notification is sent exactly 7 days before the event.",
                        icon = Icons.Rounded.CalendarViewWeek
                    )
                }
            }
        }
    }
}

@Composable
fun HelpCard(title: String, description: String, icon: ImageVector) {
    FloatingGlassContainer(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            crossAxisAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .background(Accent500.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Accent500, modifier = Modifier.size(24.dp))
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
