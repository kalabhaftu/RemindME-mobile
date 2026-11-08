package com.remindme.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.remindme.app.ui.theme.Accent500
import com.remindme.app.ui.theme.StateDanger
import com.remindme.app.ui.theme.TextPrimary
import com.remindme.app.ui.theme.TextSecondary

@Composable
fun BoxScope.AppRetrySnackbar(
    error: String?,
    onRetry: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    bottomPadding: androidx.compose.ui.unit.Dp = 104.dp
) {
    if (error.isNullOrBlank()) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = bottomPadding, start = 16.dp, end = 16.dp)
            .zIndex(25f)
    ) {
        AppCard(
            borderRadius = 16.dp,
            padding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StateDanger.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    iconRes = AppIcons.Error,
                    color = StateDanger,
                    size = 20.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = error,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onRetry,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Retry",
                        color = Accent500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        AppIcon(
                            iconRes = AppIcons.Close,
                            color = TextSecondary,
                            size = 16.dp
                        )
                    }
                }
            }
        }
    }
}
