package com.remindme.app.ui.components.liquid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.AppColors

@Composable
fun LiquidFilterChip(
    label: String,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AppColors.accent500.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) AppColors.accent500.copy(alpha = 0.4f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onSelected)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        FloatingGlassContainer(
            borderRadius = 24.dp,
            padding = 0.dp, // Content handles padding
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) AppColors.accent500 else AppColors.textSecondary
                )
            }
        }
    }
}
