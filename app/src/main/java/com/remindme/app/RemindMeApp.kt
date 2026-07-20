package com.remindme.app

import android.app.Application
import com.remindme.app.data.remote.SupabaseManager
import com.google.firebase.FirebaseApp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class RemindMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        SupabaseManager.initialize(
            url = BuildConfig.SUPABASE_URL,
            key = BuildConfig.SUPABASE_ANON_KEY
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminders",
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled reminder notifications"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
