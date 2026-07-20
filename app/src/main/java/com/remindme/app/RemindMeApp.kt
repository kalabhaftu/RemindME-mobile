package com.remindme.app

import android.app.Application
import com.remindme.app.data.remote.SupabaseManager
import com.google.firebase.FirebaseApp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.remindme.app.services.OfflineSyncScheduler
import com.remindme.app.services.NotificationChannels

class RemindMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        SupabaseManager.initialize(
            url = BuildConfig.SUPABASE_URL,
            key = BuildConfig.SUPABASE_ANON_KEY
        )

        OfflineSyncScheduler.schedule(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                NotificationChannels.reminders,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled reminder notifications"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 260, 120, 420)
                enableLights(true)
            }
            val updatesChannel = NotificationChannel(
                NotificationChannels.updates,
                "App updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New RemindME release notifications"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannels(
                listOf(remindersChannel, updatesChannel)
            )
        }
    }
}
