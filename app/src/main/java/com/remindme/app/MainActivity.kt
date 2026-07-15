package com.remindme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.remindme.app.ui.navigation.MainNavigation
import com.remindme.app.ui.theme.RemindmeMobileTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      RemindmeMobileTheme {
        MainNavigation()
      }
    }
  }
}
