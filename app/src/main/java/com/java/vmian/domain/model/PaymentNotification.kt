package com.java.vmian.domain.model

/**
 * 支付通知数据模型
 * @param type 支付类型
 * @param amount 支付金额
 * @param timestamp 时间戳
 * @param packageName 应用包名
 * @param title 通知标题
 * @param content 通知内容
 */
data class PaymentNotification(
    val type: PaymentType,
    val amount: Double,
    val timestamp: Long,
    val packageName: String,
    val title: String,
    val content: String,
    val eventId: String
)

/**
 * 支付类型枚举
 * @param value 对应的数值（微信=1，支付宝=2）
 */
enum class PaymentType(val value: Int) {
    WECHAT(1),
    ALIPAY(2)
}
