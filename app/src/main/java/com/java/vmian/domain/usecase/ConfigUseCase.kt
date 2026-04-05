package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.repository.ConfigRepository

/**
 * 配置相关业务用例
 */
class ConfigUseCase(
    private val configRepository: ConfigRepository
) {
    
    /**
     * 保存配置
     */
    suspend fun saveConfig(host: String, monitorKey: String): Result<Unit> {
        return try {
            require(monitorKey.isNotBlank()) { "监控密钥不能为空" }
            val config = PaymentConfig(host.trim(), monitorKey.trim(), true)
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
     * 格式: host/monitorKey
     */
    fun parseConfigFromQrCode(qrContent: String): Result<Pair<String, String>> {
        val content = qrContent.trim()
        val separatorIndex = content.lastIndexOf('/')

        return if (separatorIndex in 1 until content.lastIndex) {
            val host = content.substring(0, separatorIndex)
            val monitorKey = content.substring(separatorIndex + 1)
            Result.success(Pair(host, monitorKey))
        } else {
            Result.failure(IllegalArgumentException("二维码格式错误"))
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
}
