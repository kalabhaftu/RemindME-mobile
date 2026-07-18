package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LiquidScaffold(
    modifier: Modifier = Modifier,
    backdrop: LayerBackdrop = Unit,
    appBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    glassStyle: LiquidGlassStyle = LocalLiquidGlassStyle.current,
    content: @Composable (PaddingValues) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))
    } else {
        listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3))
    }

    CompositionLocalProvider(
        LocalBackdrop provides backdrop,
        LocalLiquidGlassStyle provides glassStyle
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset.Zero,
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            )

            val topPadding = if (appBar != null) 90.dp else 0.dp
            val bottomPadding = if (bottomBar != null) 90.dp else 0.dp
            content(PaddingValues(top = topPadding, bottom = bottomPadding))

            if (appBar != null) {
                Box(modifier = Modifier.align(Alignment.TopCenter)) {
                    appBar()
                }
            }

            if (bottomBar != null) {
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) {
                    bottomBar()
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)) {
                snackbarHost()
            }
        }
    }
}
