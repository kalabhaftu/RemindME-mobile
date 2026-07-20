package com.remindme.app.ui.components

import android.content.Context

object NotificationPrefsStore {
    private const val PREFS = "notification_defaults"

    private val defaultPrefs = mapOf(
        "email" to ChannelPref(enabled = true, leadTime = "at_time"),
        "push" to ChannelPref(enabled = true, leadTime = "at_time"),
        "telegram" to ChannelPref(enabled = false, leadTime = "at_time"),
        "in_app" to ChannelPref(enabled = true, leadTime = "at_time")
    )

    fun load(context: Context): Map<String, ChannelPref> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return defaultPrefs.mapValues { (channel, fallback) ->
            ChannelPref(
                enabled = prefs.getBoolean("${channel}_enabled", fallback.enabled),
                leadTime = prefs.getString("${channel}_lead_time", fallback.leadTime) ?: fallback.leadTime,
                customTime = prefs.getString("${channel}_custom_time", fallback.customTime) ?: fallback.customTime,
                offsetDays = prefs.getInt("${channel}_offset_days", fallback.offsetDays)
            )
        }
    }

    fun save(context: Context, values: Map<String, ChannelPref>) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().apply {
            values.forEach { (channel, pref) ->
                putBoolean("${channel}_enabled", pref.enabled)
                putString("${channel}_lead_time", pref.leadTime)
                putString("${channel}_custom_time", pref.customTime)
                putInt("${channel}_offset_days", pref.offsetDays)
            }
            apply()
        }
    }
}
