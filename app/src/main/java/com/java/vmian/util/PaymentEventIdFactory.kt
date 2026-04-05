package com.java.vmian.util

/**
 * 根据通知稳定特征生成支付事件ID，避免同一条通知被重复上报时产生新的事件标识。
 */
object PaymentEventIdFactory {

    fun create(
        packageName: String,
        notificationKey: String,
        postTime: Long,
        title: String,
        content: String
    ): String {
        val fingerprint = listOf(
            packageName.trim(),
            notificationKey.trim(),
            postTime.toString(),
            title.trim(),
            content.trim()
        ).joinToString("|")

        return CryptoUtils.generateSha256(fingerprint)
    }
}
