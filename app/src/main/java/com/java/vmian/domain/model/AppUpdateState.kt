package com.java.vmian.domain.model

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data class UpdateAvailable(val info: AppUpdateInfo) : AppUpdateState
    data class Downloading(
        val info: AppUpdateInfo,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val progressPercent: Int,
        val bytesPerSecond: Long,
        val etaSeconds: Long?,
        val filePath: String
    ) : AppUpdateState

    data class Downloaded(val info: AppUpdateInfo, val filePath: String) : AppUpdateState
    data class Installing(val info: AppUpdateInfo, val filePath: String) : AppUpdateState
    data class Completed(val info: AppUpdateInfo, val installedAt: Long) : AppUpdateState
    data class Failed(val message: String, val recoverable: Boolean = true) : AppUpdateState
}
