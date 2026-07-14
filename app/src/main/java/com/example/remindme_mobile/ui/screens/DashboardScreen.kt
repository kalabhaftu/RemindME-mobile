package com.example.remindme_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PostAdd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme_mobile.ui.components.liquid.FloatingGlassContainer
import com.example.remindme_mobile.ui.components.liquid.LiquidIcon
import com.example.remindme_mobile.ui.theme.AppColors
import com.example.remindme_mobile.ui.components.CalendarGrid
import com.example.remindme_mobile.ui.components.UpcomingPanel
import java.time.LocalDate

@Composable
fun DashboardScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 140.dp, bottom = 120.dp, start = 16.dp, end = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(label = "People", count = 0, modifier = Modifier.weight(1f))
            StatCard(label = "Subscriptions", count = 0, modifier = Modifier.weight(1f))
            StatCard(label = "Tasks", count = 0, modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAddTile("Person", Icons.Rounded.PersonAdd, modifier = Modifier.weight(1f)) { }
                QuickAddTile("Subscription", Icons.Rounded.AddCard, modifier = Modifier.weight(1f)) { }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAddTile("Task", Icons.Rounded.PostAdd, modifier = Modifier.weight(1f)) { }
                QuickAddTile("Holiday", Icons.Rounded.Event, modifier = Modifier.weight(1f)) { }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // CalendarGrid
        CalendarGrid(
            currentMonth = LocalDate.now(),
            occurrences = emptyList(),
            selectedDate = null,
            onSelectDate = {},
            onMonthChange = {}
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        // UpcomingPanel
        UpcomingPanel(
            occurrences = emptyList(),
            onMarkDone = { _, _ -> },
            onSnooze = { _, _ -> }
        )
    }
}

@Composable
fun StatCard(label: String, count: Int, modifier: Modifier = Modifier) {
    FloatingGlassContainer(
        borderRadius = 16.dp,
        padding = 14.dp,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.accent500,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
fun QuickAddTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingGlassContainer(
        borderRadius = 16.dp,
        padding = 14.dp,
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiquidIcon(imageVector = icon, size = 20.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.textPrimary
            )
        }
    }
}
