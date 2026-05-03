package com.java.vmian.presentation.ui.model

data class KeepAliveFloatingStatusTextModel(
    val title: String,
    val subtitle: String
)

object KeepAliveFloatingStatusText {
    fun overlay(
        mediaEnabled: Boolean,
        lastHeartbeatAtMillis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): KeepAliveFloatingStatusTextModel {
        return KeepAliveFloatingStatusTextModel(
            title = "VMian 运行中",
            subtitle = "心跳 ${heartbeatAgeText(lastHeartbeatAtMillis, nowMillis)}"
        )
    }

    private fun heartbeatAgeText(lastHeartbeatAtMillis: Long?, nowMillis: Long): String {
        if (lastHeartbeatAtMillis == null || lastHeartbeatAtMillis <= 0L) return "--"
        val ageSeconds = ((nowMillis - lastHeartbeatAtMillis).coerceAtLeast(0L) / 1_000L)
        return when {
            ageSeconds < 5L -> "刚刚"
            ageSeconds < 60L -> "${ageSeconds}秒前"
            ageSeconds < 3_600L -> "${ageSeconds / 60L}分钟前"
            else -> "${ageSeconds / 3_600L}小时前"
        }
    }
}
