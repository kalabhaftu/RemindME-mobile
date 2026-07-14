package com.example.remindme_mobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoogleLogo(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeWidth = w * 0.22f
        val rect = Rect(
            left = strokeWidth / 2,
            top = strokeWidth / 2,
            right = w - strokeWidth / 2,
            bottom = h - strokeWidth / 2
        )

        // Red (Top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -45f,
            sweepAngle = -90f,
            useCenter = false,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Yellow (Left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = -135f,
            sweepAngle = -60f,
            useCenter = false,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Green (Bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 165f,
            sweepAngle = -100f,
            useCenter = false,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue (Right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 66f,
            sweepAngle = -66f,
            useCenter = false,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue Horizontal Bar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(w / 2, h / 2 - strokeWidth / 2),
            size = Size(w / 2, strokeWidth)
        )
    }
}
