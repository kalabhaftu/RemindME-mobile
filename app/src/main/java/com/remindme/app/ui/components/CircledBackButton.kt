package com.remindme.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
    AppCard(
        borderRadius = 50.dp,
        modifier = modifier
            .size(44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                imageVector = Icons.Outlined.ArrowBack,
                color = AppColors.textPrimary,
                size = 20.dp
            )
        }
    }
}
