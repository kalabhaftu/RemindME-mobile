package com.remindme.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Color.Unspecified
) {
    AppButton(
        onClick = onClick,
        modifier = modifier,
        tint = Color.Transparent
    ) {
        AppIcon(
            imageVector = imageVector,
            size = size,
            color = color
        )
    }
}
