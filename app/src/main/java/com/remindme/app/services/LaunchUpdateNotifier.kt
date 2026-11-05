package com.remindme.app.services

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LaunchUpdateNotifier {
    private const val notificationId = 9401

    fun checkOnLaunch(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val info = UpdateService.checkForUpdate(appContext.packageManager, appContext.packageName) ?: return@launch
            if (!info.updateAvailable) return@launch
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }

            val url = info.downloadUrl.ifBlank {
                "https://github.com/kalabhaftu/RemindME-mobile/releases/latest"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                appContext,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(appContext, NotificationChannels.updates)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("RemindME ${info.latestVersion} is available")
                .setContentText("You’re on ${info.currentVersion}. Tap to download the latest APK.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("You’re on ${info.currentVersion}. RemindME ${info.latestVersion} is available. Tap to download the latest APK.")
                )
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                manager.getNotificationChannel(NotificationChannels.updates)?.importance != NotificationManager.IMPORTANCE_NONE
            ) {
                NotificationManagerCompat.from(appContext).notify(notificationId, notification)
            }
        }
    }
}
