package com.remindme.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.domain.models.ReminderItem
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class OfflineWriteResult(val queued: Boolean)

private data class PendingOp(
    val id: Long,
    val type: String,
    val reminderId: String,
    val payload: String?
)

private class ReminderDatabase(context: Context) : SQLiteOpenHelper(context, "remindme.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE reminders (id TEXT PRIMARY KEY, user_id TEXT NOT NULL, payload TEXT NOT NULL, updated_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE pending_ops (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id TEXT NOT NULL, reminder_id TEXT NOT NULL, type TEXT NOT NULL, payload TEXT, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX pending_ops_created_at ON pending_ops(created_at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

class OfflineReminderRepository(
    private val remoteRepository: ReminderRepository,
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val database = ReminderDatabase(context.applicationContext)

    private val _cachedReminders = MutableStateFlow<List<ReminderItem>>(emptyList())
    val cachedReminders: Flow<List<ReminderItem>> = _cachedReminders.asStateFlow()

    init {
        currentUserId()?.let { _cachedReminders.value = readCache(it) }
    }

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun currentUserId(): String? = SupabaseManager.client.auth.currentSessionOrNull()?.user?.id

    private fun readCache(userId: String): List<ReminderItem> {
        val result = mutableListOf<ReminderItem>()
        database.readableDatabase.query(
            "reminders",
            arrayOf("payload"),
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "updated_at DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                runCatching { json.decodeFromString<ReminderItem>(cursor.getString(0)) }
                    .onSuccess(result::add)
            }
        }
        return result
    }

    private fun cacheReminder(item: ReminderItem) {
        val values = ContentValues().apply {
            put("id", item.id)
            put("user_id", item.userId)
            put("payload", json.encodeToString(item))
            put("updated_at", System.currentTimeMillis())
        }
        database.writableDatabase.insertWithOnConflict("reminders", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun removeCachedReminder(id: String, userId: String) {
        database.writableDatabase.delete("reminders", "id = ? AND user_id = ?", arrayOf(id, userId))
    }

    private fun readPendingOps(userId: String): List<PendingOp> {
        val result = mutableListOf<PendingOp>()
        database.readableDatabase.query(
            "pending_ops",
            arrayOf("id", "type", "reminder_id", "payload"),
            "user_id = ?",
            arrayOf(userId),
            null,
            null,
            "created_at ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += PendingOp(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
            }
        }
        return result
    }

    private fun enqueue(userId: String, type: String, reminderId: String, item: ReminderItem?) {
        database.writableDatabase.delete(
            "pending_ops",
            "user_id = ? AND reminder_id = ? AND type = ?",
            arrayOf(userId, reminderId, type)
        )
        val values = ContentValues().apply {
            put("user_id", userId)
            put("reminder_id", reminderId)
            put("type", type)
            put("payload", item?.let { json.encodeToString(it) })
            put("created_at", System.currentTimeMillis())
        }
        database.writableDatabase.insert("pending_ops", null, values)
    }

    private fun removePendingOp(id: Long) {
        database.writableDatabase.delete("pending_ops", "id = ?", arrayOf(id.toString()))
    }

    suspend fun getReminders(): List<ReminderItem> = withContext(Dispatchers.IO) {
        val userId = currentUserId() ?: throw IllegalStateException("Not logged in")
        var remoteError: Exception? = null
        try {
            if (isOnline()) {
                val remote = remoteRepository.getReminders()
                database.writableDatabase.delete("reminders", "user_id = ?", arrayOf(userId))
                remote.forEach(::cacheReminder)
                _cachedReminders.value = remote
                return@withContext remote
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "getReminders remote failed", e)
            remoteError = e
        }
        val cached = readCache(userId)
        if (cached.isEmpty() && remoteError != null) {
            throw remoteError
        }
        _cachedReminders.value = cached
        cached
    }

    suspend fun addReminder(item: ReminderItem): OfflineWriteResult = withContext(Dispatchers.IO) {
        val userId = item.userId
        cacheReminder(item)
        _cachedReminders.value = readCache(userId)

        if (isOnline()) {
            try {
                remoteRepository.addReminder(item)
                database.writableDatabase.delete("pending_ops", "user_id = ? AND reminder_id = ?", arrayOf(userId, item.id))
                return@withContext OfflineWriteResult(queued = false)
            } catch (e: Exception) {
                Log.e("OfflineRepo", "addReminder remote failed", e)
                enqueue(userId, "add", item.id, item)
            }
        } else {
            enqueue(userId, "add", item.id, item)
        }
        OfflineWriteResult(queued = true)
    }

    suspend fun updateReminder(item: ReminderItem): OfflineWriteResult = withContext(Dispatchers.IO) {
        val userId = item.userId
        cacheReminder(item)
        _cachedReminders.value = readCache(userId)

        if (isOnline()) {
            try {
                remoteRepository.updateReminder(item)
                database.writableDatabase.delete("pending_ops", "user_id = ? AND reminder_id = ?", arrayOf(userId, item.id))
                return@withContext OfflineWriteResult(queued = false)
            } catch (e: Exception) {
                Log.e("OfflineRepo", "updateReminder remote failed", e)
                enqueue(userId, "update", item.id, item)
            }
        } else {
            enqueue(userId, "update", item.id, item)
        }
        OfflineWriteResult(queued = true)
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        val userId = currentUserId() ?: throw IllegalStateException("Not logged in")
        removeCachedReminder(id, userId)
        _cachedReminders.value = readCache(userId)

        if (isOnline()) {
            try {
                remoteRepository.deleteReminder(id)
                database.writableDatabase.delete("pending_ops", "user_id = ? AND reminder_id = ?", arrayOf(userId, id))
            } catch (e: Exception) {
                Log.e("OfflineRepo", "deleteReminder remote failed", e)
                enqueue(userId, "delete", id, null)
            }
        } else {
            enqueue(userId, "delete", id, null)
        }
    }

    suspend fun getReminder(id: String): ReminderItem? = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val remote = remoteRepository.getReminder(id)
                if (remote != null) {
                    cacheReminder(remote)
                }
                return@withContext remote
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "getReminder remote failed", e)
        }
        val userId = currentUserId() ?: return@withContext null
        readCache(userId).find { it.id == id }
    }

    suspend fun searchReminders(query: String): List<ReminderItem> = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                return@withContext remoteRepository.searchReminders(query)
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "searchReminders remote failed", e)
        }
        val userId = currentUserId() ?: return@withContext emptyList()
        readCache(userId).filter {
            it.name.contains(query, ignoreCase = true) ||
            (it.notes?.contains(query, ignoreCase = true) == true)
        }
    }

    suspend fun markTaskDone(id: String, occurrenceDate: String) = withContext(Dispatchers.IO) {
        if (isOnline()) {
            try { remoteRepository.markTaskDone(id, occurrenceDate) } catch (e: Exception) { Log.e("OfflineRepo", "markTaskDone remote failed", e) }
        }
    }

    suspend fun snoozeReminder(id: String, occurrenceDate: String, hours: Int = 1) = withContext(Dispatchers.IO) {
        if (isOnline()) {
            try { remoteRepository.snoozeReminder(id, occurrenceDate, hours) } catch (e: Exception) { Log.e("OfflineRepo", "snoozeReminder remote failed", e) }
        }
    }

    suspend fun syncPending() = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext
        val userId = currentUserId() ?: return@withContext
        val ops = readPendingOps(userId)
        if (ops.isEmpty()) return@withContext

        for (op in ops) {
            try {
                when (op.type) {
                    "add" -> op.payload?.let { remoteRepository.addReminder(json.decodeFromString<ReminderItem>(it)) }
                    "update" -> op.payload?.let { remoteRepository.updateReminder(json.decodeFromString<ReminderItem>(it)) }
                    "delete" -> remoteRepository.deleteReminder(op.reminderId)
                }
                removePendingOp(op.id)
            } catch (e: Exception) {
                Log.e("OfflineRepo", "syncPending: ${op.type} ${op.reminderId} failed", e)
            }
        }
    }
}
