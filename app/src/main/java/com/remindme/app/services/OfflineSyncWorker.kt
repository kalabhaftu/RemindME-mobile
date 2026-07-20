package com.remindme.app.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.remindme.app.data.remote.SupabaseManager
import com.remindme.app.data.repository.OfflineReminderRepository
import com.remindme.app.data.repository.ReminderRepository
import io.github.jan.supabase.auth.auth

class OfflineSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            if (SupabaseManager.client.auth.currentSessionOrNull() == null) return Result.success()
            OfflineReminderRepository(
                ReminderRepository(SupabaseManager.client, applicationContext),
                applicationContext
            ).syncPending()
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
