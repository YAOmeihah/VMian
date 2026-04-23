package com.java.vmian.update

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.model.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object AppUpdateCoordinator {
    private val mutableState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val state: StateFlow<AppUpdateState> = mutableState.asStateFlow()

    private var pendingUpdateInfo: AppUpdateInfo? = null
    private var metricsCalculator = DownloadMetricsCalculator()

    fun setAvailable(info: AppUpdateInfo) {
        pendingUpdateInfo = info
        mutableState.value = AppUpdateState.UpdateAvailable(info)
    }

    fun reset() {
        pendingUpdateInfo = null
        metricsCalculator = DownloadMetricsCalculator()
        mutableState.value = AppUpdateState.Idle
    }

    fun requirePendingUpdate(): AppUpdateInfo {
        return pendingUpdateInfo ?: error("No pending update available")
    }

    fun onDownloadProgress(info: AppUpdateInfo, file: File, downloadedBytes: Long, totalBytes: Long) {
        pendingUpdateInfo = info
        val metrics = metricsCalculator.recordSample(downloadedBytes, totalBytes, System.currentTimeMillis())
        mutableState.value = AppUpdateState.Downloading(
            info = info,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            progressPercent = metrics.progressPercent,
            bytesPerSecond = metrics.bytesPerSecond,
            etaSeconds = metrics.etaSeconds,
            filePath = file.absolutePath
        )
    }

    fun onDownloadCompleted(info: AppUpdateInfo, file: File) {
        pendingUpdateInfo = info
        mutableState.value = AppUpdateState.Downloaded(info, file.absolutePath)
    }

    fun onInstalling(info: AppUpdateInfo, file: File) {
        pendingUpdateInfo = info
        mutableState.value = AppUpdateState.Installing(info, file.absolutePath)
    }

    fun onCompleted(info: AppUpdateInfo) {
        pendingUpdateInfo = info
        mutableState.value = AppUpdateState.Completed(info, System.currentTimeMillis())
    }

    fun onDownloadFailed(message: String) {
        mutableState.value = AppUpdateState.Failed(message)
    }

    fun markChecking() {
        mutableState.value = AppUpdateState.Checking
    }

    fun requestInstallOrNotify(context: Context, info: AppUpdateInfo, file: File) {
        pendingUpdateInfo = info
        val installIntent = AppUpdateIntentFactory.createInstallerActivityIntent(context, file.absolutePath)
        installIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(installIntent) }
            .onFailure {
                NotificationManagerCompat.from(context).notify(
                    AppUpdateNotificationFactory.NOTIFICATION_ID,
                    AppUpdateNotificationFactory.buildCompletionNotification(context, info, file.absolutePath)
                )
            }
    }
}
