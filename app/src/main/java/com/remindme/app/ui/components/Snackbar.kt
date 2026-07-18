package com.remindme.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.AppColors
import com.remindme.app.ui.theme.StateDanger

@Composable
fun Snackbar(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isError: Boolean = false,
    
) {
    AppCard(
        borderRadius = 20.dp,
        padding = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null || isError) {
                val displayIcon = icon ?: if (isError) Icons.Rounded.Error else Icons.Rounded.CheckCircle
                val iconColor = if (isError) StateDanger else AppColors.accent500
                AppIcon(
                    imageVector = displayIcon,
                    color = iconColor,
                    size = 24.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SnackbarHost(
    hostState: androidx.compose.material3.SnackbarHostState,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            Snackbar(
                message = data.visuals.message,
                isError = data.visuals.message.lowercase().contains("error") || 
                         data.visuals.message.lowercase().contains("failed")
            )
        }
    )
}
