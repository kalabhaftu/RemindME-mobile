package com.remindme.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    AppCard(
        borderRadius = 16.dp,
        modifier = Modifier.fillMaxWidth().clickable { onTap() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCard(
                tintColor = Color.Transparent
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    AppIcon(
                        imageVector = Icons.Outlined.CalendarToday,
                        tint = if (hasValue) Accent500 else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                imageVector = Icons.Outlined.ChevronRight,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
