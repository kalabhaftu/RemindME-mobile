package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    appBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    glassStyle: ThemeStyle = LocalThemeStyle.current,
    content: @Composable (PaddingValues) -> Unit
) {
    val gradientColors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460))

    var appBarHeightPx by remember { mutableStateOf(0) }
    var bottomBarHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    CompositionLocalProvider(
        LocalThemeStyle provides glassStyle
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // Gradient background
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

            // Content — padded by real measured heights
            val topPadding = if (appBar != null) {
                if (appBarHeightPx > 0) with(density) { appBarHeightPx.toDp() } else 90.dp
            } else 0.dp
            val bottomPadding = if (bottomBar != null) {
                if (bottomBarHeightPx > 0) with(density) { bottomBarHeightPx.toDp() + 24.dp } else 90.dp
            } else 0.dp
            content(PaddingValues(top = topPadding, bottom = bottomPadding))

            // AppBar overlay (measured)
            if (appBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .onSizeChanged { appBarHeightPx = it.height }
                ) {
                    appBar()
                }
            }

            // BottomBar overlay (measured)
            if (bottomBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .onSizeChanged { bottomBarHeightPx = it.height }
                ) {
                    bottomBar()
                }
            }

            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)) {
                snackbarHost()
            }
        }
    }
}
