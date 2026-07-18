package com.remindme.app.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.remindme.app.domain.models.ReminderItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationSchedulingService(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminder(reminder: ReminderItem) {
        val nextOccurrence = reminder.recurrence?.nextOccurrenceAt ?: return
        try {
            val instant = Instant.parse(nextOccurrence)
            val triggerTimeMillis = instant.toEpochMilli()
            val nowMillis = System.currentTimeMillis()

            if (triggerTimeMillis <= nowMillis) return

            val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("reminder_name", reminder.name)
                putExtra("reminder_category", reminder.category.name)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager?.let {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (it.canScheduleExactAlarms()) {
                            it.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                        } else {
                            it.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                        }
                    } else {
                        it.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(reminderId: String) {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager?.cancel(it)
        }
    }

    fun scheduleReminders(reminders: List<ReminderItem>) {
        reminders.forEach { scheduleReminder(it) }
    }
}
