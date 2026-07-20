package com.remindme.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.components.appScrimColor
import com.remindme.app.ui.components.appSurfaceColor
import com.remindme.app.ui.theme.AppColors
import com.composables.icons.lucide.R as LucideR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToAddHoliday: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appSurfaceColor(elevated = true),
        contentColor = AppColors.textPrimary,
        scrimColor = appScrimColor(),
        dragHandle = null
    ) {
        AppCard(
            borderRadius = 32.dp,
            elevated = true,
            modifier = Modifier.fillMaxWidth()
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        color = AppColors.textSecondary.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Add",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickAddOption(
                    modifier = Modifier.weight(1f),
                    label = "Person",
                    iconRes = LucideR.drawable.lucide_ic_user_round,
                    onClick = {
                        onDismiss()
                        onNavigateToAddPerson()
                    }
                )
                QuickAddOption(
                    modifier = Modifier.weight(1f),
                    label = "Subscription",
                    iconRes = LucideR.drawable.lucide_ic_credit_card,
                    onClick = {
                        onDismiss()
                        onNavigateToAddSubscription()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickAddOption(
                    modifier = Modifier.weight(1f),
                    label = "Task",
                    iconRes = LucideR.drawable.lucide_ic_list_todo,
                    onClick = {
                        onDismiss()
                        onNavigateToAddTask()
                    }
                )
                QuickAddOption(
                    modifier = Modifier.weight(1f),
                    label = "Holiday",
                    iconRes = LucideR.drawable.lucide_ic_calendar_days,
                    onClick = {
                        onDismiss()
                        onNavigateToAddHoliday()
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }
}

@Composable
fun QuickAddOption(
    modifier: Modifier = Modifier,
    label: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier.clickable { onClick() },
        borderRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppIcon(iconRes = iconRes, tint = AppColors.accent500, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )
        }
    }
}
