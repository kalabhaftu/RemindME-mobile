package com.remindme.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.remindme.app.data.local.daos.NotificationPreferencesDao
import com.remindme.app.data.local.daos.PersonDetailsDao
import com.remindme.app.data.local.daos.RecurrenceRulesDao
import com.remindme.app.data.local.daos.ReminderDao
import com.remindme.app.data.local.daos.SubscriptionDetailsDao
import com.remindme.app.data.local.daos.TaskDetailsDao
import com.remindme.app.data.local.entities.NotificationPreferenceEntity
import com.remindme.app.data.local.entities.PersonDetailsEntity
import com.remindme.app.data.local.entities.RecurrenceRulesEntity
import com.remindme.app.data.local.entities.ReminderEntity
import com.remindme.app.data.local.entities.SubscriptionDetailsEntity
import com.remindme.app.data.local.entities.TaskDetailsEntity

@Database(
    entities = [
        ReminderEntity::class,
        PersonDetailsEntity::class,
        TaskDetailsEntity::class,
        SubscriptionDetailsEntity::class,
        RecurrenceRulesEntity::class,
        NotificationPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RemindMeDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun personDetailsDao(): PersonDetailsDao
    abstract fun taskDetailsDao(): TaskDetailsDao
    abstract fun subscriptionDetailsDao(): SubscriptionDetailsDao
    abstract fun recurrenceRulesDao(): RecurrenceRulesDao
    abstract fun notificationPreferencesDao(): NotificationPreferencesDao

    companion object {
        private var INSTANCE: RemindMeDatabase? = null

        fun getInstance(context: Context): RemindMeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RemindMeDatabase::class.java,
                    "remindme_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
