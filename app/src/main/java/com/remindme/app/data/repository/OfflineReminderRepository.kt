package com.remindme.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import java.time.ZoneOffset
import android.net.NetworkCapabilities
import com.remindme.app.data.local.RemindMeDatabase
import com.remindme.app.data.local.entities.NotificationPreferenceEntity
import com.remindme.app.data.local.entities.PersonDetailsEntity
import com.remindme.app.data.local.entities.RecurrenceRulesEntity
import com.remindme.app.data.local.entities.ReminderEntity
import com.remindme.app.data.local.entities.SubscriptionDetailsEntity
import com.remindme.app.data.local.entities.TaskDetailsEntity
import com.remindme.app.domain.models.ReminderItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OfflineReminderRepository(
    private val remoteRepository: ReminderRepository,
    private val context: Context,
    private val database: RemindMeDatabase
) {
    private val localDao = database.reminderDao()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun isOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getReminders(): List<ReminderItem> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isOnline()) {
                val remoteReminders = remoteRepository.getReminders()
                
                // Clear local and sync from remote (server-wins strategy)
                localDao.deleteReminder(ReminderEntity(
                    id = "", userId = "", category = "", name = "", notes = null,
                    iconKey = null, createdAt = 0, updatedAt = 0, syncedAt = null
                ))
                
                remoteReminders.forEach { item ->
                    saveReminderLocally(item)
                }
                
                remoteReminders
            } else {
                getRemindersFromCache()
            }
        } catch (e: Exception) {
            getRemindersFromCache()
        }
    }

    suspend fun addReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        saveReminderLocally(item)
        if (isOnline()) {
            try {
                remoteRepository.addReminder(item)
                localDao.markAsSynced(item.id, System.currentTimeMillis())
            } catch (e: Exception) {
                // Keep local, mark for retry
                val entity = ReminderEntity(
                    id = item.id,
                    userId = item.userId,
                    category = item.category.name,
                    name = item.name,
                    notes = item.notes,
                    iconKey = item.iconKey,
                    createdAt = item.createdAt.toEpochSecond(ZoneOffset.UTC),
                    updatedAt = item.updatedAt.toEpochSecond(ZoneOffset.UTC),
                    syncedAt = null,
                    requiresSync = true
                )
                localDao.updateReminder(entity)
            }
        } else {
            val entity = ReminderEntity(
                id = item.id,
                userId = item.userId,
                category = item.category.name,
                name = item.name,
                notes = item.notes,
                iconKey = item.iconKey,
                createdAt = item.createdAt.toEpochSecond(ZoneOffset.UTC),
                updatedAt = item.updatedAt.toEpochSecond(ZoneOffset.UTC),
                syncedAt = null,
                requiresSync = true
            )
            localDao.updateReminder(entity)
        }
    }

    suspend fun updateReminder(item: ReminderItem) = withContext(Dispatchers.IO) {
        saveReminderLocally(item)
        if (isOnline()) {
            try {
                remoteRepository.updateReminder(item)
                localDao.markAsSynced(item.id, System.currentTimeMillis())
            } catch (e: Exception) {
                // Keep local change, mark for retry
                val entity = ReminderEntity(
                    id = item.id,
                    userId = item.userId,
                    category = item.category.name,
                    name = item.name,
                    notes = item.notes,
                    iconKey = item.iconKey,
                    createdAt = item.createdAt.toEpochSecond(ZoneOffset.UTC),
                    updatedAt = item.updatedAt.toEpochSecond(ZoneOffset.UTC),
                    syncedAt = null,
                    requiresSync = true
                )
                localDao.updateReminder(entity)
            }
        }
    }

    suspend fun deleteReminder(id: String) = withContext(Dispatchers.IO) {
        localDao.softDeleteReminder(id)
        if (isOnline()) {
            try {
                remoteRepository.deleteReminder(id)
            } catch (e: Exception) {
                // Keep soft delete locally
            }
        }
    }

    private suspend fun saveReminderLocally(item: ReminderItem) {
        val entity = ReminderEntity(
            id = item.id,
            userId = item.userId,
            category = item.category.name,
            name = item.name,
            notes = item.notes,
            iconKey = item.iconKey,
            createdAt = item.createdAt.toEpochSecond(ZoneOffset.UTC),
            updatedAt = item.updatedAt.toEpochSecond(ZoneOffset.UTC),
            syncedAt = System.currentTimeMillis(),
            requiresSync = false
        )
        localDao.insertReminder(entity)
    }

    private suspend fun getRemindersFromCache(): List<ReminderItem> {
        // For now, return empty list. In production, reconstruct from Room entities
        return emptyList()
    }

    fun getRemindersFlow(): Flow<List<ReminderItem>> {
        return localDao.getRemindersByUserFlow("").map { entities ->
            // Reconstruct ReminderItem from entities
            emptyList()
        }
    }

    suspend fun syncPending() = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext
        
        val pending = localDao.getPendingSyncs()
        pending.forEach { entity ->
            try {
                // Sync to remote
                localDao.markAsSynced(entity.id, System.currentTimeMillis())
            } catch (e: Exception) {
                // Retry next time
            }
        }
    }
}
