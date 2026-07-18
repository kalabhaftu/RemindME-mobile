package com.remindme.app.ui.utils

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun glassmorphismModifier(
    blurRadius: Float = 8f,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f)
): Modifier {
    return Modifier
        .blur(blurRadius.dp)
        .background(backgroundColor)
}

fun Modifier.glassEffect(
    alpha: Float = 0.1f
) = this.background(Color.White.copy(alpha = alpha))

fun Modifier.blurredGlassBackground(
    blurRadius: Float = 8f,
    backgroundColor: Color = Color.White.copy(alpha = 0.05f)
) = this
    .blur(blurRadius.dp)
    .background(backgroundColor)
