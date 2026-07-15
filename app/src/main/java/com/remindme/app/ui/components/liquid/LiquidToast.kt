package com.remindme.app.ui.components.liquid

import android.content.Context
import android.widget.Toast

object LiquidToast {
    fun show(context: Context, message: String, isLong: Boolean = false) {
        Toast.makeText(context, message, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
}
