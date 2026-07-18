package com.remindme.app.ui.components

import android.content.Context
import android.content.SharedPreferences

object ThemePrefs {
    private const val PREFS_NAME = "remindme_prefs"
    private const val KEY_STYLE = "theme_style"

    fun getStyle(context: Context): ThemeStyle {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ordinal = prefs.getInt(KEY_STYLE, ThemeStyle.Glass.ordinal)
        return ThemeStyle.entries.getOrElse(ordinal) { ThemeStyle.Glass }
    }

    fun setStyle(context: Context, style: ThemeStyle) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_STYLE, style.ordinal).apply()
    }
}
