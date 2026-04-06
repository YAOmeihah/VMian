package com.java.vmian.presentation.ui.model

import com.java.vmian.presentation.viewmodel.MainUiState

enum class MainScreenStage {
    Setup,
    PermissionsRequired,
    Ready
}

data class MainScreenUiModel(
    val stage: MainScreenStage,
    val headline: String,
    val supportingText: String,
    val primaryActionLabel: String
) {
    companion object {
        fun from(
            uiState: MainUiState,
            missingPermissionCount: Int
        ): MainScreenUiModel {
            return when {
                !uiState.isConfigured -> MainScreenUiModel(
                    stage = MainScreenStage.Setup,
                    headline = "先完成监控配置",
                    supportingText = "还没有完成监控配置，先扫码或手动填写服务器信息。",
                    primaryActionLabel = "立即配置"
                )

                missingPermissionCount > 0 -> MainScreenUiModel(
                    stage = MainScreenStage.PermissionsRequired,
                    headline = "补齐关键权限",
                    supportingText = "还缺 $missingPermissionCount 项关键权限，完成后监听和保活会更稳定。",
                    primaryActionLabel = "去完成权限配置"
                )

                else -> MainScreenUiModel(
                    stage = MainScreenStage.Ready,
                    headline = "监控端已准备就绪",
                    supportingText = "配置和关键权限都已就绪，可以直接开始检测服务状态。",
                    primaryActionLabel = "检测心跳"
                )
            }
        }
    }
}
