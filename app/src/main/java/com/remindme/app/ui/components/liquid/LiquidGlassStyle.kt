package com.remindme.app.ui.components.liquid

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

enum class LiquidGlassStyle {
    Frosted,
    Clear
}

val LocalLiquidGlassStyle = staticCompositionLocalOf { LiquidGlassStyle.Frosted }
