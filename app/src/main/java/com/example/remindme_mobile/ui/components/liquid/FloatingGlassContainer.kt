package com.example.remindme_mobile.ui.components.liquid

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import com.kyant.shapes.Squircle

@Composable
fun FloatingGlassContainer(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 28.dp,
    padding: Dp = 0.dp,
    backdrop: Backdrop = LocalBackdrop.current,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) {
        Color.Black.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Squircle(borderRadius.value / 28f) }, // Approximate curvature based on standard 28dp
                effects = {
                    vibrancy()
                    blur(16f.dp.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(tintColor, blendMode = BlendMode.SrcOver)
                }
            )
            .padding(padding)
    ) {
        content()
    }
}
