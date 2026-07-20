package com.remindme.app.ui.theme

import androidx.compose.ui.graphics.Color

val BgCanvas = Color(0xFF06070A)
val BgSurface1 = Color(0x08FFFFFF)
val BgSurface2 = Color(0xCC18263B)
val BgSurface3 = Color(0x19FFFFFF)
val BgElevated = Color(0xFF1E2029)

val Accent500 = Color(0xFF3B82F6)
val Accent400 = Color(0xFF5B9CFF)
val Accent600 = Color(0xFF2563EB)

val TextPrimary = Color(0xEBFFFFFF)
val TextSecondary = Color(0x99FFFFFF)
val TextTertiary = Color(0x61FFFFFF)

val StateSuccess = Color(0xFF34D399)
val StateWarning = Color(0xFFF59E0B)
val StateDanger = Color(0xFFEF4444)
val BorderSubtle = Color(0x14FFFFFF)

// Backward compatibility alias — all screens can use AppColors.xxx
object AppColors {
    val bgCanvas get() = BgCanvas
    val bgSurface1 get() = BgSurface1
    val bgSurface2 get() = BgSurface2
    val bgSurface3 get() = BgSurface3
    val bgElevated get() = BgElevated
    val accent500 get() = Accent500
    val accent400 get() = Accent400
    val accent600 get() = Accent600
    val textPrimary get() = com.remindme.app.ui.theme.TextPrimary
    val textSecondary get() = com.remindme.app.ui.theme.TextSecondary
    val textTertiary get() = com.remindme.app.ui.theme.TextTertiary
    val stateSuccess get() = com.remindme.app.ui.theme.StateSuccess
    val stateWarning get() = com.remindme.app.ui.theme.StateWarning
    val stateDanger get() = com.remindme.app.ui.theme.StateDanger
    val glassBorder get() = com.remindme.app.ui.theme.BorderSubtle
}
