package com.example.remindme_mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Accent500,
    onPrimary = Color.White,
    primaryContainer = Accent600,
    onPrimaryContainer = Color.White,
    surface = BgElevated,
    onSurface = TextPrimary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = BgCanvas,
    surfaceContainerLow = BgElevated,
    surfaceContainer = BgElevated,
    surfaceContainerHigh = BgElevated,
    surfaceContainerHighest = Color(0xFF252836),
    error = StateDanger,
    onError = Color.White,
    background = BgCanvas,
    onBackground = TextPrimary
)

@Composable
fun RemindmeMobileTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
