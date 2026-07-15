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
    val countryCode: String,
    @SerialName("holiday_key")
    val holidayKey: String,
    @SerialName("holiday_date")
    val holidayDate: String,
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
    val endsValue: String? = null
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
    
    // Details using list representations matching Postgrest array outputs
    @SerialName("person_details")
    val personDetails: List<PersonDetails>? = null,
    @SerialName("subscription_details")
    val subscriptionDetails: List<SubscriptionDetails>? = null,
    @SerialName("task_details")
    val taskDetails: List<TaskDetails>? = null,
    @SerialName("holiday_details")
    val holidayDetails: List<HolidayDetails>? = null,
    @SerialName("recurrence_rules")
    val recurrenceRules: List<RecurrenceRules>? = null,
    @SerialName("escalation_state")
    val escalationState: List<EscalationState>? = null
) {
    val person: PersonDetails? get() = personDetails?.firstOrNull()
    val subscription: SubscriptionDetails? get() = subscriptionDetails?.firstOrNull()
    val task: TaskDetails? get() = taskDetails?.firstOrNull()
    val holiday: HolidayDetails? get() = holidayDetails?.firstOrNull()
    val recurrence: RecurrenceRules? get() = recurrenceRules?.firstOrNull()
}
