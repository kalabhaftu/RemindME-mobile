package com.remindme.app.utils

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object OccurrenceScheduler {

    private fun atNoon(date: LocalDateTime): LocalDateTime {
        return date.with(LocalTime.NOON)
    }

    private fun startOfDay(date: LocalDateTime): LocalDateTime {
        return date.with(LocalTime.MIDNIGHT)
    }

    fun nextBirthday(birthdateStr: String): LocalDateTime {
        val birthdate = LocalDateTime.parse(birthdateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val today = startOfDay(LocalDateTime.now())
        var candidate = LocalDateTime.of(today.year, birthdate.month, birthdate.dayOfMonth, 0, 0)
        
        if (candidate.isBefore(today)) {
            candidate = LocalDateTime.of(today.year + 1, birthdate.month, birthdate.dayOfMonth, 0, 0)
        }
        return atNoon(candidate)
    }

    fun nextRenewal(renewalDateStr: String, cycle: String): LocalDateTime {
        val renewal = LocalDateTime.parse(renewalDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val today = startOfDay(LocalDateTime.now())
        var candidate = atNoon(renewal)
        
        while (candidate.isBefore(today)) {
            candidate = when (cycle) {
                "weekly" -> candidate.plusDays(7)
                "yearly" -> candidate.plusYears(1)
                else -> candidate.plusMonths(1) // "monthly" is default
            }
        }
        return candidate
    }

    fun nextHoliday(holidayDateStr: String): LocalDateTime {
        val holiday = LocalDateTime.parse(holidayDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val today = startOfDay(LocalDateTime.now())
        var candidate = LocalDateTime.of(today.year, holiday.month, holiday.dayOfMonth, 0, 0)
        
        if (candidate.isBefore(today)) {
            candidate = LocalDateTime.of(today.year + 1, holiday.month, holiday.dayOfMonth, 0, 0)
        }
        return atNoon(candidate)
    }

    fun computeInitialNextOccurrence(
        category: String,
        birthdate: String? = null,
        renewalDate: String? = null,
        cycle: String? = null,
        dueAt: String? = null,
        holidayDate: String? = null
    ): String? {
        val nextDate = when (category) {
            "person" -> birthdate?.let { nextBirthday(it) }
            "subscription" -> renewalDate?.let { nextRenewal(it, cycle ?: "monthly") }
            "task" -> dueAt?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
            "custom_holiday" -> holidayDate?.let { nextHoliday(it) }
            else -> null
        }
        return nextDate?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}
