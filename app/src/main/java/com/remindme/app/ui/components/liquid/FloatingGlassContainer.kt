package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun FloatingGlassContainer(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 28.dp,
    padding: Dp = 0.dp,
    tintColor: Color? = null,
    backdrop: Backdrop = LocalBackdrop.current,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassStyle = LocalLiquidGlassStyle.current
    val activeTintColor = tintColor ?: when {
        glassStyle == LiquidGlassStyle.Clear && isDark -> Color.White.copy(alpha = 0.04f)
        glassStyle == LiquidGlassStyle.Clear -> Color.White.copy(alpha = 0.08f)
        isDark -> Color.Black.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(borderRadius) },
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(activeTintColor, blendMode = BlendMode.SrcOver)
                }
            )
            .padding(padding)
    ) {
        content()
    }
}
