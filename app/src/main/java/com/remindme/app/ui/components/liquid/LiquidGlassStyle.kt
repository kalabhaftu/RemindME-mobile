package com.remindme.app.ui.components.liquid

import androidx.compose.runtime.staticCompositionLocalOf

enum class LiquidGlassStyle {
    Glass,
    Solid
}

val LocalLiquidGlassStyle = staticCompositionLocalOf { LiquidGlassStyle.Glass }
