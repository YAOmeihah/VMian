package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.presentation.viewmodel.MainUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenUiModelTest {

    @Test
    fun from_returnsSetupState_whenConfigMissing() {
        val model = MainScreenUiModel.from(
            uiState = MainUiState(
                config = null,
                isConfigured = false
            ),
            missingPermissionCount = 0
        )

        assertEquals(MainScreenStage.Setup, model.stage)
        assertEquals("立即配置", model.primaryActionLabel)
        assertEquals("还没有完成监控配置，先扫码或手动填写服务器信息。", model.supportingText)
    }

    @Test
    fun from_returnsPermissionState_whenPermissionsMissing() {
        val model = MainScreenUiModel.from(
            uiState = MainUiState(
                config = PaymentConfig(
                    host = "https://vmq.example",
                    terminalCode = "terminal-a",
                    monitorKey = "secret",
                    isConfigured = true
                ),
                isConfigured = true
            ),
            missingPermissionCount = 2
        )

        assertEquals(MainScreenStage.PermissionsRequired, model.stage)
        assertEquals("去完成权限配置", model.primaryActionLabel)
        assertEquals("还缺 2 项关键权限，完成后监听和保活会更稳定。", model.supportingText)
    }

    @Test
    fun from_returnsReadyState_whenConfiguredAndPermissionsReady() {
        val model = MainScreenUiModel.from(
            uiState = MainUiState(
                config = PaymentConfig(
                    host = "https://vmq.example",
                    terminalCode = "terminal-a",
                    monitorKey = "secret",
                    isConfigured = true
                ),
                isConfigured = true
            ),
            missingPermissionCount = 0
        )

        assertEquals(MainScreenStage.Ready, model.stage)
        assertEquals("检测心跳", model.primaryActionLabel)
        assertEquals("配置和关键权限都已就绪，可以直接开始检测服务状态。", model.supportingText)
    }
}
