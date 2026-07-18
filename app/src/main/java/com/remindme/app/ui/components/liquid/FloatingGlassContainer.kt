package com.remindme.app.ui.components.liquid

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

@Composable
fun FloatingGlassContainer(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 28.dp,
    padding: Dp = 0.dp,
    tintColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassStyle = LocalLiquidGlassStyle.current

    val bgColor = if (glassStyle == LiquidGlassStyle.Solid) {
        if (isDark) Color(0xFF1E1E2E) else Color(0xFFF2F2F7)
    } else {
        tintColor ?: if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
    }

    val shape = RoundedCornerShape(borderRadius)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor)
                .then(
                    if (glassStyle == LiquidGlassStyle.Glass) Modifier.blur(8.dp)
                    else Modifier
                )
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
