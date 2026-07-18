package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlassEffect(
    blurRadius: Float = 6f,
    cornerRadius: Float = 12f
): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.Black.copy(alpha = 0.12f)
    }
    
    return this
        .blur(blurRadius.dp)
        .background(backgroundColor)
}

fun Modifier.liquidButtonEffect(): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Black.copy(alpha = 0.15f)
    }
    
    return this
        .blur(2.dp)
        .background(backgroundColor)
}

fun Modifier.liquidSurfaceEffect(): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }
    
    return this
        .blur(8.dp)
        .background(backgroundColor)
}
