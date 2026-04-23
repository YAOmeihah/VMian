package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentNotification
import com.java.vmian.domain.model.PaymentPushPayload
import com.java.vmian.domain.model.PaymentType
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.PaymentRepository
import com.java.vmian.util.CryptoUtils
import com.java.vmian.util.MoneyUtils
import java.util.UUID

/**
 * 支付相关业务用例
 */
class PaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val configRepository: ConfigRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val nonceFactory: () -> String = { UUID.randomUUID().toString() }
) {
    
    /**
     * 发送心跳
     */
    suspend fun sendHeartbeat(): ApiResponse<String> {
        val config = configRepository.getConfig() ?: return ApiResponse.Error("配置未设置")
        val timestamp = currentTimeMillis()
        val sign = CryptoUtils.generateMd5("$timestamp${config.monitorKey}")
        return paymentRepository.sendHeartbeat(config.terminalCode, timestamp, sign)
    }

    /**
     * 推送支付数据
     */
    suspend fun pushPayment(notification: PaymentNotification): ApiResponse<String> {
        val config = configRepository.getConfig() ?: return ApiResponse.Error("配置未设置")
        val timestamp = currentTimeMillis()
        val nonce = nonceFactory()
        val amountCents = MoneyUtils.toAmountCents(notification.amount)
        val signingText =
            "${config.terminalCode}|${notification.type.value}|$amountCents|$timestamp|$nonce|${notification.eventId}"
        val sign = CryptoUtils.generateHmacSha256(signingText, config.monitorKey)

        return paymentRepository.pushPayment(
            PaymentPushPayload(
                terminalCode = config.terminalCode,
                type = notification.type.value,
                amountCents = amountCents,
                timestamp = timestamp,
                nonce = nonce,
                eventId = notification.eventId,
                sign = sign
            )
        )
    }

    /**
     * 从通知内容中提取金额（兼容旧接口）
     */
    fun extractAmountFromContent(content: String): Double? {
        return extractAmountFromText(content)
    }

    /**
     * 从通知标题和内容中提取金额
     */
    fun extractAmountFromNotification(title: String, content: String): Double? {
        // 优先从标题中提取金额（支付宝通常在标题中包含金额）
        val titleAmount = extractAmountFromText(title)
        if (titleAmount != null) {
            return titleAmount
        }

        // 如果标题中没有找到，再从内容中提取
        return extractAmountFromText(content)
    }

    /**
     * 从文本中提取金额的核心方法
     */
    private fun extractAmountFromText(text: String): Double? {
        // 支持多种金额格式的正则表达式
        val amountPatterns = listOf(
            // 匹配 "收款0.01元"、"成功收款10.50元" 等格式
            Regex("""收款(\d+(?:\.\d{1,2})?)元"""),
            // 匹配 "付款￥10.50"、"转账￥100" 等格式
            Regex("""[付转]款?￥(\d+(?:\.\d{1,2})?)"""),
            // 匹配 "10.50元"、"100元" 等格式
            Regex("""(\d+(?:\.\d{1,2})?)元"""),
            // 匹配 "￥10.50"、"￥100" 等格式
            Regex("""￥(\d+(?:\.\d{1,2})?)"""),
            // 匹配纯数字（作为最后的备选方案）
            Regex("""(\d+\.\d{1,2})""")
        )

        // 按优先级尝试匹配
        for (pattern in amountPatterns) {
            val matchResult = pattern.find(text)
            if (matchResult != null) {
                val amountStr = matchResult.groupValues[1]
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    return amount
                }
            }
        }

        return null
    }

    /**
     * 识别支付类型
     */
    fun identifyPaymentType(packageName: String, title: String, content: String): PaymentType? {
        return when (packageName) {
            "com.eg.android.AlipayGphone" -> {
                identifyAlipayPayment(title, content)
            }
            "com.tencent.mm" -> {
                identifyWechatPayment(title, content)
            }
            else -> null
        }
    }

    /**
     * 识别支付宝收款通知
     */
    private fun identifyAlipayPayment(title: String, content: String): PaymentType? {
        val alipayKeywords = listOf(
            "通过扫码向你付款",
            "成功收款",
            "你已成功收款",
            "收款成功",
            "已收款",
            "收到转账",
            "收到付款"
        )

        // 检查标题或内容中是否包含支付宝收款关键字
        val hasKeyword = alipayKeywords.any { keyword ->
            title.contains(keyword) || content.contains(keyword)
        }

        return if (hasKeyword) PaymentType.ALIPAY else null
    }

    /**
     * 识别微信收款通知
     */
    private fun identifyWechatPayment(title: String, content: String): PaymentType? {
        val wechatTitles = listOf("微信支付", "微信收款助手", "微信收款商业版")
        return if (title in wechatTitles) PaymentType.WECHAT else null
    }
}
