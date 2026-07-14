package com.example.remindme_mobile.domain.models

import java.time.LocalDateTime
import java.time.LocalDate

enum class CategoryType {
    PERSON,
    SUBSCRIPTION,
    TASK,
    CUSTOM_HOLIDAY
}

data class ReminderItem(
    val id: String,
    val userId: String,
    val category: CategoryType,
    val name: String,
    val iconKey: String? = null,
    val colorAccent: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    
    // Details (Using maps for flexibility right now to mimic JSON dynamic nature, but normally we'd make sealed classes)
    val personDetails: Map<String, Any>? = null,
    val subscriptionDetails: Map<String, Any>? = null,
    val taskDetails: Map<String, Any>? = null,
    val holidayDetails: Map<String, Any>? = null,
    val recurrenceRules: Map<String, Any>? = null,
    val escalationState: List<Map<String, Any>>? = null
)
