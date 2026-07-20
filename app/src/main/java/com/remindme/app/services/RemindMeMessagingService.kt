package com.remindme.app.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import com.remindme.app.MainActivity

class RemindMeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PushTokenRegistrar.register(token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to sync token to Supabase", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "RemindME"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val reminderId = remoteMessage.data["reminder_item_id"]
        val category = remoteMessage.data["category"] ?: "reminder"

        if (body.isNotEmpty()) {
            showNotification(title, body, reminderId, category)
        }
    }

    private fun showNotification(title: String, message: String, reminderId: String?, category: String) {
        val channelId = NotificationChannels.reminders
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // The application creates the high-priority channel with vibration settings.
        }

        val contentIntent = TaskStackBuilder.create(this).run {
            val intent = Intent(this@RemindMeMessagingService, MainActivity::class.java).apply {
                if (!reminderId.isNullOrBlank()) {
                    putExtra("open_reminder_id", reminderId)
                    putExtra("open_reminder_category", category)
                }
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                (reminderId ?: "$title:$message").hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)

        // System.currentTimeMillis().toInt() truncates a 64-bit timestamp to
        // 32 bits and can produce the same ID for notifications dispatched
        // close together, silently overwriting one instead of showing both.
        // A hash of title+message+a random component is a lot less likely
        // to collide across near-simultaneous notifications.
        val notificationId = (title + message + System.nanoTime()).hashCode()
        notificationManager.notify(notificationId, builder.build())
    }
}
