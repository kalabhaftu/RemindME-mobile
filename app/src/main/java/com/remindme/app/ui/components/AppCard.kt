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
 *                  (SelectedDaySheet, QuickAddSheet, FAB, PopupMenu). Standard cards stay subtle.
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
            // Popup/sheet/FAB surfaces: use the app's background base color at near-full opacity
            // so they look solid and match the app theme without being see-through
            if (isDark) Color(0xFF1A1A2E).copy(alpha = 0.96f)
            else Color(0xFFE0EAFC).copy(alpha = 0.94f)
        }
        else -> {
            // Standard card: semi-transparent frosted tint over the gradient background
            // High enough to be readable, low enough to show gradient depth
            if (isDark) Color.White.copy(alpha = 0.22f)
            else Color.White.copy(alpha = 0.55f)
        }
    }

    val shape = RoundedCornerShape(borderRadius)

    // Note: Do NOT use Modifier.blur() here — it blurs the entire layer including content
    // (text, icons), making everything unreadable. The frosted-glass look comes from the
    // semi-transparent background color alone, which lets the gradient behind show through.
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor)
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
