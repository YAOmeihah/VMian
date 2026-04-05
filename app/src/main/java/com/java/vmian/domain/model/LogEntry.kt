package com.java.vmian.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志条目数据类
 */
data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: LogType,
    val message: String
) {
    /**
     * 格式化时间戳为可读字符串
     */
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    /**
     * 获取完整的日志文本
     */
    fun getFullLogText(): String {
        return "[${getFormattedTime()}] ${type.prefix} $message"
    }
}

/**
 * 日志类型枚举
 */
enum class LogType(val prefix: String) {
    HEARTBEAT("心跳"),
    PAYMENT_ALIPAY("支付宝"),
    PAYMENT_WECHAT("微信"),
    NETWORK("网络"),
    CONFIG("配置"),
    SYSTEM("系统"),
    ERROR("错误")
}
