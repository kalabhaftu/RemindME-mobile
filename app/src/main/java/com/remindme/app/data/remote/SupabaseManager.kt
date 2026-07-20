package com.remindme.app.data.remote

import com.remindme.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import android.net.Uri

object SupabaseManager {
    lateinit var client: SupabaseClient
        private set

    val magicLinkRedirectUrl: String
        get() = BuildConfig.MAGIC_LINK_REDIRECT_URL

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
            install(Auth) {
                val redirect = Uri.parse(BuildConfig.MAGIC_LINK_REDIRECT_URL)
                scheme = redirect.scheme ?: "remindamie"
                host = redirect.host ?: "fallback"
                flowType = FlowType.PKCE
            }
            install(Realtime)
            install(Storage)
        }
    }
}
