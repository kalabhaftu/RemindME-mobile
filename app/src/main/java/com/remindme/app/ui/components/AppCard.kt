package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.BgElevated

/**
 * @param elevated  When true, uses a higher-opacity glass background for popup/sheet surfaces
 *                  (SelectedDaySheet, QuickAddSheet, FAB). Standard cards stay subtle.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 28.dp,
    padding: Dp = 0.dp,
    tintColor: Color? = null,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassStyle = LocalThemeStyle.current

    val bgColor = when {
        tintColor != null -> tintColor
        glassStyle == ThemeStyle.Solid -> {
            if (isDark) BgElevated else Color(0xFFF2F2F7)
        }
        elevated -> {
            // Popup/sheet surfaces need strong enough glass to be readable
            if (isDark) Color(0xFF1E2340).copy(alpha = 0.82f)
            else Color.White.copy(alpha = 0.72f)
        }
        else -> {
            // Standard card — subtle tint that lets gradient show
            if (isDark) Color.White.copy(alpha = 0.10f)
            else Color.Black.copy(alpha = 0.07f)
        }
    }

    val shape = RoundedCornerShape(borderRadius)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor)
                .then(
                    if (glassStyle == ThemeStyle.Glass) Modifier.blur(8.dp)
                    else Modifier
                )
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
