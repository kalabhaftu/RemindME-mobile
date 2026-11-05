package com.remindme.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.OccurrenceStatus
import com.remindme.app.domain.models.ReminderOccurrence
import com.remindme.app.ui.components.AppCard
import com.remindme.app.ui.components.FilterChip
import com.remindme.app.ui.components.AppIcon
import com.remindme.app.ui.theme.AppColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class UpcomingFilter { ThreeDays, SevenDays, Month, All }

@Composable
fun UpcomingPanel(
    occurrences: List<ReminderOccurrence>,
    onMarkDone: (String, LocalDate) -> Unit,
    onSnooze: (String, LocalDate) -> Unit,
    onPreview: (com.remindme.app.domain.models.ReminderItem) -> Unit,
    onEdit: (com.remindme.app.domain.models.ReminderItem) -> Unit
) {
    var filter by remember { mutableStateOf(UpcomingFilter.SevenDays) }

    val filtered = remember(occurrences, filter) {
        val today = LocalDate.now()
        val days = when (filter) {
            UpcomingFilter.ThreeDays -> 3L
            UpcomingFilter.SevenDays -> 7L
            UpcomingFilter.Month -> 30L
            UpcomingFilter.All -> 365L
        }
        val end = today.plusDays(days)

        occurrences.filter {
            it.status != OccurrenceStatus.COMPLETED_PAST && !it.date.isAfter(end)
        }.sortedBy { it.date }
    }

    AppCard(
        borderRadius = 24.dp,
        padding = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Upcoming",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip("Next 3 Days", filter == UpcomingFilter.ThreeDays, onSelected = { filter = UpcomingFilter.ThreeDays })
                FilterChip("Next 7 Days", filter == UpcomingFilter.SevenDays, onSelected = { filter = UpcomingFilter.SevenDays })
                FilterChip("This Month", filter == UpcomingFilter.Month, onSelected = { filter = UpcomingFilter.Month })
                FilterChip("All Upcoming", filter == UpcomingFilter.All, onSelected = { filter = UpcomingFilter.All })
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AppIcon(iconRes = AppIcons.Notifications, size = 32.dp, color = AppColors.textTertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All caught up", color = AppColors.textSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtered.forEach { occ ->
                        UpcomingItem(
                            occurrence = occ,
                            onMarkDone = { onMarkDone(occ.item.id, occ.date) },
                            onSnooze = { onSnooze(occ.item.id, occ.date) },
                            onPreview = { onPreview(occ.item) },
                            onEdit = { onEdit(occ.item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingItem(
    occurrence: ReminderOccurrence,
    onMarkDone: () -> Unit,
    onSnooze: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit
) {
    val item = occurrence.item
    val isDone = occurrence.status == OccurrenceStatus.COMPLETED_PAST

    val iconRes = when (item.category) {
        CategoryType.TASK -> AppIcons.Checklist
        CategoryType.PERSON -> AppIcons.Person
        CategoryType.SUBSCRIPTION -> AppIcons.CreditCard
        CategoryType.CUSTOM_HOLIDAY -> AppIcons.Cake
    }

    AppCard(
        borderRadius = 16.dp,
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPreview() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCard(
                    borderRadius = 10.dp,
                    padding = 9.dp
                ) {
                    AppIcon(
                        iconRes = iconRes,
                        size = 18.dp,
                        tint = if (isDone) AppColors.textTertiary else AppColors.accent500
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) AppColors.textTertiary else AppColors.textPrimary,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        formatRelativeDate(occurrence.date).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        getSubtitle(occurrence),
                        fontSize = 12.sp,
                        color = AppColors.textSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.category == CategoryType.TASK && !isDone) {
                    AppCard(
                        borderRadius = 20.dp,
                        padding = 8.dp,
                        modifier = Modifier.clickable { onSnooze() }
                    ) {
                        AppIcon(iconRes = AppIcons.Snooze, size = 18.dp, color = AppColors.stateWarning)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    AppCard(
                        borderRadius = 20.dp,
                        padding = 8.dp,
                        modifier = Modifier.clickable { onMarkDone() }
                    ) {
                        AppIcon(iconRes = AppIcons.CheckCircle, size = 18.dp, color = AppColors.stateSuccess)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                if (item.category != CategoryType.CUSTOM_HOLIDAY) {
                    AppCard(
                        borderRadius = 20.dp,
                        padding = 8.dp,
                        modifier = Modifier.clickable { onEdit() }
                    ) {
                        AppIcon(iconRes = AppIcons.Edit, size = 18.dp, color = AppColors.textTertiary)
                    }
                }
            }
        }
    }
}

fun formatRelativeDate(date: LocalDate): String {
    val today = LocalDate.now()
    val diff = ChronoUnit.DAYS.between(today, date)
    return when {
        diff == 0L -> "Today"
        diff == 1L -> "Tomorrow"
        diff > 0L -> "In $diff days"
        else -> "${kotlin.math.abs(diff)} days ago"
    }
}

fun getSubtitle(occurrence: ReminderOccurrence): String {
    val item = occurrence.item
    return when (item.category) {
        CategoryType.PERSON -> {
            val bdStr = item.person?.birthdate
            if (bdStr != null) {
                val age = occurrence.date.year - LocalDate.parse(bdStr.substringBefore("T")).year
                "Turns $age"
            } else "Birthday"
        }
        CategoryType.SUBSCRIPTION -> {
            val amount = item.subscription?.billingAmount
            val curr = item.subscription?.billingCurrency ?: "USD"
            val cycle = item.subscription?.cycle ?: "monthly"
            if (amount != null) "$curr $amount / $cycle" else "Subscription renewal"
        }
        else -> "Task"
    }
}
