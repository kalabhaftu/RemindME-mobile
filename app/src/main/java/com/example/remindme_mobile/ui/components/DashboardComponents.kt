package com.example.remindme_mobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.remindme_mobile.domain.models.CategoryType
import com.example.remindme_mobile.domain.models.Occurrence
import com.example.remindme_mobile.ui.components.liquid.FloatingGlassContainer
import com.example.remindme_mobile.ui.components.liquid.LiquidIcon

import com.example.remindme_mobile.ui.theme.Accent500
import com.example.remindme_mobile.ui.theme.TextPrimary
import com.example.remindme_mobile.ui.theme.TextSecondary
import com.example.remindme_mobile.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    occurrences: List<Occurrence>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    onMonthChange: (Int) -> Unit
) {
    FloatingGlassContainer(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                LiquidIcon(
                    imageVector = Icons.Default.ChevronLeft,
                    tint = TextSecondary,
                    modifier = Modifier.clickable { onMonthChange(-1) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                LiquidIcon(
                    imageVector = Icons.Default.ChevronRight,
                    tint = TextSecondary,
                    modifier = Modifier.clickable { onMonthChange(1) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Generate days of the month
            val daysInMonth = currentMonth.lengthOfMonth()
            val days = (1..daysInMonth).map { currentMonth.withDayOfMonth(it) }
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(days) { date ->
                    val isSelected = date == selectedDate
                    val hasEvents = occurrences.any { it.date == date }
                    
                    FloatingGlassContainer(
                        borderRadius = 12.dp,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onSelectDate(date) },
                        backgroundColor = if (isSelected) Accent500.copy(alpha = 0.25f) else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) Accent500 else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (hasEvents) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp)
                                        .size(4.dp)
                                ) {
                                    // A dot indicator can go here
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingPanel(
    occurrences: List<Occurrence>,
    onMarkDone: (String, LocalDate) -> Unit,
    onSnooze: (String, LocalDate) -> Unit
) {
    FloatingGlassContainer(
        modifier = Modifier.fillMaxWidth(),
        borderRadius = 16.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Upcoming",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (occurrences.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidIcon(Icons.Default.Notifications, tint = TextTertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All caught up", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    occurrences.take(5).forEach { occ ->
                        FloatingGlassContainer(
                            borderRadius = 12.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = occ.item.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = occ.date.format(DateTimeFormatter.ofPattern("MMM d")),
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

