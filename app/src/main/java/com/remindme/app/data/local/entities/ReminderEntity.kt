package com.remindme.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val name: String,
    val notes: String?,
    val iconKey: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long?,
    val isDeleted: Boolean = false,
    val requiresSync: Boolean = true
)

@Entity(tableName = "person_details")
data class PersonDetailsEntity(
    @PrimaryKey val reminderId: String,
    val name: String,
    val relationship: String?,
    val dateOfBirth: String,
    val notes: String?
)

@Entity(tableName = "task_details")
data class TaskDetailsEntity(
    @PrimaryKey val reminderId: String,
    val dueAt: String
)

@Entity(tableName = "subscription_details")
data class SubscriptionDetailsEntity(
    @PrimaryKey val reminderId: String,
    val cost: Double?,
    val renewalDate: String?,
    val renewalCycle: String?,
    val logoUrl: String?
)

@Entity(tableName = "recurrence_rules")
data class RecurrenceRulesEntity(
    @PrimaryKey val reminderId: String,
    val frequency: String,
    val intervalCount: Int = 1,
    val ends: String = "never",
    val endsValue: String?,
    val nextOccurrenceAt: String?
)

@Entity(tableName = "notification_preferences")
data class NotificationPreferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: String,
    val channel: String,
    val enabled: Boolean = true,
    val leadTime: String = "morning_of",
    val customTime: String = "09:00",
    val offsetDays: Int = 0
)
