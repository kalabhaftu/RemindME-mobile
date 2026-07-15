package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.AppColors

@Composable
fun LiquidSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = AppColors.accent500
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}
