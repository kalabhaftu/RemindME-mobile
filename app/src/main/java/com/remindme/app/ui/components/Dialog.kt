package com.remindme.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.remindme.app.ui.theme.AppColors

@Composable
fun AppDialog(
    title: String,
    content: @Composable () -> Unit,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
        ) {
            AppCard(
                borderRadius = 24.dp,
                padding = 24.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        content()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            tint = Color.Transparent
                        ) {
                            Text(cancelText, color = AppColors.textSecondary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        AppButton(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            tint = if (isDestructive) AppColors.stateDanger else AppColors.accent500
                        ) {
                            Text(confirmText, color = AppColors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}
