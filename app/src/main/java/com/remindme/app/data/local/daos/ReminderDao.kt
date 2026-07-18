package com.remindme.app.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.remindme.app.data.local.entities.NotificationPreferenceEntity
import com.remindme.app.data.local.entities.PersonDetailsEntity
import com.remindme.app.data.local.entities.RecurrenceRulesEntity
import com.remindme.app.data.local.entities.ReminderEntity
import com.remindme.app.data.local.entities.SubscriptionDetailsEntity
import com.remindme.app.data.local.entities.TaskDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getRemindersByUser(userId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE userId = :userId AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getRemindersByUserFlow(userId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE requiresSync = 1")
    suspend fun getPendingSyncs(): List<ReminderEntity>

    @Query("UPDATE reminders SET requiresSync = 0, syncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: String, timestamp: Long)

    @Query("UPDATE reminders SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteReminder(id: String)

    @Query("DELETE FROM reminders WHERE isDeleted = 1 AND syncedAt IS NOT NULL AND syncedAt < :cutoffTime")
    suspend fun cleanupDeletedReminders(cutoffTime: Long)
}

@Dao
interface PersonDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personDetails: PersonDetailsEntity)

    @Query("SELECT * FROM person_details WHERE reminderId = :reminderId")
    suspend fun getByReminderId(reminderId: String): PersonDetailsEntity?
}

@Dao
interface TaskDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskDetails: TaskDetailsEntity)

    @Query("SELECT * FROM task_details WHERE reminderId = :reminderId")
    suspend fun getByReminderId(reminderId: String): TaskDetailsEntity?
}

@Dao
interface SubscriptionDetailsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscriptionDetails: SubscriptionDetailsEntity)

    @Query("SELECT * FROM subscription_details WHERE reminderId = :reminderId")
    suspend fun getByReminderId(reminderId: String): SubscriptionDetailsEntity?
}

@Dao
interface RecurrenceRulesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurrenceRules: RecurrenceRulesEntity)

    @Query("SELECT * FROM recurrence_rules WHERE reminderId = :reminderId")
    suspend fun getByReminderId(reminderId: String): RecurrenceRulesEntity?
}

@Dao
interface NotificationPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: NotificationPreferenceEntity)

    @Query("DELETE FROM notification_preferences WHERE reminderId = :reminderId")
    suspend fun deleteByReminderId(reminderId: String)

    @Query("SELECT * FROM notification_preferences WHERE reminderId = :reminderId")
    suspend fun getByReminderId(reminderId: String): List<NotificationPreferenceEntity>
}
