package com.remindme.app.ui.components.liquid

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.remindme.app.ui.theme.AppColors

@Composable
fun LiquidSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = AppColors.accent500,
            uncheckedThumbColor = AppColors.textSecondary,
            uncheckedTrackColor = AppColors.bgSurface1,
            uncheckedBorderColor = Color.Transparent
        )
    )
}
