package com.remindme.app.ui.components.liquid

import androidx.compose.runtime.staticCompositionLocalOf
import com.kyant.backdrop.Backdrop

val LocalBackdrop = staticCompositionLocalOf<Backdrop> {
    error("No Backdrop provided")
}
