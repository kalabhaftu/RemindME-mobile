package com.remindme.app.data

import android.content.Context
import android.content.SharedPreferences

object AuthCache {
    private const val PREF_NAME = "remindme_auth_cache"
    private const val KEY_LAST_USER_ID = "last_user_id"
    private const val KEY_LAST_USER_EMAIL = "last_user_email"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, userId: String, email: String? = null) {
        prefs(context).edit().apply {
            putString(KEY_LAST_USER_ID, userId)
            if (email != null) {
                putString(KEY_LAST_USER_EMAIL, email)
            }
            apply()
        }
    }

    fun getUserId(context: Context): String? {
        return prefs(context).getString(KEY_LAST_USER_ID, null)
    }

    fun getUserEmail(context: Context): String? {
        return prefs(context).getString(KEY_LAST_USER_EMAIL, null)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
