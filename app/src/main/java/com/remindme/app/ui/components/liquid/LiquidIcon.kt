package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Unspecified,
    tint: Color = Color.Unspecified
) {
    val resolvedTint = if (tint != Color.Unspecified) tint else color
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.size(size),
        tint = resolvedTint
    )
}
