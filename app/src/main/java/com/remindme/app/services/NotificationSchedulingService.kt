package com.remindme.app.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.remindme.app.domain.models.ReminderItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Objects

class NotificationSchedulingService(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminders(reminders: List<ReminderItem>) {
        reminders.forEach { scheduleReminder(it) }
    }

    fun scheduleReminder(reminder: ReminderItem) {
        val nextOccurrence = reminder.recurrence?.nextOccurrenceAt ?: return
        val enabledPrefs = reminder.notificationPreferences.orEmpty().filter { it.enabled }
        if (enabledPrefs.isEmpty()) return

        try {
            val instant = Instant.parse(nextOccurrence)
            val nowMillis = System.currentTimeMillis()
            val zone = ZoneId.systemDefault()
            val event = instant.atZone(zone)

            enabledPrefs.forEach { pref ->
                val triggerInstant = resolveLocalTime(event.toLocalDate(), event.toLocalTime(), pref).atZone(zone).toInstant()
                val triggerTimeMillis = triggerInstant.toEpochMilli()

                if (triggerTimeMillis > nowMillis) {
                    val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
                        putExtra("reminder_id", reminder.id)
                        putExtra("reminder_name", reminder.name)
                        putExtra("reminder_category", reminder.category.name)
                        putExtra("preference_channel", pref.channel)
                    }

                    val requestCode = Objects.hash(reminder.id, pref.channel, pref.offsetDays, pref.leadTime)

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    alarmManager?.let {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && it.canScheduleExactAlarms()) {
                                it.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                            } else {
                                it.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                            }
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (_: Exception) { }
    }

    private fun resolveLocalTime(
        occurrenceDate: LocalDate,
        eventTime: LocalTime,
        preference: com.remindme.app.domain.models.NotificationPreference
    ): LocalDateTime {
        val date = occurrenceDate.minusDays(preference.offsetDays.toLong())
        val time = when (preference.leadTime) {
            "morning_of" -> LocalTime.of(9, 0)
            "noon_of" -> LocalTime.NOON
            "evening_of" -> LocalTime.of(18, 0)
            "custom" -> preference.customTime.toLocalTimeOrNull() ?: LocalTime.of(9, 0)
            else -> eventTime
        }
        return LocalDateTime.of(date, time)
    }

    private fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
        LocalTime.parse(this.take(5), DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull()

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
}
