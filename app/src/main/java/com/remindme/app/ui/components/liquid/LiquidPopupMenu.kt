package com.remindme.app.ui.components.liquid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.remindme.app.ui.theme.AppColors

@Composable
fun LiquidPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 4.dp),
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // A raw Popup draws nothing of its own -- no forced Surface, shape,
    // tonal/shadow elevation, or min-width like Material3's DropdownMenu.
    // FloatingGlassContainer is the only visual chrome, matching the rest
    // of the liquid glass system instead of a Material dropdown with the
    // fill color merely swapped for transparent.
    if (expanded) {
        Popup(
            offset = androidx.compose.ui.unit.IntOffset(
                with(androidx.compose.ui.platform.LocalDensity.current) { offset.x.roundToPx() },
                with(androidx.compose.ui.platform.LocalDensity.current) { offset.y.roundToPx() }
            ),
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.92f),
                exit = fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.92f)
            ) {
                FloatingGlassContainer(
                    borderRadius = 16.dp,
                    modifier = modifier.width(180.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidPopupMenuItem(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) AppColors.stateDanger else AppColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) AppColors.stateDanger else AppColors.textPrimary
        )
    }
}
