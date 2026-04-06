package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionStatus

data class PermissionOverviewUiModel(
    val progressText: String,
    val headline: String,
    val supportingText: String
) {
    companion object {
        fun from(status: PermissionStatus): PermissionOverviewUiModel {
            val requiredMissingCount = status.allPermissions.count {
                it.importance == PermissionImportance.REQUIRED && !it.isGranted
            }

            val progressText = "已完成 ${status.grantedCount}/${status.totalCount} 项"

            return when {
                requiredMissingCount > 0 -> PermissionOverviewUiModel(
                    progressText = progressText,
                    headline = "优先完成必需权限",
                    supportingText = "还差 $requiredMissingCount 项必需权限会直接影响收款监听。"
                )

                !status.allPermissionsGranted -> PermissionOverviewUiModel(
                    progressText = progressText,
                    headline = "核心功能可用",
                    supportingText = "必需权限已就绪，补齐推荐权限可以让后台运行更稳定。"
                )

                else -> PermissionOverviewUiModel(
                    progressText = progressText,
                    headline = "权限状态良好",
                    supportingText = "当前权限已经满足运行要求，后续只需要按需复查。"
                )
            }
        }
    }
}
