package com.remindme.app.data.repository

import com.remindme.app.domain.models.ReminderItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonPrimitive
import java.time.Instant

@Serializable
data class EscalationStateUpsert(
    val reminder_item_id: String,
    val occurrence_date: String,
    val marked_done_at: String
)

class ReminderRepository(private val supabase: SupabaseClient) {

    private val selectQuery = """
        *,
        person_details (*),
        subscription_details (*),
        task_details (*),
        holiday_details (*),
        recurrence_rules (*),
        notification_preferences (*),
        escalation_state (*)
    """.trimIndent()

    suspend fun getReminders(): List<ReminderItem> = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"]
            .select(columns = Columns.raw(selectQuery)) {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ReminderItem>()
    }

    suspend fun getReminder(id: String): ReminderItem? = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"]
            .select(columns = Columns.raw(selectQuery)) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<ReminderItem>()
    }

    suspend fun searchReminders(query: String): List<ReminderItem> = withContext(Dispatchers.IO) {
        val session = supabase.auth.currentSessionOrNull()
        val userId = session?.user?.id ?: return@withContext emptyList()
        
        supabase.postgrest["reminder_items"]
            .select(columns = Columns.raw(selectQuery)) {
                filter {
                    eq("user_id", userId)
                    or {
                        ilike("name", "%$query%")
                        ilike("notes", "%$query%")
                    }
                }
                order("updated_at", Order.DESCENDING)
                limit(20)
            }
            .decodeList<ReminderItem>()
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"]
            .delete {
                filter { eq("id", id) }
            }
    }

    suspend fun markTaskDone(id: String, occurrenceDate: String) = withContext(Dispatchers.IO) {
        val payload = EscalationStateUpsert(
            reminder_item_id = id,
            occurrence_date = occurrenceDate,
            marked_done_at = Instant.now().toString()
        )
        supabase.postgrest["escalation_state"].upsert(payload)
    }

    suspend fun snoozeReminder(id: String, occurrenceDate: String, hours: Int = 1) = withContext(Dispatchers.IO) {
        val snoozedUntil = java.time.LocalDateTime.now().plusHours(hours.toLong()).toString() + "Z"
        val payload = buildJsonObject {
            put("reminder_item_id", id)
            put("occurrence_date", occurrenceDate)
            put("snoozed_until", snoozedUntil)
        }
        supabase.postgrest["snooze_state"].upsert(payload)
    }

    private suspend fun insertOrUpsertDetails(item: ReminderItem, isUpdate: Boolean) {
        val personTable = supabase.postgrest["person_details"]
        val subscriptionTable = supabase.postgrest["subscription_details"]
        val taskTable = supabase.postgrest["task_details"]
        val holidayTable = supabase.postgrest["holiday_details"]
        val recurrenceTable = supabase.postgrest["recurrence_rules"]

        item.person?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.birthdate?.let { put("birthdate", it) }
                it.gender?.let { put("gender", it) }
                it.relationship?.let { put("relationship", it) }
                it.customRelationship?.let { put("custom_relationship", it) }
                it.avatarUrl?.let { put("avatar_url", it) }
            }
            if (isUpdate) personTable.upsert(payload) else personTable.insert(payload)
        }

        item.subscription?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.logoUrl?.let { put("logo_url", it) }
                it.logoDomain?.let { put("logo_domain", it) }
                it.billingAmount?.let { put("billing_amount", it) }
                it.billingCurrency?.let { put("billing_currency", it) }
                it.renewalDate?.let { put("renewal_date", it) }
                it.cycle?.let { put("cycle", it) }
            }
            if (isUpdate) subscriptionTable.upsert(payload) else subscriptionTable.insert(payload)
        }

        item.task?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.dueAt?.let { put("due_at", it) }
            }
            if (isUpdate) taskTable.upsert(payload) else taskTable.insert(payload)
        }

        item.holiday?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                put("country_code", it.countryCode)
                put("holiday_key", it.holidayKey)
                put("holiday_date", it.holidayDate)
                it.isCustom?.let { put("is_custom", it) }
            }
            if (isUpdate) holidayTable.upsert(payload) else holidayTable.insert(payload)
        }

        item.recurrence?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                put("frequency", it.frequency)
                put("interval_count", it.intervalCount)
                put("ends", it.ends)
                it.endsValue?.let { put("ends_value", it) }
                // Previously omitted -- see RecurrenceRules.nextOccurrenceAt.
                it.nextOccurrenceAt?.let { put("next_occurrence_at", it) }
            }
            if (isUpdate) recurrenceTable.upsert(payload) else recurrenceTable.insert(payload)
        }
    }

    suspend fun addReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"].insert(item)
        insertOrUpsertDetails(item, isUpdate = false)
    }

    suspend fun updateReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"].update(item) {
            filter { eq("id", item.id) }
        }
        insertOrUpsertDetails(item, isUpdate = true)
    }
}
