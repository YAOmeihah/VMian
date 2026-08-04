package com.java.vmian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.java.vmian.MainActivity
import com.java.vmian.R
import com.java.vmian.presentation.ui.model.KeepAliveFloatingStatusText
import com.java.vmian.ui.KeepAliveFloatingStatusViewFactory
import com.java.vmian.util.KeepAliveController
import com.java.vmian.util.KeepAliveRuntimeStatusStore
import com.java.vmian.util.KeepAliveSettingsStore
import com.java.vmian.util.SharedKeepAliveServiceStartGate

class KeepAliveOverlayService : Service() {

    companion object {
        private const val TAG = "KeepAliveOverlayService"
        private const val FOREGROUND_NOTIFICATION_ID = 1003
        private const val CHANNEL_ID = "vmq_keepalive_overlay_channel"
        private const val STATUS_REFRESH_INTERVAL_MS = 15_000L

        @Volatile
        private var isRunning = false

        fun isServiceRunning(): Boolean = isRunning

        fun start(context: Context) {
            if (!KeepAliveController.canDrawOverlays(context)) return
            if (!SharedKeepAliveServiceStartGate.shouldAllowStart("overlay")) return
            try {
                val intent = Intent(context, KeepAliveOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "启动悬浮窗保活服务失败", e)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, KeepAliveOverlayService::class.java))
        }
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private val refreshStatusRunnable = object : Runnable {
        override fun run() {
            refreshFloatingStatus()
            handler.postDelayed(this, STATUS_REFRESH_INTERVAL_MS)
        }
    }
    private val heartbeatStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == KeepAliveRuntimeStatusStore.ACTION_HEARTBEAT_STATUS_CHANGED) {
                refreshFloatingStatus()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        isRunning = showOverlay()
        if (!isRunning) {
            stopSelf()
            return START_NOT_STICKY
        }
        registerHeartbeatStatusReceiver()
        handler.removeCallbacks(refreshStatusRunnable)
        handler.post(refreshStatusRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(refreshStatusRunnable)
        unregisterHeartbeatStatusReceiver()
        removeOverlay()
        isRunning = false
        super.onDestroy()
    }

    private fun showOverlay(): Boolean {
        if (overlayView != null) return true
        if (!KeepAliveController.canDrawOverlays(this)) return false

        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = KeepAliveFloatingStatusViewFactory.create(
            context = this,
            text = createFloatingStatusText()
        )

        val params = WindowManager.LayoutParams(
            KeepAliveFloatingStatusViewFactory.widthPx(this),
            KeepAliveFloatingStatusViewFactory.heightPx(this),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            x = resources.displayMetrics.widthPixels -
                KeepAliveFloatingStatusViewFactory.widthPx(this@KeepAliveOverlayService) - 16
            y = 120
        }
        attachDragHandler(view, manager, params)

        return try {
            manager.addView(view, params)
            windowManager = manager
            overlayView = view
            Log.d(TAG, "悬浮窗状态标签已显示")
            true
        } catch (e: Exception) {
            Log.e(TAG, "显示悬浮窗状态标签失败", e)
            overlayView = null
            false
        }
    }

    private fun createFloatingStatusText() = KeepAliveFloatingStatusText.overlay(
        mediaEnabled = KeepAliveSettingsStore.isMediaEnabled(this),
        lastHeartbeatAtMillis = KeepAliveRuntimeStatusStore.getLastHeartbeatAt(this)
    )

    private fun refreshFloatingStatus() {
        KeepAliveFloatingStatusViewFactory.update(overlayView, createFloatingStatusText())
    }

    private fun registerHeartbeatStatusReceiver() {
        val filter = IntentFilter(KeepAliveRuntimeStatusStore.ACTION_HEARTBEAT_STATUS_CHANGED)
        try {
            ContextCompat.registerReceiver(
                this,
                heartbeatStatusReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } catch (e: Exception) {
            Log.e(TAG, "注册心跳状态广播失败", e)
        }
    }

    private fun unregisterHeartbeatStatusReceiver() {
        runCatching { unregisterReceiver(heartbeatStatusReceiver) }
    }

    private fun attachDragHandler(
        view: View,
        manager: WindowManager,
        params: WindowManager.LayoutParams
    ) {
        var downRawX = 0f
        var downRawY = 0f
        var downX = 0
        var downY = 0

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    downX = params.x
                    downY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = downX + (event.rawX - downRawX).toInt()
                    params.y = downY + (event.rawY - downRawY).toInt()
                    runCatching { manager.updateViewLayout(view, params) }
                    true
                }
                else -> true
            }
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        try {
            windowManager?.removeView(view)
            Log.d(TAG, "悬浮窗状态标签已移除")
        } catch (e: Exception) {
            Log.e(TAG, "移除悬浮窗状态标签失败", e)
        } finally {
            overlayView = null
            windowManager = null
        }
    }

    private fun startForegroundNotification() {
        createNotificationChannel()
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.keepalive_overlay_service_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.keepalive_overlay_text)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.keepalive_overlay_title))
            .setContentText(getString(R.string.keepalive_overlay_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
