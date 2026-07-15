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

    suspend fun addReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        // Insert main item
        supabase.postgrest["reminder_items"].insert(item)

        // Insert details depending on category
        item.personDetails?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.forEach { (k, v) -> put(k, v.toString()) }
            }
            supabase.postgrest["person_details"].insert(payload)
        }
        item.subscriptionDetails?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.forEach { (k, v) -> put(k, v.toString()) }
            }
            supabase.postgrest["subscription_details"].insert(payload)
        }
        item.taskDetails?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.forEach { (k, v) -> put(k, v.toString()) }
            }
            supabase.postgrest["task_details"].insert(payload)
        }
        item.holidayDetails?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.forEach { (k, v) -> put(k, v.toString()) }
            }
            supabase.postgrest["holiday_details"].insert(payload)
        }
        item.recurrenceRules?.let {
            val payload = buildJsonObject {
                put("reminder_item_id", item.id)
                it.forEach { (k, v) -> put(k, v.toString()) }
            }
            supabase.postgrest["recurrence_rules"].insert(payload)
        }
    }
}
