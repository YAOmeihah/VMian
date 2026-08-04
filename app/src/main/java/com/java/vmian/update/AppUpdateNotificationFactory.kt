package com.java.vmian.update

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import com.java.vmian.MainActivity
import com.java.vmian.R
import com.java.vmian.domain.model.AppUpdateInfo

object AppUpdateNotificationFactory {
    const val CHANNEL_ID = "app_update_channel"
    const val NOTIFICATION_ID = 2002

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "应用更新",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "应用更新下载与安装状态"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun notifyIfEnabled(context: Context, notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    fun buildProgressNotification(context: Context, progress: Int, message: String): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("应用更新下载中")
            .setContentText(message)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .build()
    }

    fun buildCompletionNotification(context: Context, info: AppUpdateInfo, apkPath: String): Notification {
        ensureChannel(context)
        val pendingIntent = PendingIntentCompat.getActivity(
            context,
            0,
            AppUpdateIntentFactory.createInstallerActivityIntent(context, apkPath),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("更新下载完成")
            .setContentText("点击安装 ${info.versionName}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    fun buildFailureNotification(context: Context, message: String): Notification {
        ensureChannel(context)
        val intent = android.content.Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntentCompat.getActivity(
            context,
            1,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT,
            false
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("更新失败")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }
}
