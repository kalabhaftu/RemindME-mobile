package com.remindme.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.BgElevated
import com.remindme.app.ui.theme.BorderSubtle
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary
import com.remindme.app.ui.theme.TextTertiary

enum class ThemeStyle {
    Glass,
    Solid
}

val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Glass }

@Composable
fun appSurfaceColor(elevated: Boolean = false): Color = when (LocalThemeStyle.current) {
    ThemeStyle.Glass -> if (elevated) Color(0xF21A2A43) else Color(0xCC18263B)
    ThemeStyle.Solid -> if (elevated) Color(0xFF252D40) else BgElevated
}

@Composable
fun appControlColor(): Color = when (LocalThemeStyle.current) {
    ThemeStyle.Glass -> Color(0xB8243550)
    ThemeStyle.Solid -> Color(0xFF252D40)
}

@Composable
fun appBorderColor(): Color = when (LocalThemeStyle.current) {
    ThemeStyle.Glass -> Color(0x3DFFFFFF)
    ThemeStyle.Solid -> BorderSubtle
}

@Composable
fun appScrimColor(): Color = Color(0x9906070A)

object AppTextColors {
    val primary = TextPrimary
    val secondary = TextSecondary
    val tertiary = TextTertiary
    val accent = Accent500
}
