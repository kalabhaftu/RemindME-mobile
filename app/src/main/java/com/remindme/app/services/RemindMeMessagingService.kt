package com.remindme.app.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        if (body.isNotEmpty()) {
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "remindme_channel"
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "RemindME Notifications",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)

        // System.currentTimeMillis().toInt() truncates a 64-bit timestamp to
        // 32 bits and can produce the same ID for notifications dispatched
        // close together, silently overwriting one instead of showing both.
        // A hash of title+message+a random component is a lot less likely
        // to collide across near-simultaneous notifications.
        val notificationId = (title + message + System.nanoTime()).hashCode()
        notificationManager.notify(notificationId, builder.build())
    }
}
