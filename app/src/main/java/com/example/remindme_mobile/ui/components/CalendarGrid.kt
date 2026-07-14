package com.example.remindme_mobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.remindme_mobile.domain.models.ReminderOccurrence
import com.example.remindme_mobile.ui.components.liquid.FloatingGlassContainer
import com.example.remindme_mobile.ui.theme.AppColors
import java.time.LocalDate

@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    occurrences: List<ReminderOccurrence>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    onMonthChange: (LocalDate) -> Unit
) {
    FloatingGlassContainer(
        borderRadius = 24.dp,
        padding = 16.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "CalendarGrid (Not yet implemented)",
                color = AppColors.textSecondary
            )
        }
    }
}
