package com.example.remindme_mobile.data.repository

import com.example.remindme_mobile.domain.models.ReminderItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.serialization.Serializable
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
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
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
        val user = io.github.jan.supabase.gotrue.auth.currentUserOrNull(supabase)
        if (user == null) return@withContext emptyList()
        
        supabase.postgrest["reminder_items"]
            .select(columns = Columns.raw(selectQuery)) {
                filter {
                    eq("user_id", user.id)
                    or {
                        ilike("name", "%$query%")
                        ilike("notes", "%$query%")
                    }
                }
                order("updated_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
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
        supabase.postgrest["escalation_state"].upsert(
            value = payload,
            onConflict = "reminder_item_id, occurrence_date"
        )
    }
}
