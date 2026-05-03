package com.java.vmian.presentation.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepAliveControlUiModelTest {

    @Test
    fun from_marksMediaEnabledOnlyWhenPreferenceAndServiceAreBothActive() {
        val model = KeepAliveControlUiModel.from(
            mediaPreferenceEnabled = true,
            mediaServiceRunning = false,
            recentsHiddenPreferenceEnabled = false,
            overlayPermissionGranted = true,
            overlayPreferenceEnabled = false,
            overlayServiceRunning = false
        )

        assertFalse(model.media.isActive)
        assertEquals("已开启，等待服务启动", model.media.statusText)
    }

    @Test
    fun from_blocksOverlayToggleWhenPermissionIsMissing() {
        val model = KeepAliveControlUiModel.from(
            mediaPreferenceEnabled = false,
            mediaServiceRunning = false,
            recentsHiddenPreferenceEnabled = false,
            overlayPermissionGranted = false,
            overlayPreferenceEnabled = true,
            overlayServiceRunning = false
        )

        assertFalse(model.overlay.canToggle)
        assertEquals("未授权", model.overlay.permissionText)
        assertEquals("请先授予悬浮窗权限", model.overlay.statusText)
    }

    @Test
    fun from_reportsOverlayRunningWhenPermissionPreferenceAndServiceAreActive() {
        val model = KeepAliveControlUiModel.from(
            mediaPreferenceEnabled = false,
            mediaServiceRunning = false,
            recentsHiddenPreferenceEnabled = false,
            overlayPermissionGranted = true,
            overlayPreferenceEnabled = true,
            overlayServiceRunning = true
        )

        assertTrue(model.overlay.isActive)
        assertEquals("运行中", model.overlay.statusText)
        assertEquals("已授权", model.overlay.permissionText)
    }

    @Test
    fun from_reportsRecentsHiddenStateFromPreference() {
        val model = KeepAliveControlUiModel.from(
            mediaPreferenceEnabled = false,
            mediaServiceRunning = false,
            recentsHiddenPreferenceEnabled = true,
            overlayPermissionGranted = true,
            overlayPreferenceEnabled = false,
            overlayServiceRunning = false
        )

        assertTrue(model.recentsHidden.isChecked)
        assertTrue(model.recentsHidden.isActive)
        assertTrue(model.recentsHidden.canToggle)
        assertEquals("已隐藏", model.recentsHidden.statusText)
    }
}
