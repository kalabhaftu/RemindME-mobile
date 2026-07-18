package com.remindme.app.services

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.remindme.app.R

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderName = intent.getStringExtra("reminder_name") ?: "Reminder"
        val category = intent.getStringExtra("reminder_category") ?: "TASK"

        val notification = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminderName)
            .setContentText("Time for your $category reminder")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(reminderId.hashCode(), notification)
    }
}
