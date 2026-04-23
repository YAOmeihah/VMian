package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.MonitorConfigPayload
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.repository.ConfigRepository
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * 配置相关业务用例
 */
class ConfigUseCase(
    private val configRepository: ConfigRepository
) {
    
    /**
     * 保存配置
     */
    suspend fun saveConfig(host: String, terminalCode: String, monitorKey: String): Result<Unit> {
        return try {
            require(host.isNotBlank()) { "服务器地址不能为空" }
            require(terminalCode.isNotBlank()) { "终端编码不能为空" }
            require(monitorKey.isNotBlank()) { "监控密钥不能为空" }
            val config = PaymentConfig(
                host = host.trim(),
                terminalCode = terminalCode.trim(),
                monitorKey = monitorKey.trim(),
                isConfigured = true
            )
            configRepository.saveConfig(config)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取配置
     */
    suspend fun getConfig(): PaymentConfig? {
        return configRepository.getConfig()
    }

    /**
     * 解析二维码配置
     * 格式: host/monitor-bind?terminalCode=xxx&monitorKey=xxx
     */
    fun parseConfigFromQrCode(qrContent: String): Result<MonitorConfigPayload> {
        val content = qrContent.trim()

        return try {
            parseQueryPayload(content)
                ?: parsePathPayload(content)
                ?: throw IllegalArgumentException("二维码格式错误")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 清除配置
     */
    suspend fun clearConfig(): Result<Unit> {
        return try {
            configRepository.clearConfig()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseQueryPayload(content: String): Result<MonitorConfigPayload>? {
        val queryIndex = content.indexOf('?')
        if (queryIndex <= 0 || queryIndex == content.lastIndex) return null

        val rawBase = content.substring(0, queryIndex).trimEnd('/')
        val host = rawBase.removeSuffix("/monitor-bind")
        val params = parseQueryParams(content.substring(queryIndex + 1))
        val terminalCode = params["terminalCode"] ?: params["terminal_code"]
        val monitorKey = params["monitorKey"] ?: params["monitor_key"]

        if (host.isBlank() || terminalCode.isNullOrBlank() || monitorKey.isNullOrBlank()) {
            return null
        }

        return Result.success(
            MonitorConfigPayload(
                host = host,
                terminalCode = terminalCode.trim(),
                monitorKey = monitorKey.trim()
            )
        )
    }

    private fun parsePathPayload(content: String): Result<MonitorConfigPayload>? {
        val normalized = content.trimEnd('/')
        val monitorKeySeparator = normalized.lastIndexOf('/')
        if (monitorKeySeparator <= 0 || monitorKeySeparator == normalized.lastIndex) return null

        val hostAndTerminal = normalized.substring(0, monitorKeySeparator)
        val terminalSeparator = hostAndTerminal.lastIndexOf('/')
        if (terminalSeparator <= 0 || terminalSeparator == hostAndTerminal.lastIndex) return null

        val host = hostAndTerminal.substring(0, terminalSeparator)
        val terminalCode = decode(hostAndTerminal.substring(terminalSeparator + 1))
        val monitorKey = decode(normalized.substring(monitorKeySeparator + 1))

        if (host.isBlank() || terminalCode.isBlank() || monitorKey.isBlank()) return null

        return Result.success(
            MonitorConfigPayload(
                host = host,
                terminalCode = terminalCode,
                monitorKey = monitorKey
            )
        )
    }

    private fun parseQueryParams(query: String): Map<String, String> {
        return query.split('&')
            .mapNotNull { item ->
                if (item.isBlank()) return@mapNotNull null
                val separator = item.indexOf('=')
                if (separator <= 0) return@mapNotNull null
                decode(item.substring(0, separator)) to decode(item.substring(separator + 1))
            }
            .toMap()
    }

    private fun decode(value: String): String {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.name())
    }
}
