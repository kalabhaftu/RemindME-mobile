package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.remindme.app.ui.theme.AppColors

@Composable
fun CircledBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingGlassContainer(
        borderRadius = 50.dp,
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LiquidIcon(
                imageVector = Icons.Rounded.ArrowBack,
                color = AppColors.textPrimary,
                size = 20.dp
            )
        }
    }
}
