package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.ui.theme.AppColors

data class LiquidPopupMenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun LiquidPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<LiquidPopupMenuItem>
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(AppColors.bgCanvas)
            .clip(RoundedCornerShape(16.dp))
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismissRequest()
                        item.onClick()
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.icon != null) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (item.isDestructive) AppColors.stateDanger else AppColors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (item.isDestructive) AppColors.stateDanger else AppColors.textPrimary
                )
            }
        }
    }
}
