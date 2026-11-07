package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextTertiary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DateTile(
    label: String,
    value: LocalDateTime?,
    placeholder: String,
    formatter: (LocalDateTime) -> String = { it.format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")) },
    onTap: () -> Unit
) {
    val hasValue = value != null
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clip(shape)
            .background(appControlColor())
            .clickable { onTap() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (hasValue) Accent500.copy(alpha = 0.15f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    iconRes = AppIcons.CalendarToday,
                    tint = if (hasValue) Accent500 else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (hasValue) formatter(value!!) else placeholder,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (hasValue) TextPrimary else TextTertiary
                )
            }
            
            AppIcon(
                iconRes = AppIcons.ChevronRight,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
