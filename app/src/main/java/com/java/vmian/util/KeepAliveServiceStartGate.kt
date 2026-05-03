package com.java.vmian.util

class KeepAliveServiceStartGate(
    private val minIntervalMillis: Long
) {
    private val lastStartTimes = mutableMapOf<String, Long>()

    @Synchronized
    fun shouldAllowStart(key: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val lastStartTime = lastStartTimes[key]
        if (lastStartTime != null && nowMillis - lastStartTime < minIntervalMillis) {
            return false
        }
        lastStartTimes[key] = nowMillis
        return true
    }
}

object SharedKeepAliveServiceStartGate {
    private val gate = KeepAliveServiceStartGate(minIntervalMillis = 500)

    fun shouldAllowStart(key: String): Boolean = gate.shouldAllowStart(key)
}
