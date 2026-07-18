package com.remindme.app.ui.components.liquid

import android.content.Context
import android.content.SharedPreferences

object LiquidGlassPrefs {
    private const val PREFS_NAME = "liquid_glass_prefs"
    private const val KEY_STYLE = "glass_style"

    fun getStyle(context: Context): LiquidGlassStyle {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ordinal = prefs.getInt(KEY_STYLE, LiquidGlassStyle.Glass.ordinal)
        return LiquidGlassStyle.entries.getOrElse(ordinal) { LiquidGlassStyle.Glass }
    }

    fun setStyle(context: Context, style: LiquidGlassStyle) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_STYLE, style.ordinal).apply()
    }
}
