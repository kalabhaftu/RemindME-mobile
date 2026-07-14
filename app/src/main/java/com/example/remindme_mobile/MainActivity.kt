package com.example.remindme_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.example.remindme_mobile.ui.screens.MainShell
import com.example.remindme_mobile.ui.theme.RemindmeMobileTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      RemindmeMobileTheme {
        MainShell()
      }
    }
  }
}
