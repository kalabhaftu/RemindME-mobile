package com.remindme.app.ui.components.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.remindme.app.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidDateTimePickerDialog(
    initialDate: LocalDateTime?,
    onDismissRequest: () -> Unit,
    onDateTimeSelected: (LocalDateTime) -> Unit
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

    if (!showTimePicker) {
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Next", color = AppColors.accent500)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancel", color = AppColors.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = AppColors.bgSurface1)
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
            containerColor = AppColors.bgSurface1,
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
