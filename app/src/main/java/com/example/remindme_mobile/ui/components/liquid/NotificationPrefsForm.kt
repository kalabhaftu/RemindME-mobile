package com.example.remindme_mobile.ui.components.liquid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import com.example.remindme_mobile.ui.theme.Accent500
import com.example.remindme_mobile.ui.theme.GlassBorder
import com.example.remindme_mobile.ui.theme.TextPrimary
import com.example.remindme_mobile.ui.theme.TextTertiary

data class ChannelPref(
    val enabled: Boolean = false,
    val leadTime: String = "morning_of",
    val customTime: String = "09:00",
    val offsetDays: Int = 0
)

val CHANNELS = listOf("email", "push", "telegram", "in_app")

val LEAD_TIME_OPTIONS = mapOf(
    "at_time" to "At time of event",
    "morning_of" to "Morning of",
    "evening_before" to "Evening before",
    "custom" to "Custom time"
)

val OFFSET_DAY_OPTIONS = mapOf(
    0 to "On the day",
    1 to "1 day before",
    2 to "2 days before",
    3 to "3 days before",
    7 to "1 week before"
)

@Composable
fun NotificationPrefsForm(
    matrix: Map<String, ChannelPref>,
    onChanged: (Map<String, ChannelPref>) -> Unit
) {
    Column {
        CHANNELS.forEach { channel ->
            val pref = matrix[channel] ?: ChannelPref()
            
            FloatingGlassContainer(
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
                        FloatingGlassContainer(
                            borderRadius = 8.dp,
                            backgroundColor = Color.Transparent
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                LiquidIcon(
                                    imageVector = getChannelIcon(channel),
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
                        
                        LiquidSwitch(
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
                                        next[channel] = pref.copy(leadTime = it)
                                        onChanged(next)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                PickerField(
                                    label = "Advance notice",
                                    value = pref.offsetDays,
                                    displayValue = { OFFSET_DAY_OPTIONS[it] ?: "$it days" },
                                    title = "Select advance notice",
                                    items = OFFSET_DAY_OPTIONS.map { BottomSheetPickerItem(it.key, it.value) },
                                    onChanged = {
                                        val next = matrix.toMutableMap()
                                        next[channel] = pref.copy(offsetDays = it)
                                        onChanged(next)
                                    }
                                )
                            }
                        }
                        
                        if (pref.leadTime == "custom") {
                            // Custom Time Picker Trigger
                            // For simplicity, we just use a clickable row here. 
                            // Real app would open a TimePickerDialog.
                            FloatingGlassContainer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { /* TODO open Time Picker */ },
                                borderRadius = 12.dp,
                                backgroundColor = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FloatingGlassContainer(
                                        borderRadius = 8.dp,
                                        backgroundColor = Color.Transparent
                                    ) {
                                        Box(modifier = Modifier.padding(7.dp)) {
                                            LiquidIcon(
                                                imageVector = Icons.Rounded.AccessTime,
                                                tint = Accent500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Time: ${pref.customTime}",
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    LiquidIcon(
                                        imageVector = Icons.Default.ChevronRight,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getChannelIcon(channel: String) = when(channel) {
    "email" -> Icons.Rounded.Email
    "push" -> Icons.Rounded.Notifications
    "telegram" -> Icons.Rounded.Send
    "in_app" -> Icons.Rounded.Smartphone
    else -> Icons.Rounded.Notifications
}

private fun getChannelLabel(channel: String) = when(channel) {
    "email" -> "Email"
    "push" -> "Push Notification"
    "telegram" -> "Telegram"
    "in_app" -> "In-App"
    else -> channel.uppercase()
}

@Composable
fun LiquidSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val align by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "switch"
    )

    Box(
        modifier = Modifier
            .width(44.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(if (checked) Accent500 else GlassBorder)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = 18.dp * align)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
