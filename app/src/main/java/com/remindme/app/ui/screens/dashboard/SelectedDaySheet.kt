package com.remindme.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.domain.models.OccurrenceStatus
import com.remindme.app.domain.models.ReminderOccurrence
import com.remindme.app.ui.components.LocalThemeStyle
import com.remindme.app.ui.components.ThemeStyle
import com.remindme.app.ui.theme.AppColors
import com.remindme.app.ui.theme.BgSurface2
import com.remindme.app.ui.theme.BgElevated
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FormatListBulleted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedDaySheet(
    date: LocalDate,
    occurrences: List<ReminderOccurrence>,
    onMarkDone: (String, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val glassStyle = LocalThemeStyle.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (glassStyle == ThemeStyle.Glass) AppColors.bgSurface2 else AppColors.bgElevated,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.textSecondary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AppColors.textSecondary)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            if (occurrences.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = AppColors.textTertiary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No reminders on this day.", color = AppColors.textTertiary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(occurrences) { occ ->
                        val item = occ.item
                        val isDone = occ.status == OccurrenceStatus.COMPLETED_PAST
                        
                        AppCard(
                            borderRadius = 16.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppCard(
                                    borderRadius = 10.dp,
                                    modifier = Modifier.padding(9.dp)
                                ) {
                                    AppIcon(
                                        imageVector = when (item.category) {
                                            CategoryType.TASK -> Icons.Default.FormatListBulleted
                                            CategoryType.PERSON -> Icons.Default.Person
                                            CategoryType.SUBSCRIPTION -> Icons.Default.CreditCard
                                            CategoryType.CUSTOM_HOLIDAY -> Icons.Default.Event
                                        },
                                        tint = if (isDone) AppColors.textTertiary else AppColors.accent500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDone) AppColors.textTertiary else AppColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = when (item.category) {
                                            CategoryType.PERSON -> if (item.person?.birthdate != null) "Birthday" else "Relationship"
                                            CategoryType.SUBSCRIPTION -> "Subscription"
                                            else -> "Task"
                                        },
                                        fontSize = 12.sp,
                                        color = AppColors.textSecondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                if (item.category == CategoryType.TASK && !isDone) {
                                    IconButton(onClick = { onMarkDone(item.id, occ.date) }) {
                                        AppIcon(imageVector = Icons.Default.CheckCircle, tint = AppColors.stateSuccess, modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
