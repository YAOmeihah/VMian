package com.java.vmian.data.repository

import com.java.vmian.data.remote.PaymentApiService
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.PaymentRepository

/**
 * 支付Repository实现类
 */
class PaymentRepositoryImpl(
    private val apiService: PaymentApiService,
    private val configRepository: ConfigRepository
) : PaymentRepository {

    override suspend fun sendHeartbeat(timestamp: Long, sign: String): ApiResponse<String> {
        return try {
            val config = configRepository.getConfig()
            if (config == null) {
                return ApiResponse.Error("配置未设置")
            }

            // 确保host包含协议，构建完整URL
            val baseUrl = if (config.host.startsWith("http://") || config.host.startsWith("https://")) {
                config.host
            } else {
                "http://${config.host}"
            }
            val url = "$baseUrl/appHeart"

            val response = apiService.sendHeartbeat(url, timestamp, sign)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccess()) {
                    ApiResponse.Success(body.message)
                } else {
                    ApiResponse.Error("心跳失败: ${body?.getErrorMessage() ?: "未知错误"}")
                }
            } else {
                ApiResponse.Error("心跳失败: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error("网络错误: ${e.message}")
        }
    }

    override suspend fun pushPayment(type: Int, price: Double, timestamp: Long, sign: String): ApiResponse<String> {
        return try {
            val config = configRepository.getConfig()
            if (config == null) {
                return ApiResponse.Error("配置未设置")
            }

            // 确保host包含协议，构建完整URL
            val baseUrl = if (config.host.startsWith("http://") || config.host.startsWith("https://")) {
                config.host
            } else {
                "http://${config.host}"
            }
            val url = "$baseUrl/appPush"

            val response = apiService.pushPayment(url, timestamp, type, price, sign)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccess()) {
                    ApiResponse.Success(body.message)
                } else {
                    ApiResponse.Error("推送失败: ${body?.getErrorMessage() ?: "未知错误"}")
                }
            } else {
                ApiResponse.Error("推送失败: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error("网络错误: ${e.message}")
        }
    }
}
