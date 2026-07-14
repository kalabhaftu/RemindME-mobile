package com.example.remindme_mobile.data.repository

import com.example.remindme_mobile.domain.models.ReminderItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        supabase.postgrest["reminder_items"]
            .delete {
                filter { eq("id", id) }
            }
    }
}
