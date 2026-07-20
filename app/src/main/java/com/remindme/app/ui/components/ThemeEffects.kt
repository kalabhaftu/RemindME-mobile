package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.surfaceEffect(
    blurRadius: Float = 6f,
    cornerRadius: Float = 12f
): Modifier {
    return this
        .blur(blurRadius.dp)
        .background(appSurfaceColor())
}

@Composable
fun Modifier.buttonEffect(): Modifier {
    return this
        .background(appControlColor())
}
