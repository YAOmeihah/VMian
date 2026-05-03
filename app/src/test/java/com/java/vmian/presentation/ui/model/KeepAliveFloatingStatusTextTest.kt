package com.java.vmian.presentation.ui.model

import org.junit.Assert.assertEquals
import org.junit.Test

class KeepAliveFloatingStatusTextTest {

    @Test
    fun overlayStatus_reportsHeartbeatOnlyWhenMediaKeepAliveIsEnabled() {
        val text = KeepAliveFloatingStatusText.overlay(
            mediaEnabled = true,
            lastHeartbeatAtMillis = null,
            nowMillis = 60_000L
        )

        assertEquals("VMian 运行中", text.title)
        assertEquals("心跳 --", text.subtitle)
    }

    @Test
    fun overlayStatus_reportsHeartbeatOnlyWhenMediaKeepAliveIsDisabled() {
        val text = KeepAliveFloatingStatusText.overlay(
            mediaEnabled = false,
            lastHeartbeatAtMillis = 45_000L,
            nowMillis = 60_000L
        )

        assertEquals("VMian 运行中", text.title)
        assertEquals("心跳 15秒前", text.subtitle)
    }

    @Test
    fun overlayStatus_reportsMinuteLevelHeartbeatAge() {
        val text = KeepAliveFloatingStatusText.overlay(
            mediaEnabled = true,
            lastHeartbeatAtMillis = 60_000L,
            nowMillis = 240_000L
        )

        assertEquals("心跳 3分钟前", text.subtitle)
    }
}
