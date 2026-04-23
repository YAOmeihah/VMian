package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.AppUpdateState
import java.util.Locale

data class AppUpdateUiModel(
    val title: String,
    val body: String,
    val progressLabel: String? = null,
    val speedLabel: String? = null,
    val etaLabel: String? = null,
    val primaryActionLabel: String? = null,
    val secondaryActionLabel: String? = null
) {
    companion object {
        fun from(state: AppUpdateState): AppUpdateUiModel = when (state) {
            is AppUpdateState.UpdateAvailable -> AppUpdateUiModel(
                title = "发现新版本 ${state.info.versionName}",
                body = state.info.notes,
                primaryActionLabel = "立即更新",
                secondaryActionLabel = "忽略此版本"
            )
            is AppUpdateState.Downloading -> AppUpdateUiModel(
                title = "正在下载更新",
                body = "下载将在后台继续",
                progressLabel = "${state.progressPercent}%",
                speedLabel = formatSpeed(state.bytesPerSecond),
                etaLabel = formatEta(state.etaSeconds),
                primaryActionLabel = "后台继续",
                secondaryActionLabel = "取消下载"
            )
            is AppUpdateState.Downloaded -> AppUpdateUiModel(
                title = "下载完成",
                body = "安装包已准备好，可以立即安装",
                primaryActionLabel = "立即安装"
            )
            is AppUpdateState.Installing -> AppUpdateUiModel(
                title = "正在准备安装",
                body = "请在系统安装界面确认继续"
            )
            is AppUpdateState.Completed -> AppUpdateUiModel(
                title = "更新完成",
                body = "已完成新版本安装"
            )
            is AppUpdateState.Failed -> AppUpdateUiModel(
                title = "更新失败",
                body = state.message,
                primaryActionLabel = "重新下载"
            )
            AppUpdateState.Checking -> AppUpdateUiModel(
                title = "正在检查更新",
                body = "稍候将返回检查结果"
            )
            AppUpdateState.Idle -> AppUpdateUiModel(title = "", body = "")
        }

        private fun formatSpeed(bytesPerSecond: Long): String {
            if (bytesPerSecond <= 0) return "0 B/s"
            val megaBytesPerSecond = bytesPerSecond / 1024.0 / 1024.0
            return String.format(Locale.US, "%.1f MB/s", megaBytesPerSecond)
        }

        private fun formatEta(etaSeconds: Long?): String? {
            etaSeconds ?: return null
            return "约 ${etaSeconds}s"
        }
    }
}
