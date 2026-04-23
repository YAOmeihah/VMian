package com.java.vmian.update

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadMetricsCalculatorTest {

    @Test
    fun update_returnsStableSpeedAndEta_afterTwoSamples() {
        val calculator = DownloadMetricsCalculator()

        calculator.recordSample(downloadedBytes = 1_000_000L, totalBytes = 5_000_000L, nowMs = 1_000L)
        val metrics = calculator.recordSample(downloadedBytes = 2_000_000L, totalBytes = 5_000_000L, nowMs = 2_000L)

        assertEquals(1_000_000L, metrics.bytesPerSecond)
        assertEquals(3L, metrics.etaSeconds)
        assertEquals(40, metrics.progressPercent)
    }
}
