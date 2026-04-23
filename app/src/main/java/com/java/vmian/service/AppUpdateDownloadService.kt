package com.java.vmian.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.update.AppUpdateCoordinator
import com.java.vmian.update.AppUpdateFileStore
import com.java.vmian.update.AppUpdateIntegrityVerifier
import com.java.vmian.update.AppUpdateNotificationFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class AppUpdateDownloadService : Service() {
    companion object {
        private const val ACTION_START = "com.java.vmian.action.APP_UPDATE_START"
        private const val ACTION_CANCEL = "com.java.vmian.action.APP_UPDATE_CANCEL"

        fun createStartIntent(context: android.content.Context): Intent {
            return Intent(context, AppUpdateDownloadService::class.java).apply {
                action = ACTION_START
            }
        }

        fun createCancelIntent(context: android.content.Context): Intent {
            return Intent(context, AppUpdateDownloadService::class.java).apply {
                action = ACTION_CANCEL
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val okHttpClient = OkHttpClient.Builder().build()
    private val integrityVerifier = AppUpdateIntegrityVerifier()
    private var activeJob: Job? = null
    private var activeCall: Call? = null
    private var activeTempFile: File? = null
    private var cancellationRequested = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL -> {
                handleCancel(startId)
                return START_NOT_STICKY
            }
            ACTION_START, null -> Unit
            else -> return START_NOT_STICKY
        }

        if (activeJob?.isActive == true) {
            return START_NOT_STICKY
        }

        cancellationRequested = false
        AppUpdateNotificationFactory.ensureChannel(this)
        val notification = AppUpdateNotificationFactory.buildProgressNotification(this, 0, "准备下载…")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                AppUpdateNotificationFactory.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(AppUpdateNotificationFactory.NOTIFICATION_ID, notification)
        }

        activeJob = serviceScope.launch {
            var tempFile: File? = null
            runCatching {
                val updateInfo = AppUpdateCoordinator.requirePendingUpdate()
                tempFile = AppUpdateFileStore.createTempApkFile(this@AppUpdateDownloadService, updateInfo)
                activeTempFile = tempFile
                streamDownload(updateInfo, requireNotNull(tempFile))
                val completedFile = AppUpdateFileStore.promoteTempFile(requireNotNull(tempFile), updateInfo)
                check(integrityVerifier.verifySha256(completedFile, updateInfo.sha256)) {
                    "安装包校验失败"
                }
                AppUpdateCoordinator.onDownloadCompleted(updateInfo, completedFile)
                AppUpdateCoordinator.requestInstallOrNotify(this@AppUpdateDownloadService, updateInfo, completedFile)
            }.onFailure { throwable ->
                tempFile?.let(AppUpdateFileStore::deleteQuietly)
                if (!cancellationRequested && !throwable.isCancellationSignal()) {
                    AppUpdateCoordinator.onDownloadFailed(throwable.message ?: "下载失败")
                    NotificationManagerCompat.from(this@AppUpdateDownloadService).notify(
                        AppUpdateNotificationFactory.NOTIFICATION_ID,
                        AppUpdateNotificationFactory.buildFailureNotification(
                            this@AppUpdateDownloadService,
                            throwable.message ?: "下载失败"
                        )
                    )
                }
            }.also {
                activeCall = null
                activeTempFile = null
                activeJob = null
            }
            clearForegroundNotification()
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        activeCall?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun streamDownload(updateInfo: AppUpdateInfo, destination: File) {
        var lastNotifiedProgress = -1
        val call = okHttpClient.newCall(Request.Builder().url(updateInfo.apkUrl).build())
        activeCall = call
        call.execute().use { response ->
            val body = response.body ?: error("Empty body")
            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    val totalBytes = body.contentLength()
                    while (true) {
                        serviceScope.coroutineContext.ensureActive()
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        val progress = if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0
                        AppUpdateCoordinator.onDownloadProgress(updateInfo, destination, downloaded, totalBytes)
                        if (progress != lastNotifiedProgress) {
                            NotificationManagerCompat.from(this).notify(
                                AppUpdateNotificationFactory.NOTIFICATION_ID,
                                AppUpdateNotificationFactory.buildProgressNotification(
                                    this,
                                    progress,
                                    "已下载 ${progress}%"
                                )
                            )
                            lastNotifiedProgress = progress
                        }
                    }
                }
            }
        }
    }

    private fun handleCancel(startId: Int) {
        cancellationRequested = true
        activeCall?.cancel()
        activeJob?.cancel(CancellationException("下载已取消"))
        activeTempFile?.let(AppUpdateFileStore::deleteQuietly)
        activeTempFile = null
        activeCall = null
        activeJob = null
        AppUpdateCoordinator.reset()
        clearForegroundNotification()
        stopSelf(startId)
    }

    private fun clearForegroundNotification() {
        NotificationManagerCompat.from(this).cancel(AppUpdateNotificationFactory.NOTIFICATION_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun Throwable.isCancellationSignal(): Boolean {
        return this is CancellationException ||
            this is IOException && message?.contains("Canceled", ignoreCase = true) == true
    }
}
