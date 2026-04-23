package com.java.vmian.domain.model

/**
 * 支付配置数据模型
 * @param host 服务器地址
 * @param terminalCode 终端编码
 * @param monitorKey 监控密钥
 * @param isConfigured 是否已配置
 */
data class PaymentConfig(
    val host: String,
    val terminalCode: String,
    val monitorKey: String,
    val isConfigured: Boolean = false
)
