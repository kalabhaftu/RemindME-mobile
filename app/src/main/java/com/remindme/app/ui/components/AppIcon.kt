package com.remindme.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.DrawableRes
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Unspecified,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    val resolvedTint = if (tint != Color.Unspecified) tint else color
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = resolvedTint
    )
}

@Composable
fun AppIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Unspecified,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    val resolvedTint = if (tint != Color.Unspecified) tint else color
    androidx.compose.material3.Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = resolvedTint
    )
}
