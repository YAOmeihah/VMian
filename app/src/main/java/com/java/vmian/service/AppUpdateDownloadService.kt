package com.java.vmian.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.update.AppUpdateCoordinator
import com.java.vmian.update.AppUpdateFileStore
import com.java.vmian.update.AppUpdateIntegrityVerifier
import com.java.vmian.update.AppUpdateNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class AppUpdateDownloadService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val okHttpClient = OkHttpClient.Builder().build()
    private val integrityVerifier = AppUpdateIntegrityVerifier()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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

        serviceScope.launch {
            var tempFile: File? = null
            runCatching {
                val updateInfo = AppUpdateCoordinator.requirePendingUpdate()
                tempFile = AppUpdateFileStore.createTempApkFile(this@AppUpdateDownloadService, updateInfo)
                streamDownload(updateInfo, requireNotNull(tempFile))
                val completedFile = AppUpdateFileStore.promoteTempFile(requireNotNull(tempFile), updateInfo)
                check(integrityVerifier.verifySha256(completedFile, updateInfo.sha256)) {
                    "安装包校验失败"
                }
                AppUpdateCoordinator.onDownloadCompleted(updateInfo, completedFile)
                AppUpdateCoordinator.requestInstallOrNotify(this@AppUpdateDownloadService, updateInfo, completedFile)
            }.onFailure { throwable ->
                tempFile?.let(AppUpdateFileStore::deleteQuietly)
                AppUpdateCoordinator.onDownloadFailed(throwable.message ?: "下载失败")
                NotificationManagerCompat.from(this@AppUpdateDownloadService).notify(
                    AppUpdateNotificationFactory.NOTIFICATION_ID,
                    AppUpdateNotificationFactory.buildFailureNotification(
                        this@AppUpdateDownloadService,
                        throwable.message ?: "下载失败"
                    )
                )
            }
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun streamDownload(updateInfo: AppUpdateInfo, destination: File) {
        okHttpClient.newCall(Request.Builder().url(updateInfo.apkUrl).build()).execute().use { response ->
            val body = response.body ?: error("Empty body")
            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    val totalBytes = body.contentLength()
                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        AppUpdateCoordinator.onDownloadProgress(updateInfo, destination, downloaded, totalBytes)
                        NotificationManagerCompat.from(this).notify(
                            AppUpdateNotificationFactory.NOTIFICATION_ID,
                            AppUpdateNotificationFactory.buildProgressNotification(
                                this,
                                if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0,
                                "已下载 ${if (totalBytes > 0) ((downloaded * 100) / totalBytes).toInt() else 0}%"
                            )
                        )
                    }
                }
            }
        }
    }
}
