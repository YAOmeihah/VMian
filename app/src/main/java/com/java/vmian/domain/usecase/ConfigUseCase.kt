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
    suspend fun saveConfig(host: String, key: String): Result<Unit> {
        return try {
            val config = PaymentConfig(host, key, true)
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
     * 格式: host/key
     */
    fun parseConfigFromQrCode(qrContent: String): Result<Pair<String, String>> {
        val parts = qrContent.split("/")
        return if (parts.size == 2) {
            Result.success(Pair(parts[0], parts[1]))
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
