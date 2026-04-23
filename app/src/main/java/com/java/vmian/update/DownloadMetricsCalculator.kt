package com.java.vmian.update

data class DownloadMetrics(
    val progressPercent: Int,
    val bytesPerSecond: Long,
    val etaSeconds: Long?
)

class DownloadMetricsCalculator(
    private val minMetricsRefreshIntervalMs: Long = 900L
) {
    private var windowStartBytes: Long = 0L
    private var windowStartTimestampMs: Long = 0L
    private var smoothedBytesPerSecond: Double = 0.0
    private var lastEtaSeconds: Long? = null

    fun recordSample(downloadedBytes: Long, totalBytes: Long, nowMs: Long): DownloadMetrics {
        val progress = if (totalBytes <= 0) 0 else ((downloadedBytes * 100) / totalBytes).toInt()
        if (windowStartTimestampMs == 0L) {
            windowStartBytes = downloadedBytes
            windowStartTimestampMs = nowMs
            return DownloadMetrics(progress, bytesPerSecond = 0L, etaSeconds = null)
        }

        val shouldRefreshMetrics =
            nowMs - windowStartTimestampMs >= minMetricsRefreshIntervalMs ||
                downloadedBytes >= totalBytes

        if (shouldRefreshMetrics) {
            val deltaBytes = downloadedBytes - windowStartBytes
            val deltaTimeMs = (nowMs - windowStartTimestampMs).coerceAtLeast(1L)
            val instantSpeed = deltaBytes * 1000.0 / deltaTimeMs
            smoothedBytesPerSecond =
                if (smoothedBytesPerSecond == 0.0) instantSpeed
                else (smoothedBytesPerSecond * 0.7) + (instantSpeed * 0.3)

            val remainingBytes = (totalBytes - downloadedBytes).coerceAtLeast(0L)
            lastEtaSeconds =
                if (smoothedBytesPerSecond <= 0.0) null
                else (remainingBytes / smoothedBytesPerSecond).toLong()

            windowStartBytes = downloadedBytes
            windowStartTimestampMs = nowMs
        }

        return DownloadMetrics(progress, smoothedBytesPerSecond.toLong(), lastEtaSeconds)
    }
}
