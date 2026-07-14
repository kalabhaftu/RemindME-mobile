package com.example.remindme_mobile

import android.app.Application
import com.example.remindme_mobile.data.remote.SupabaseManager
import com.google.firebase.FirebaseApp

class RemindMeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        SupabaseManager.initialize(
            url = BuildConfig.SUPABASE_URL,
            key = BuildConfig.SUPABASE_ANON_KEY
        )
    }
}
