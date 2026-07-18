package com.remindme.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.remindme.app.domain.models.ReminderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

private data class PendingOp(
    val type: String,
    val reminderId: String,
    val timestamp: Long = System.currentTimeMillis()
)

class OfflineReminderRepository(
    private val remoteRepository: ReminderRepository,
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val cacheDir = File(context.filesDir, "cache").also { it.mkdirs() }
    private val remindersFile = File(cacheDir, "reminders.json")
    private val pendingFile = File(cacheDir, "pending_ops.json")

    private val _cachedReminders = MutableStateFlow<List<ReminderItem>>(emptyList())
    val cachedReminders: Flow<List<ReminderItem>> = _cachedReminders.asStateFlow()

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun readCache(): List<ReminderItem> {
        if (!remindersFile.exists()) return emptyList()
        return try {
            json.decodeFromString<List<ReminderItem>>(remindersFile.readText())
        } catch (e: Exception) {
            Log.e("OfflineRepo", "readCache: failed to decode cache", e)
            remindersFile.delete()
            emptyList()
        }
    }

    private fun writeCache(items: List<ReminderItem>) {
        remindersFile.writeText(json.encodeToString(items))
    }

    private fun readPendingOps(): MutableList<PendingOp> {
        if (!pendingFile.exists()) return mutableListOf()
        return try {
            json.decodeFromString<List<PendingOp>>(pendingFile.readText()).toMutableList()
        } catch (_: Exception) { mutableListOf() }
    }

    private fun writePendingOps(ops: List<PendingOp>) {
        pendingFile.writeText(json.encodeToString(ops))
    }

    private fun addPendingOp(op: PendingOp) {
        val ops = readPendingOps()
        ops.removeAll { it.reminderId == op.reminderId && it.type == op.type }
        ops.add(op)
        writePendingOps(ops)
    }

    private fun removePendingOp(reminderId: String, type: String) {
        val ops = readPendingOps()
        ops.removeAll { it.reminderId == reminderId && it.type == type }
        writePendingOps(ops)
    }

    suspend fun getReminders(): List<ReminderItem> = withContext(Dispatchers.IO) {
        var remoteError: Exception? = null
        try {
            if (isOnline()) {
                val remote = remoteRepository.getReminders()
                writeCache(remote)
                _cachedReminders.value = remote
                return@withContext remote
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "getReminders remote failed", e)
            remoteError = e
        }
        val cached = readCache()
        if (cached.isEmpty() && remoteError != null) {
            throw remoteError
        }
        _cachedReminders.value = cached
        cached
    }

    suspend fun addReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        val cached = readCache().toMutableList()
        cached.removeAll { it.id == item.id }
        cached.add(0, item)
        writeCache(cached)
        _cachedReminders.value = cached

        if (isOnline()) {
            try {
                remoteRepository.addReminder(item)
                removePendingOp(item.id, "add")
            } catch (e: Exception) {
                Log.e("OfflineRepo", "addReminder remote failed", e)
                addPendingOp(PendingOp("add", item.id))
            }
        } else {
            addPendingOp(PendingOp("add", item.id))
        }
    }

    suspend fun updateReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        val cached = readCache().toMutableList()
        cached.removeAll { it.id == item.id }
        cached.add(0, item)
        writeCache(cached)
        _cachedReminders.value = cached

        if (isOnline()) {
            try {
                remoteRepository.updateReminder(item)
                removePendingOp(item.id, "update")
            } catch (e: Exception) {
                Log.e("OfflineRepo", "updateReminder remote failed", e)
                addPendingOp(PendingOp("update", item.id))
            }
        } else {
            addPendingOp(PendingOp("update", item.id))
        }
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        val cached = readCache().toMutableList()
        cached.removeAll { it.id == id }
        writeCache(cached)
        _cachedReminders.value = cached

        if (isOnline()) {
            try {
                remoteRepository.deleteReminder(id)
            } catch (e: Exception) {
                Log.e("OfflineRepo", "deleteReminder remote failed", e)
                addPendingOp(PendingOp("delete", id))
            }
        } else {
            addPendingOp(PendingOp("delete", id))
        }
    }

    suspend fun getReminder(id: String): ReminderItem? = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val remote = remoteRepository.getReminder(id)
                if (remote != null) {
                    val cached = readCache().toMutableList()
                    cached.removeAll { it.id == id }
                    cached.add(0, remote)
                    writeCache(cached)
                }
                return@withContext remote
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "getReminder remote failed", e)
        }
        readCache().find { it.id == id }
    }

    suspend fun searchReminders(query: String): List<ReminderItem> = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                return@withContext remoteRepository.searchReminders(query)
            }
        } catch (e: Exception) {
            Log.e("OfflineRepo", "searchReminders remote failed", e)
        }
        readCache().filter {
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
        val ops = readPendingOps()
        if (ops.isEmpty()) return@withContext

        val cache = readCache().associateBy { it.id }
        val remaining = mutableListOf<PendingOp>()

        for (op in ops) {
            try {
                when (op.type) {
                    "add" -> cache[op.reminderId]?.let { remoteRepository.addReminder(it) }
                    "update" -> cache[op.reminderId]?.let { remoteRepository.updateReminder(it) }
                    "delete" -> remoteRepository.deleteReminder(op.reminderId)
                }
            } catch (e: Exception) {
                Log.e("OfflineRepo", "syncPending: ${op.type} ${op.reminderId} failed", e)
                remaining.add(op)
            }
        }
        writePendingOps(remaining)
    }
}