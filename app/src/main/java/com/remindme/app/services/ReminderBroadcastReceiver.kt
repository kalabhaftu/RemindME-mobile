package com.remindme.app.services

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.remindme.app.R
import androidx.core.app.TaskStackBuilder
import com.remindme.app.MainActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderName = intent.getStringExtra("reminder_name") ?: "Reminder"
        val category = intent.getStringExtra("reminder_category") ?: "TASK"

        val contentIntent = TaskStackBuilder.create(context).run {
            val openIntent = Intent(context, MainActivity::class.java).apply {
                putExtra("open_reminder_id", reminderId)
                putExtra("open_reminder_category", category)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            addNextIntentWithParentStack(openIntent)
            getPendingIntent(
                reminderId.hashCode(),
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminderName)
            .setContentText("Time for your ${category.lowercase()} reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(reminderId.hashCode(), notification)
    }
}
