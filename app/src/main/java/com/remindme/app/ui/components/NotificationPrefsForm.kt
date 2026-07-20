package com.remindme.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import com.remindme.app.ui.components.BottomSheetPickerItem
import com.remindme.app.ui.components.PickerField
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.BorderSubtle
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextTertiary

data class ChannelPref(
    val enabled: Boolean = true,
    val leadTime: String = "morning_of",
    val customTime: String = "",
    val offsetDays: Int = 0
)

private fun formatTime12h(time: String): String {
    val parts = time.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: return formatTime12h(LocalTime.now())
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return formatTime12h(hour, minute)
}

private fun formatTime12h(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return buildString {
        append(displayHour)
        append(":")
        append(String.format("%02d", minute))
        append(" ")
        append(amPm)
    }
}

private fun formatTime12h(time: LocalTime): String = formatTime12h(time.hour, time.minute)

val CHANNELS = listOf("email", "push", "telegram", "in_app")

val LEAD_TIME_OPTIONS = mapOf(
    "at_time" to "At time of event",
    "morning_of" to "Morning of",
    "noon_of" to "Noon of",
    "evening_of" to "Evening of",
    "custom" to "Custom time"
)

val OFFSET_DAY_OPTIONS = mapOf(
    0 to "On the day",
    1 to "1 day before",
    3 to "3 days before",
    7 to "1 week before",
    14 to "2 weeks before"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPrefsForm(
    matrix: Map<String, ChannelPref>,
    onChanged: (Map<String, ChannelPref>) -> Unit
) {
    Column {
        CHANNELS.forEach { channel ->
            val pref = matrix[channel] ?: ChannelPref()
            
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                borderRadius = 16.dp,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = matrix.toMutableMap()
                                next[channel] = pref.copy(enabled = !pref.enabled)
                                onChanged(next)
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        AppCard(
                            borderRadius = 8.dp,
                            tintColor = Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                AppIcon(
                                    iconRes = getChannelIcon(channel),
                                    tint = Accent500,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = getChannelLabel(channel),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        AppSwitch(
                            checked = pref.enabled,
                            onCheckedChange = {
                                val next = matrix.toMutableMap()
                                next[channel] = pref.copy(enabled = it)
                                onChanged(next)
                            }
                        )
                    }
                    
                    if (pref.enabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                PickerField(
                                    label = "When",
                                    value = pref.leadTime,
                                    displayValue = { LEAD_TIME_OPTIONS[it] ?: it },
                                    title = "Select timing",
                                    items = LEAD_TIME_OPTIONS.map { BottomSheetPickerItem(it.key, it.value) },
                                    onChanged = {
                                        val next = matrix.toMutableMap()
                                        val offsetDays = if (it == "at_time") 0 else pref.offsetDays
                                        next[channel] = pref.copy(leadTime = it, offsetDays = offsetDays)
                                        onChanged(next)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (pref.leadTime != "at_time" && pref.leadTime != "custom") {
                                    PickerField(
                                        label = "Advance notice",
                                        value = pref.offsetDays.toString(),
                                        displayValue = { OFFSET_DAY_OPTIONS[it.toIntOrNull() ?: 0] ?: "$it days" },
                                        title = "Select advance notice",
                                        items = OFFSET_DAY_OPTIONS.map { BottomSheetPickerItem(it.key.toString(), it.value) },
                                        onChanged = { selected ->
                                            val next = matrix.toMutableMap()
                                            next[channel] = pref.copy(offsetDays = selected.toIntOrNull() ?: 0)
                                            onChanged(next)
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (pref.leadTime == "custom") {
                            var showTimePicker by remember { mutableStateOf(false) }
                            val now = LocalTime.now()
                            val timePickerState = rememberTimePickerState(
                                initialHour = pref.customTime.substringBefore(":").toIntOrNull() ?: now.hour,
                                initialMinute = pref.customTime.substringAfter(":").toIntOrNull() ?: now.minute,
                                is24Hour = false
                            )

                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true },
                                borderRadius = 12.dp,
                                tintColor = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppCard(
                                        borderRadius = 8.dp,
                                        tintColor = Color.Transparent
                                    ) {
                                        Box(modifier = Modifier.padding(7.dp)) {
                                            AppIcon(
                                                iconRes = PremiumIcons.AccessTime,
                                                tint = Accent500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Time: ${formatTime12h(pref.customTime)}",
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AppIcon(
                                        iconRes = PremiumIcons.ChevronRight,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (showTimePicker) {
                                AlertDialog(
                                    onDismissRequest = { showTimePicker = false },
                                    containerColor = appSurfaceColor(elevated = true),
                                    titleContentColor = com.remindme.app.ui.theme.TextPrimary,
                                    textContentColor = com.remindme.app.ui.theme.TextSecondary,
                                    title = { Text("Select custom time", color = com.remindme.app.ui.theme.TextPrimary) },
                                    text = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            TimePicker(state = timePickerState)
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            val time = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                            val next = matrix.toMutableMap()
                                            next[channel] = pref.copy(customTime = time)
                                            onChanged(next)
                                            showTimePicker = false
                                        }) {
                                            Text("Done", color = com.remindme.app.ui.theme.Accent500)
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showTimePicker = false }) {
                                            Text("Cancel", color = com.remindme.app.ui.theme.TextSecondary)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getChannelIcon(channel: String) = when(channel) {
    "email" -> PremiumIcons.Email
    "push" -> PremiumIcons.Notifications
    "telegram" -> PremiumIcons.Send
    "in_app" -> PremiumIcons.Smartphone
    else -> PremiumIcons.Notifications
}

private fun getChannelLabel(channel: String) = when(channel) {
    "email" -> "Email"
    "push" -> "Push Notification"
    "telegram" -> "Telegram"
    "in_app" -> "In-App"
    else -> channel.uppercase()
}
