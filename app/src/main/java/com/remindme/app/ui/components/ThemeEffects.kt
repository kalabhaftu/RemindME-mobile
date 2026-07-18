package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.surfaceEffect(
    blurRadius: Float = 6f,
    cornerRadius: Float = 12f
): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.25f)
    }
    
    return this
        .background(backgroundColor)
}

@Composable
fun Modifier.buttonEffect(): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.Black.copy(alpha = 0.3f)
    }
    
    return this
        .background(backgroundColor)
}

@Composable
fun Modifier.surfaceEffect(): Modifier {
    val isLight = !isSystemInDarkTheme()
    val backgroundColor = if (isLight) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.Black.copy(alpha = 0.15f)
    }
    
    return this
        .background(backgroundColor)
}
