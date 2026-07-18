package com.remindme.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCES_NAME = "remindme_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

object PreferencesKeys {
    val LAST_REMINDER_CATEGORY = stringPreferencesKey("last_reminder_category")
    val LAST_NOTIFICATION_CHANNEL = stringPreferencesKey("last_notification_channel")
    val LAST_NOTIFICATION_LEAD_TIME = stringPreferencesKey("last_notification_lead_time")
    val LAST_NOTIFICATION_TIME = stringPreferencesKey("last_notification_time")
    val LAST_NOTIFICATION_OFFSET = stringPreferencesKey("last_notification_offset")
    val APP_THEME = stringPreferencesKey("app_theme")
    val SYNC_STATUS = stringPreferencesKey("sync_status")
}

class PreferenceDataStore(private val context: Context) {
    fun getLastReminderCategory(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_REMINDER_CATEGORY] }

    suspend fun setLastReminderCategory(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_REMINDER_CATEGORY] = value
        }
    }

    fun getLastNotificationChannel(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_NOTIFICATION_CHANNEL] }

    suspend fun setLastNotificationChannel(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFICATION_CHANNEL] = value
        }
    }

    fun getLastNotificationLeadTime(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_NOTIFICATION_LEAD_TIME] }

    suspend fun setLastNotificationLeadTime(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFICATION_LEAD_TIME] = value
        }
    }

    fun getLastNotificationTime(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_NOTIFICATION_TIME] }

    suspend fun setLastNotificationTime(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFICATION_TIME] = value
        }
    }

    fun getAppTheme(): Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.APP_THEME] ?: "blur" }

    suspend fun setAppTheme(value: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = value
        }
    }

    fun getSyncStatus(): Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.SYNC_STATUS] ?: "synced" }

    suspend fun setSyncStatus(status: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_STATUS] = status
        }
    }
}
