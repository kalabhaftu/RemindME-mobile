package com.remindme.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.domain.models.OccurrenceStatus
import com.remindme.app.domain.models.ReminderOccurrence
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.theme.AppColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

enum class CalendarViewType { Month, Week, Agenda }

@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    occurrences: List<ReminderOccurrence>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    onMonthChange: (LocalDate) -> Unit
) {
    var viewType by remember { mutableStateOf(CalendarViewType.Month) }

    val occurrencesByDate = remember(occurrences) {
        occurrences.groupBy { it.date }
    }

    fun navigate(dir: Int) {
        if (viewType == CalendarViewType.Week) {
            onMonthChange(currentMonth.plusDays((dir * 7).toLong()))
        } else {
            onMonthChange(currentMonth.plusMonths(dir.toLong()))
        }
    }

    fun headerLabel(): String {
        return if (viewType == CalendarViewType.Week) {
            val ws = currentMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            val we = ws.plusDays(6)
            "${ws.format(DateTimeFormatter.ofPattern("MMM d"))} - ${we.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
        } else {
            currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        }
    }

    AppCard(
        borderRadius = 24.dp,
        padding = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerLabel(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.bgSurface1)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ViewChip("Month", viewType == CalendarViewType.Month) { viewType = CalendarViewType.Month }
                    ViewChip("Week", viewType == CalendarViewType.Week) { viewType = CalendarViewType.Week }
                    ViewChip("Agenda", viewType == CalendarViewType.Agenda) { viewType = CalendarViewType.Agenda }
                }

                IconButton(onClick = { navigate(-1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.KeyboardArrowLeft, contentDescription = "Prev", tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { navigate(1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = "Next", tint = AppColors.textSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = viewType,
                transitionSpec = { fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(90)) },
                label = "calendar_view_anim"
            ) { targetView ->
                when (targetView) {
                    CalendarViewType.Month -> MonthView(currentMonth, occurrencesByDate, selectedDate, onSelectDate)
                    CalendarViewType.Week -> WeekView(currentMonth, occurrencesByDate, selectedDate, onSelectDate)
                    CalendarViewType.Agenda -> AgendaView(occurrencesByDate, onSelectDate)
                }
            }
        }
    }
}

@Composable
private fun ViewChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) AppColors.accent500 else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else AppColors.textTertiary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun MonthView(
    currentMonth: LocalDate,
    occurrencesByDate: Map<LocalDate, List<ReminderOccurrence>>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit
) {
    val yearMonth = YearMonth.from(currentMonth)
    val startOfMonth = currentMonth.withDayOfMonth(1)
    val startDayOfWeek = startOfMonth.dayOfWeek.value % 7 // Sunday = 0
    
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = ((daysInMonth + startDayOfWeek) / 7 + 1) * 7
    val days = (0 until totalCells).map { startOfMonth.minusDays(startDayOfWeek.toLong()).plusDays(it.toLong()) }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { d ->
                Text(d, fontSize = 12.sp, color = AppColors.textTertiary)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            items(days) { day ->
                DayCell(
                    day = day,
                    isCurrentMonth = day.month == currentMonth.month,
                    occurrences = occurrencesByDate[day] ?: emptyList(),
                    isSelected = day == selectedDate,
                    compact = false,
                    showWeekday = false,
                    onClick = { onSelectDate(day) }
                )
            }
        }
    }
}

@Composable
private fun WeekView(
    currentMonth: LocalDate,
    occurrencesByDate: Map<LocalDate, List<ReminderOccurrence>>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit
) {
    val ws = currentMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val days = (0..6).map { ws.plusDays(it.toLong()) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.heightIn(max = 120.dp)
    ) {
        items(days) { day ->
            DayCell(
                day = day,
                isCurrentMonth = true,
                occurrences = occurrencesByDate[day] ?: emptyList(),
                isSelected = day == selectedDate,
                compact = true,
                showWeekday = true,
                onClick = { onSelectDate(day) }
            )
        }
    }
}

@Composable
private fun AgendaView(
    occurrencesByDate: Map<LocalDate, List<ReminderOccurrence>>,
    onSelectDate: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    
    val daysWithOccurrences = remember(today, occurrencesByDate) {
        (0..13).map { today.plusDays(it.toLong()) }
            .associateWith { day -> occurrencesByDate[day] ?: emptyList() }
            .filterValues { it.isNotEmpty() }
    }

    if (daysWithOccurrences.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No events in the next 2 weeks.", color = AppColors.textTertiary, fontSize = 13.sp)
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        daysWithOccurrences.forEach { (day, occs) ->
            val isToday = day == today
            val dateLabel = if (isToday) "Today" else day.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDate(day) }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = dateLabel,
                    fontSize = 12.sp,
                    color = AppColors.textTertiary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                occs.forEach { occ ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(hexToColor(occ.item.colorAccent))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = occ.item.name,
                            fontSize = 13.sp,
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun DayCell(
    day: LocalDate,
    isCurrentMonth: Boolean,
    occurrences: List<ReminderOccurrence>,
    isSelected: Boolean,
    compact: Boolean,
    showWeekday: Boolean,
    onClick: () -> Unit
) {
    val isToday = day == LocalDate.now()

    AppCard(
        borderRadius = 12.dp,
        padding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(if (compact) 0.65f else 1.0f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() }
                .background(
                    when {
                        isSelected -> AppColors.accent500.copy(alpha = 0.25f)
                        isToday -> AppColors.accent500.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }
                )
                .padding(4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (showWeekday) {
                    Text(
                        text = day.format(DateTimeFormatter.ofPattern("EEE")),
                        fontSize = 10.sp,
                        color = AppColors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = day.dayOfMonth.toString(),
                    fontSize = 13.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isSelected -> AppColors.accent500
                        isToday -> AppColors.accent500.copy(alpha = 0.8f)
                        isCurrentMonth || compact -> AppColors.textPrimary
                        else -> AppColors.textTertiary
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (occurrences.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        occurrences.take(3).forEach { occ ->
                            if (occ.status == OccurrenceStatus.COMPLETED_PAST) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = AppColors.stateSuccess.copy(alpha = 0.5f),
                                    modifier = Modifier.size(8.dp)
                                )
                            } else if (occ.status == OccurrenceStatus.MISSED_PAST) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 1.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.stateWarning.copy(alpha = 0.5f))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 1.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(hexToColor(occ.item.colorAccent))
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

private val colorCache = mutableMapOf<String, Color>()

private fun hexToColor(hex: String?): Color {
    if (hex == null) return AppColors.accent500
    return colorCache.getOrPut(hex) {
        val colorString = if (hex.length == 7) "#FF" + hex.substring(1) else hex
        try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            AppColors.accent500
        }
    }
}
