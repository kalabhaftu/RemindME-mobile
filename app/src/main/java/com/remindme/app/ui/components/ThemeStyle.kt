package com.remindme.app.ui.components

import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeStyle {
    Glass,
    Solid
}

val LocalThemeStyle = staticCompositionLocalOf { ThemeStyle.Glass }
