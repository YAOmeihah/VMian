package com.java.vmian.update

data class DownloadMetrics(
    val progressPercent: Int,
    val bytesPerSecond: Long,
    val etaSeconds: Long?
)

class DownloadMetricsCalculator {
    private var lastBytes: Long = 0L
    private var lastTimestampMs: Long = 0L
    private var smoothedBytesPerSecond: Double = 0.0

    fun recordSample(downloadedBytes: Long, totalBytes: Long, nowMs: Long): DownloadMetrics {
        val deltaBytes = downloadedBytes - lastBytes
        val deltaTimeMs = (nowMs - lastTimestampMs).coerceAtLeast(1L)
        val instantSpeed = deltaBytes * 1000.0 / deltaTimeMs
        smoothedBytesPerSecond =
            if (smoothedBytesPerSecond == 0.0) instantSpeed
            else (smoothedBytesPerSecond * 0.7) + (instantSpeed * 0.3)
        lastBytes = downloadedBytes
        lastTimestampMs = nowMs

        val progress = if (totalBytes <= 0) 0 else ((downloadedBytes * 100) / totalBytes).toInt()
        val remainingBytes = (totalBytes - downloadedBytes).coerceAtLeast(0L)
        val eta = if (smoothedBytesPerSecond <= 0.0) null else (remainingBytes / smoothedBytesPerSecond).toLong()
        return DownloadMetrics(progress, smoothedBytesPerSecond.toLong(), eta)
    }
}
