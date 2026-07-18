package com.remindme.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object SupabaseManager {
    lateinit var client: SupabaseClient
        private set

    fun initialize(url: String, key: String) {
        client = createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
            // A Kotlin model that's missing a column the DB returns (as
            // RecurrenceRules was, for next_occurrence_at, until this patch)
            // would throw on decode without ignoreUnknownKeys -- turning a
            // schema drift into "the app fetches nothing" instead of just
            // ignoring the extra field.
            defaultSerializer = KotlinXSerializer(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    encodeDefaults = true
                }
            )
            install(Postgrest)
            install(Auth)
            install(Realtime)
            install(Storage)
        }
    }
}
