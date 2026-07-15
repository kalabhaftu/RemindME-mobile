package com.remindme.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    lateinit var client: SupabaseClient
        private set

    fun initialize(url: String, key: String) {
        client = createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
