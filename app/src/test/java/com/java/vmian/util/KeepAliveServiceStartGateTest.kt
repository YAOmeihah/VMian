package com.java.vmian.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeepAliveServiceStartGateTest {

    @Test
    fun shouldAllowStart_throttlesRapidRepeatedRequestsPerKey() {
        val gate = KeepAliveServiceStartGate(minIntervalMillis = 500)

        assertTrue(gate.shouldAllowStart("media", nowMillis = 1_000))
        assertFalse(gate.shouldAllowStart("media", nowMillis = 1_200))
        assertTrue(gate.shouldAllowStart("media", nowMillis = 1_500))
    }

    @Test
    fun shouldAllowStart_tracksDifferentKeysIndependently() {
        val gate = KeepAliveServiceStartGate(minIntervalMillis = 500)

        assertTrue(gate.shouldAllowStart("media", nowMillis = 1_000))
        assertTrue(gate.shouldAllowStart("overlay", nowMillis = 1_100))
    }
}
