package com.remindme.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class OccurrenceSchedulerTest {
    @Test
    fun taskOccurrencePreservesUtcInstant() {
        val dueAt = "2030-04-05T09:30:00Z"

        assertEquals(
            dueAt,
            OccurrenceScheduler.computeInitialNextOccurrence(category = "task", dueAt = dueAt)
        )
    }

    @Test
    fun dateOnlyBirthdayProducesAnOccurrence() {
        val occurrence = OccurrenceScheduler.computeInitialNextOccurrence(
            category = "person",
            birthdate = "2030-04-05"
        )

        assertEquals(true, occurrence?.endsWith("Z"))
    }
}
