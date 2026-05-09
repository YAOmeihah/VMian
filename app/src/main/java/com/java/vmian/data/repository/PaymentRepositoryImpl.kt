package com.java.vmian.data.repository

import com.google.gson.JsonParser
import com.java.vmian.data.remote.PaymentApiService
import com.java.vmian.data.remote.SecureEndpointBuilder
import com.java.vmian.data.remote.dto.PushPaymentRequestDto
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentPushPayload
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.PaymentRepository
import retrofit2.Response

/**
 * 支付Repository实现类
 */
class PaymentRepositoryImpl(
    private val apiService: PaymentApiService,
    private val configRepository: ConfigRepository
) : PaymentRepository {

    override suspend fun sendHeartbeat(
        terminalCode: String,
        timestamp: Long,
        sign: String
    ): ApiResponse<String> {
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

            val response = apiService.sendHeartbeat(url, terminalCode, timestamp, sign)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccess()) {
                    ApiResponse.Success(body.message)
                } else {
                    ApiResponse.Error("心跳失败: ${body?.getErrorMessage() ?: "未知错误"}")
                }
            } else {
                ApiResponse.Error(
                    message = httpErrorMessage(
                        response = response,
                        prefix = "心跳失败",
                        rateLimitedMessage = "心跳请求过于频繁，等待下次心跳"
                    ),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            ApiResponse.Error("网络错误: ${e.message}")
        }
    }

    override suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String> {
        return try {
            val config = configRepository.getConfig()
            if (config == null) {
                return ApiResponse.Error("配置未设置")
            }

            val url = SecureEndpointBuilder.build(config.host, "/appPush")
            val request = PushPaymentRequestDto(
                terminalCode = payload.terminalCode,
                type = payload.type,
                amountCents = payload.amountCents,
                ts = payload.timestamp,
                nonce = payload.nonce,
                eventId = payload.eventId,
                sign = payload.sign
            )

            val response = apiService.pushPayment(url, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.isSuccess()) {
                    ApiResponse.Success(body.message)
                } else {
                    ApiResponse.Error("推送失败: ${body?.getErrorMessage() ?: "未知错误"}")
                }
            } else {
                ApiResponse.Error(
                    message = httpErrorMessage(
                        response = response,
                        prefix = "推送失败",
                        rateLimitedMessage = "支付推送被服务端限流，请人工核对订单"
                    ),
                    code = response.code()
                )
            }
        } catch (e: IllegalArgumentException) {
            ApiResponse.Error(e.message ?: "服务器地址必须使用 HTTPS")
        } catch (e: Exception) {
            ApiResponse.Error("网络错误: ${e.message}")
        }
    }

    private fun httpErrorMessage(
        response: Response<*>,
        prefix: String,
        rateLimitedMessage: String
    ): String {
        if (response.code() == 429) {
            return rateLimitedMessage
        }

        val serverMessage = response.errorBody()
            ?.string()
            ?.let(::parseServerMessage)
            ?.takeIf { it.isNotBlank() }

        return if (serverMessage != null) {
            "$prefix: $serverMessage"
        } else {
            "$prefix: HTTP ${response.code()}"
        }
    }

    private fun parseServerMessage(errorBody: String): String? {
        return runCatching {
            JsonParser().parse(errorBody)
                .asJsonObject
                .get("msg")
                ?.getAsString()
        }.getOrNull()
    }
}
