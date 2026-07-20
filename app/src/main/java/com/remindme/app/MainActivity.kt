package com.remindme.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.remindme.app.ui.navigation.MainNavigation
import com.remindme.app.ui.theme.RemindmeMobileTheme
import io.github.jan.supabase.auth.handleDeeplinks
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    private var pendingReminderId by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle permission result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingReminderId = intent.getStringExtra("open_reminder_id")

        val prefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            prefs.edit().putString("last_crash", stackTrace).commit()
            defaultHandler?.uncaughtException(thread, throwable) ?: System.exit(2)
        }

        askNotificationPermission()

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            RemindmeMobileTheme {
                var showCrash by remember { mutableStateOf(lastCrash != null) }
                if (showCrash) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    AlertDialog(
                        onDismissRequest = { 
                            showCrash = false
                            prefs.edit().remove("last_crash").apply()
                        },
                        title = { Text("App Crashed Previously") },
                        text = { 
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.5f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(lastCrash ?: "") 
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Crash Log", lastCrash)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }) { Text("Copy") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showCrash = false
                                prefs.edit().remove("last_crash").apply()
                            }) { Text("Dismiss") }
                        }
                    )
                }
                MainNavigation(
                    openReminderId = pendingReminderId,
                    onReminderOpened = { pendingReminderId = null }
                )
            }
        }
        
        com.remindme.app.data.remote.SupabaseManager.client.handleDeeplinks(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingReminderId = intent.getStringExtra("open_reminder_id")
        com.remindme.app.data.remote.SupabaseManager.client.handleDeeplinks(intent)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
