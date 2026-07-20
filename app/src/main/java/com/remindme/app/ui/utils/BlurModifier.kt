package com.remindme.app.ui.utils

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.components.appControlColor
import androidx.compose.runtime.Composable

@Composable
fun themedBackground(
    blurRadius: Float = 8f,
    backgroundColor: Color? = null
): Modifier {
    return Modifier
        .blur(blurRadius.dp)
        .background(backgroundColor ?: appControlColor())
}

@Composable
fun Modifier.surfaceTint() = this.background(appControlColor())

@Composable
fun Modifier.blurredBackground(
    blurRadius: Float = 8f,
    backgroundColor: Color? = null
) = this
    .blur(blurRadius.dp)
    .background(backgroundColor ?: appControlColor())
