package com.java.vmian.domain.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * 推送日志条目数据类
 */
data class PushLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: PushLogType,
    val paymentType: String, // 支付类型：支付宝、微信
    val amount: Double,
    val message: String,
    val isSuccess: Boolean
) {
    /**
     * 格式化时间戳为可读字符串
     */
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    /**
     * 获取简化时间格式
     */
    fun getSimpleFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    /**
     * 获取完整的推送日志文本
     */
    fun getFullLogText(): String {
        return "[${getFormattedTime()}] ${type.prefix} $paymentType ¥$amount - $message"
    }
}

/**
 * 推送日志类型枚举
 */
enum class PushLogType(val prefix: String) {
    SUCCESS("成功"),
    FAILED("失败")
}
