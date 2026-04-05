package com.java.vmian.domain.repository

import com.java.vmian.domain.model.PaymentConfig

/**
 * 配置管理Repository接口
 */
interface ConfigRepository {
    /**
     * 保存配置
     */
    suspend fun saveConfig(config: PaymentConfig)
    
    /**
     * 获取配置
     */
    suspend fun getConfig(): PaymentConfig?
    
    /**
     * 清除配置
     */
    suspend fun clearConfig()
}
