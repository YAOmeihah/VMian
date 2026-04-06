package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.domain.model.PermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionOverviewUiModelTest {

    @Test
    fun from_prioritizesRequiredPermissions_whenIncomplete() {
        val status = PermissionStatus(
            allPermissions = listOf(
                PermissionInfo(
                    id = "listener",
                    name = "通知监听",
                    description = "监听收款通知",
                    importance = PermissionImportance.REQUIRED,
                    isGranted = false,
                    canRequest = false
                ),
                PermissionInfo(
                    id = "notifications",
                    name = "通知权限",
                    description = "显示前台服务通知",
                    importance = PermissionImportance.REQUIRED,
                    isGranted = true
                ),
                PermissionInfo(
                    id = "battery",
                    name = "电池优化白名单",
                    description = "降低后台被杀概率",
                    importance = PermissionImportance.RECOMMENDED,
                    isGranted = false,
                    canRequest = false
                )
            ),
            requiredPermissionsGranted = false,
            recommendedPermissionsGranted = false,
            allPermissionsGranted = false
        )

        val model = PermissionOverviewUiModel.from(status)

        assertEquals("已完成 1/3 项", model.progressText)
        assertEquals("优先完成必需权限", model.headline)
        assertEquals("还差 1 项必需权限会直接影响收款监听。", model.supportingText)
    }

    @Test
    fun from_returnsHealthySummary_whenAllPermissionsGranted() {
        val status = PermissionStatus(
            allPermissions = listOf(
                PermissionInfo(
                    id = "listener",
                    name = "通知监听",
                    description = "监听收款通知",
                    importance = PermissionImportance.REQUIRED,
                    isGranted = true,
                    canRequest = false
                ),
                PermissionInfo(
                    id = "notifications",
                    name = "通知权限",
                    description = "显示前台服务通知",
                    importance = PermissionImportance.REQUIRED,
                    isGranted = true
                )
            ),
            requiredPermissionsGranted = true,
            recommendedPermissionsGranted = true,
            allPermissionsGranted = true
        )

        val model = PermissionOverviewUiModel.from(status)

        assertEquals("已完成 2/2 项", model.progressText)
        assertEquals("权限状态良好", model.headline)
        assertEquals("当前权限已经满足运行要求，后续只需要按需复查。", model.supportingText)
    }
}
