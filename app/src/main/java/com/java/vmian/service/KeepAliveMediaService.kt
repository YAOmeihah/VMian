package com.java.vmian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.java.vmian.MainActivity
import com.java.vmian.R
import com.java.vmian.util.KeepAliveSettingsStore
import com.java.vmian.util.SharedKeepAliveServiceStartGate
import kotlin.math.min

/**
 * 静音媒体播放保活服务
 * 播放几乎听不到的无声 PCM 音频，让系统认为该 App 是"正在播放媒体"的前台应用，
 * 从而提高进程优先级，降低被系统杀死的概率。
 */
class KeepAliveMediaService : Service() {

    companion object {
        private const val TAG = "KeepAliveMediaService"
        private const val FOREGROUND_NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "vmq_keepalive_media_channel"
        private const val PLAYBACK_RESTART_DELAY_MS = 1_000L

        @Volatile
        private var isRunning = false

        fun isServiceRunning(): Boolean = isRunning

        fun start(context: Context) {
            if (!SharedKeepAliveServiceStartGate.shouldAllowStart("media")) return
            try {
                val intent = Intent(context, KeepAliveMediaService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "启动静音媒体播放保活服务失败", e)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, KeepAliveMediaService::class.java)
            context.stopService(intent)
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private var mediaDataSource: MediaDataSource? = null
    private val handler = Handler(Looper.getMainLooper())
    private val restartPlaybackRunnable = Runnable {
        if (KeepAliveSettingsStore.isMediaEnabled(this)) {
            Log.d(TAG, "尝试恢复静音媒体播放")
            isRunning = startSilentPlayback()
        } else {
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "静音媒体播放服务创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        isRunning = startSilentPlayback()
        if (!isRunning) {
            Log.w(TAG, "静音媒体播放启动失败，停止保活服务")
            stopSelf(startId)
            return START_NOT_STICKY
        }
        Log.d(TAG, "静音媒体播放保活服务启动")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(restartPlaybackRunnable)
        stopSilentPlayback()
        isRunning = false
        Log.d(TAG, "静音媒体播放保活服务已停止")
        super.onDestroy()
    }

    private fun startForegroundNotification() {
        createNotificationChannel()
        val notification = buildForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.keepalive_media_service_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.keepalive_media_text)
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.keepalive_media_title))
            .setContentText(getString(R.string.keepalive_media_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun startSilentPlayback(): Boolean {
        stopSilentPlayback()

        return try {
            val source = ByteArrayMediaDataSource(KeepAliveAudioSource.createSilentWav())
            mediaDataSource = source
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(source)
                isLooping = true
                setVolume(0f, 0f)
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer 播放错误: what=$what extra=$extra")
                    handlePlaybackFailure()
                    true
                }
                setOnCompletionListener {
                    Log.w(TAG, "MediaPlayer 静音播放意外结束，准备恢复")
                    handlePlaybackFailure()
                }
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "静音媒体播放异常", e)
            stopSilentPlayback()
            false
        }
    }

    private fun handlePlaybackFailure() {
        handler.removeCallbacks(restartPlaybackRunnable)
        isRunning = false
        stopSilentPlayback()
        handler.postDelayed(restartPlaybackRunnable, PLAYBACK_RESTART_DELAY_MS)
    }

    private fun stopSilentPlayback() {
        val player = mediaPlayer
        mediaPlayer = null
        try {
            player?.apply {
                if (isPlaying) {
                    stop()
                }
            }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "停止 MediaPlayer 时状态异常", e)
        } catch (e: Exception) {
            Log.e(TAG, "停止 MediaPlayer 异常", e)
        } finally {
            runCatching { player?.release() }
                .onFailure { Log.e(TAG, "释放 MediaPlayer 异常", it) }
            runCatching { mediaDataSource?.close() }
            mediaDataSource = null
        }
    }

    private class ByteArrayMediaDataSource(
        private val bytes: ByteArray
    ) : MediaDataSource() {

        private var closed = false

        override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
            if (closed || position >= bytes.size) return -1
            val length = min(size, bytes.size - position.toInt())
            System.arraycopy(bytes, position.toInt(), buffer, offset, length)
            return length
        }

        override fun getSize(): Long = bytes.size.toLong()

        override fun close() {
            closed = true
        }
    }

}
