package com.remindme.app.services

import com.google.firebase.messaging.FirebaseMessaging
import com.remindme.app.data.remote.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PushTokenRegistrar {
    suspend fun registerCurrentToken() {
        val token = suspendCancellableCoroutine<String> { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        register(token)
    }

    suspend fun register(token: String) {
        val user = SupabaseManager.client.auth.currentUserOrNull() ?: return
        val stableId = UUID.nameUUIDFromBytes(token.toByteArray(StandardCharsets.UTF_8)).toString()
        val payload = buildJsonObject {
            put("id", JsonPrimitive(stableId))
            put("user_id", JsonPrimitive(user.id))
            put("channel", JsonPrimitive("push"))
            put("encrypted_token", JsonPrimitive(token))
            put("verified_at", JsonPrimitive(Instant.now().toString()))
        }
        SupabaseManager.client.postgrest["notification_channels"].upsert(payload)
    }
}
