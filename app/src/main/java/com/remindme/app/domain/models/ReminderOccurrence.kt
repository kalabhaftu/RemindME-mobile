package com.remindme.app.domain.models

import java.time.LocalDate

enum class OccurrenceStatus {
    UPCOMING,
    TODAY,
    COMPLETED_PAST,
    MISSED_PAST
}

data class ReminderOccurrence(
    val date: LocalDate,
    val item: ReminderItem,
    val status: OccurrenceStatus
)
