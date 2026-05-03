package com.java.vmian.presentation.ui.model

data class KeepAliveControlItemUiModel(
    val isChecked: Boolean,
    val isActive: Boolean,
    val canToggle: Boolean,
    val statusText: String,
    val permissionText: String? = null
)

data class KeepAliveControlUiModel(
    val media: KeepAliveControlItemUiModel,
    val recentsHidden: KeepAliveControlItemUiModel,
    val overlay: KeepAliveControlItemUiModel
) {
    companion object {
        fun from(
            mediaPreferenceEnabled: Boolean,
            mediaServiceRunning: Boolean,
            recentsHiddenPreferenceEnabled: Boolean,
            overlayPermissionGranted: Boolean,
            overlayPreferenceEnabled: Boolean,
            overlayServiceRunning: Boolean
        ): KeepAliveControlUiModel {
            return KeepAliveControlUiModel(
                media = KeepAliveControlItemUiModel(
                    isChecked = mediaPreferenceEnabled,
                    isActive = mediaPreferenceEnabled && mediaServiceRunning,
                    canToggle = true,
                    statusText = serviceStatusText(mediaPreferenceEnabled, mediaServiceRunning)
                ),
                recentsHidden = KeepAliveControlItemUiModel(
                    isChecked = recentsHiddenPreferenceEnabled,
                    isActive = recentsHiddenPreferenceEnabled,
                    canToggle = true,
                    statusText = if (recentsHiddenPreferenceEnabled) "已隐藏" else "已显示"
                ),
                overlay = KeepAliveControlItemUiModel(
                    isChecked = overlayPreferenceEnabled && overlayPermissionGranted,
                    isActive = overlayPermissionGranted && overlayPreferenceEnabled && overlayServiceRunning,
                    canToggle = overlayPermissionGranted,
                    statusText = when {
                        !overlayPermissionGranted -> "请先授予悬浮窗权限"
                        else -> serviceStatusText(overlayPreferenceEnabled, overlayServiceRunning)
                    },
                    permissionText = if (overlayPermissionGranted) "已授权" else "未授权"
                )
            )
        }

        private fun serviceStatusText(preferenceEnabled: Boolean, serviceRunning: Boolean): String {
            return when {
                preferenceEnabled && serviceRunning -> "运行中"
                preferenceEnabled -> "已开启，等待服务启动"
                else -> "已关闭"
            }
        }
    }
}
