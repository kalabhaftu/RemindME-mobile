package com.remindme.app.utils

import com.remindme.app.domain.models.CategoryType
import com.remindme.app.domain.models.OccurrenceStatus
import com.remindme.app.domain.models.ReminderItem
import com.remindme.app.domain.models.ReminderOccurrence
import java.time.LocalDate

object OccurrenceCalculator {

    private fun getStatusForDate(
        date: LocalDate,
        today: LocalDate,
        item: ReminderItem
    ): OccurrenceStatus {
        val dateStr = date.toString()

        // Check escalation state
        val state = item.escalationState?.firstOrNull { it.occurrenceDate == dateStr }
        if (state != null && state.markedDoneAt != null) {
            return OccurrenceStatus.COMPLETED_PAST
        }

        return when {
            date.isEqual(today) -> OccurrenceStatus.TODAY
            date.isBefore(today) -> OccurrenceStatus.MISSED_PAST
            else -> OccurrenceStatus.UPCOMING
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun generateOccurrences(
        items: List<ReminderItem>,
        startDate: LocalDate,
        endDate: LocalDate,
        today: LocalDate = LocalDate.now()
    ): List<ReminderOccurrence> {
        val occurrences = mutableListOf<ReminderOccurrence>()

        for (item in items) {
            val currentDates = mutableListOf<LocalDate>()

            when (item.category) {
                CategoryType.PERSON -> {
                    val bdStr = item.person?.birthdate?.trim()
                    if (!bdStr.isNullOrEmpty()) {
                        val bd = LocalDate.parse(bdStr)
                        var curr = LocalDate.of(startDate.year, bd.monthValue, bd.dayOfMonth)

                        if (curr.isBefore(startDate)) {
                            curr = curr.plusYears(1)
                        }

                        while (!curr.isAfter(endDate)) {
                            var occurrenceDate = curr
                            if (bd.monthValue == 2 && bd.dayOfMonth == 29 && !isLeapYear(occurrenceDate.year)) {
                                occurrenceDate = LocalDate.of(occurrenceDate.year, 2, 28)
                            }
                            if (!occurrenceDate.isBefore(startDate) && !occurrenceDate.isAfter(endDate)) {
                                currentDates.add(occurrenceDate)
                            }
                            curr = curr.plusYears(1)
                        }
                    }
                }
                CategoryType.SUBSCRIPTION -> {
                    val rdStr = item.subscription?.renewalDate?.trim()
                    if (!rdStr.isNullOrEmpty()) {
                        val rd = LocalDate.parse(rdStr)
                        val cycle = item.subscription?.cycle ?: "monthly"
                        var curr = rd

                        while (curr.isBefore(startDate)) {
                            curr = when (cycle) {
                                "weekly" -> curr.plusWeeks(1)
                                "monthly" -> curr.plusMonths(1)
                                "yearly" -> curr.plusYears(1)
                                else -> curr.plusDays(1)
                            }
                        }

                        while (!curr.isAfter(endDate)) {
                            currentDates.add(curr)
                            curr = when (cycle) {
                                "weekly" -> curr.plusWeeks(1)
                                "monthly" -> curr.plusMonths(1)
                                "yearly" -> curr.plusYears(1)
                                else -> curr.plusDays(1)
                            }
                        }
                    }
                }
                CategoryType.CUSTOM_HOLIDAY -> {
                    val hdStr = item.holiday?.holidayDate?.trim()
                    if (!hdStr.isNullOrEmpty()) {
                        val hd = LocalDate.parse(hdStr)
                        var curr = LocalDate.of(startDate.year, hd.monthValue, hd.dayOfMonth)
                        if (curr.isBefore(startDate)) {
                            curr = curr.plusYears(1)
                        }
                        while (!curr.isAfter(endDate)) {
                            if (!curr.isBefore(startDate)) currentDates.add(curr)
                            curr = curr.plusYears(1)
                        }
                    }
                }
                CategoryType.TASK -> {
                    val dueStr = item.task?.dueAt?.trim()
                    if (!dueStr.isNullOrEmpty()) {
                        val due = LocalDate.parse(dueStr.substring(0, 10))
                        val rr = item.recurrence

                        if (rr == null || rr.frequency == "none") {
                            if (!due.isBefore(startDate) && !due.isAfter(endDate)) {
                                currentDates.add(due)
                            }
                        } else {
                            var curr = due
                            val freq = rr.frequency
                            val interval = rr.intervalCount.toLong()
                            var count = 0

                            val ends = rr.ends
                            val endsValue = rr.endsValue

                            while (curr.isBefore(startDate)) {
                                if (ends == "after_occurrences" && endsValue != null && count >= endsValue.toInt()) break
                                if (ends == "on_date" && endsValue != null && curr.isAfter(LocalDate.parse(endsValue))) break

                                curr = when (freq) {
                                    "daily" -> curr.plusDays(interval)
                                    "weekly" -> curr.plusWeeks(interval)
                                    "monthly" -> curr.plusMonths(interval)
                                    "yearly" -> curr.plusYears(interval)
                                    else -> curr
                                }
                                count++
                            }

                            while (!curr.isAfter(endDate)) {
                                if (ends == "after_occurrences" && endsValue != null && count >= endsValue.toInt()) break
                                if (ends == "on_date" && endsValue != null && curr.isAfter(LocalDate.parse(endsValue))) break

                                currentDates.add(curr)

                                curr = when (freq) {
                                    "daily" -> curr.plusDays(interval)
                                    "weekly" -> curr.plusWeeks(interval)
                                    "monthly" -> curr.plusMonths(interval)
                                    "yearly" -> curr.plusYears(interval)
                                    else -> curr
                                }
                                count++
                            }
                        }
                    }
                }
            }

            for (d in currentDates) {
                occurrences.add(
                    ReminderOccurrence(
                        date = d,
                        item = item,
                        status = getStatusForDate(d, today, item)
                    )
                )
            }
        }

        return occurrences.sortedBy { it.date }
    }
}
