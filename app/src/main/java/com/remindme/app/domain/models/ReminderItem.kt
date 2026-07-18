package com.remindme.app.domain.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val str = decoder.decodeString()
        val cleanStr = if (str.endsWith("Z")) str.substring(0, str.length - 1) else str
        val normalized = cleanStr.substringBefore("+")
        return LocalDateTime.parse(normalized, formatter)
    }
}

@Serializable
enum class CategoryType {
    @SerialName("person")
    PERSON,
    @SerialName("subscription")
    SUBSCRIPTION,
    @SerialName("task")
    TASK,
    @SerialName("custom_holiday")
    CUSTOM_HOLIDAY
}

@Serializable
data class PersonDetails(
    val birthdate: String? = null,
    val relationship: String? = null,
    @SerialName("custom_relationship")
    val customRelationship: String? = null,
    val gender: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)

@Serializable
data class SubscriptionDetails(
    @SerialName("logo_url")
    val logoUrl: String? = null,
    @SerialName("logo_domain")
    val logoDomain: String? = null,
    @SerialName("billing_amount")
    val billingAmount: Double? = null,
    @SerialName("billing_currency")
    val billingCurrency: String? = null,
    @SerialName("renewal_date")
    val renewalDate: String? = null,
    val cycle: String? = null
)

@Serializable
data class TaskDetails(
    @SerialName("due_at")
    val dueAt: String? = null
)

@Serializable
data class HolidayDetails(
    @SerialName("country_code")
    val countryCode: String = "US",
    @SerialName("holiday_key")
    val holidayKey: String? = null,
    @SerialName("holiday_date")
    val holidayDate: String? = null,
    @SerialName("is_custom")
    val isCustom: Boolean? = null
)

@Serializable
data class RecurrenceRules(
    val frequency: String,
    @SerialName("interval_count")
    val intervalCount: Int = 1,
    val ends: String,
    @SerialName("ends_value")
    val endsValue: String? = null,
    // This was missing entirely from the model. next_occurrence_at is the
    // column the Supabase cron dispatcher (reminder_occurrences_due) reads
    // to decide what's due -- without it, nothing ever gets written here,
    // so nothing was ever found as "due" for ANY reminder category.
    @SerialName("next_occurrence_at")
    val nextOccurrenceAt: String? = null
)

@Serializable
data class EscalationState(
    @SerialName("occurrence_date")
    val occurrenceDate: String,
    @SerialName("first_notified_at")
    val firstNotifiedAt: String? = null,
    @SerialName("marked_done_at")
    val markedDoneAt: String? = null,
    @SerialName("nudge_sent_at")
    val nudgeSentAt: String? = null
)

@Serializable
data class NotificationPreference(
    @SerialName("reminder_item_id")
    val reminderItemId: String? = null,
    val channel: String,
    val enabled: Boolean = true,
    @SerialName("lead_time")
    val leadTime: String = "morning_of",
    @SerialName("custom_time")
    val customTime: String = "09:00",
    @SerialName("offset_days")
    val offsetDays: Int = 0
)

@Serializable
data class ReminderItem(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val category: CategoryType,
    val name: String,
    @SerialName("icon_key")
    val iconKey: String? = null,
    @SerialName("color_accent")
    val colorAccent: String? = null,
    val notes: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("created_at")
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("updated_at")
    val updatedAt: LocalDateTime,
    
    @SerialName("person_details")
    val personDetails: PersonDetails? = null,
    @SerialName("subscription_details")
    val subscriptionDetails: SubscriptionDetails? = null,
    @SerialName("task_details")
    val taskDetails: TaskDetails? = null,
    @SerialName("holiday_details")
    val holidayDetails: HolidayDetails? = null,
    @SerialName("recurrence_rules")
    val recurrenceRules: RecurrenceRules? = null,
    @SerialName("escalation_state")
    val escalationState: List<EscalationState>? = null,
    @SerialName("notification_preferences")
    val notificationPreferences: List<NotificationPreference>? = null
) {
    val person: PersonDetails? get() = personDetails
    val subscription: SubscriptionDetails? get() = subscriptionDetails
    val task: TaskDetails? get() = taskDetails
    val holiday: HolidayDetails? get() = holidayDetails
    val recurrence: RecurrenceRules? get() = recurrenceRules
}
