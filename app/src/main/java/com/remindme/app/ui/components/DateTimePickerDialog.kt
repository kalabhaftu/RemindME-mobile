package com.remindme.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.remindme.app.ui.theme.AppColors
import com.remindme.app.ui.theme.BgElevated
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    initialDate: LocalDateTime?,
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit,
    dateOnly: Boolean = false
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = initialDate?.hour ?: 9,
        initialMinute = initialDate?.minute ?: 0
    )

    val glassStyle = LocalThemeStyle.current
    val isDark = isSystemInDarkTheme()
    val dialogBgColor = when {
        glassStyle == ThemeStyle.Solid -> {
            if (isDark) BgElevated else Color(0xFFF2F2F7)
        }
        else -> {
            if (isDark) Color(0xFF1A1A2E).copy(alpha = 0.95f)
            else Color(0xFFE0EAFC).copy(alpha = 0.92f)
        }
    }

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = {
                    if (dateOnly) {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                            val dateTime = date.atTime(12, 0)
                            onDateTimeSelected(dateTime)
                        }
                    } else {
                        showTimePicker = true
                    }
                }) {
                    Text(if (dateOnly) "Confirm" else "Next", color = AppColors.accent500)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel", color = AppColors.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = dialogBgColor)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = AppColors.textPrimary,
                    headlineContentColor = AppColors.textPrimary,
                    weekdayContentColor = AppColors.textSecondary,
                    subheadContentColor = AppColors.textPrimary,
                    yearContentColor = AppColors.textPrimary,
                    currentYearContentColor = AppColors.textPrimary,
                    selectedYearContentColor = AppColors.textPrimary,
                    selectedYearContainerColor = AppColors.accent500,
                    dayContentColor = AppColors.textPrimary,
                    disabledDayContentColor = AppColors.textTertiary,
                    selectedDayContentColor = AppColors.textPrimary,
                    selectedDayContainerColor = AppColors.accent500,
                    todayContentColor = AppColors.accent500,
                    todayDateBorderColor = AppColors.accent500
                )
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                        val dateTime = date.atTime(timePickerState.hour, timePickerState.minute)
                        onDateTimeSelected(dateTime)
                    }
                }) {
                    Text("Confirm", color = AppColors.accent500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Back", color = AppColors.textSecondary)
                }
            },
            containerColor = dialogBgColor,
            title = { Text("Select time", color = AppColors.textPrimary) },
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = AppColors.bgSurface2,
                            selectorColor = AppColors.accent500,
                            containerColor = AppColors.bgSurface2,
                            periodSelectorBorderColor = AppColors.accent500,
                            periodSelectorSelectedContainerColor = AppColors.accent500,
                            periodSelectorUnselectedContainerColor = AppColors.bgSurface2,
                            periodSelectorSelectedContentColor = AppColors.textPrimary,
                            periodSelectorUnselectedContentColor = AppColors.textSecondary,
                            timeSelectorSelectedContainerColor = AppColors.accent500,
                            timeSelectorUnselectedContainerColor = AppColors.bgSurface2,
                            timeSelectorSelectedContentColor = AppColors.textPrimary,
                            timeSelectorUnselectedContentColor = AppColors.textPrimary
                        )
                    )
                }
            }
        )
    }
}
