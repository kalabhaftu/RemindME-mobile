package com.remindme.app.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.remindme.app.ui.theme.AppColors
import com.remindme.app.ui.theme.Accent500

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Accent500,
            uncheckedThumbColor = AppColors.textSecondary,
            uncheckedTrackColor = appControlColor(),
            uncheckedBorderColor = appBorderColor(),
            checkedBorderColor = appBorderColor()
        )
    )
}
