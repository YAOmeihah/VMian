package com.java.vmian.presentation.ui.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogPanelLayoutTest {

    @Test
    fun cardHeight_isClampedWithinSafeRange() {
        assertEquals(320, LogPanelLayout.resolveCardHeightDp(screenHeightDp = 520))
        assertEquals(460, LogPanelLayout.resolveCardHeightDp(screenHeightDp = 1000))
        assertEquals(520, LogPanelLayout.resolveCardHeightDp(screenHeightDp = 1400))
    }

    @Test
    fun bodyHeight_staysBoundedInsideCard() {
        val cardHeightDp = LogPanelLayout.resolveCardHeightDp(screenHeightDp = 1000)
        val bodyHeightDp = LogPanelLayout.resolveBodyHeightDp(cardHeightDp)

        assertTrue(bodyHeightDp >= 200)
        assertTrue(bodyHeightDp < cardHeightDp)
    }
}
