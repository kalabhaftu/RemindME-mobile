package com.remindme.app.ui.components.liquid

import android.os.Build
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
    val bgColor = tintColor ?: when {
        glassStyle == LiquidGlassStyle.Clear -> Color.White.copy(alpha = if (isDark) 0.04f else 0.08f)
        isDark -> Color.Black.copy(alpha = 0.18f)
        else -> Color.White.copy(alpha = 0.2f)
    }
    val shape = RoundedCornerShape(borderRadius)
    val blurRadius = if (Build.VERSION.SDK_INT >= 31 && bgColor.alpha > 0f) 16.dp else 0.dp

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(bgColor)
                .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier)
        )
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
