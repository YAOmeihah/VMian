package com.java.vmian.data.repository

import com.java.vmian.data.remote.PaymentApiService
import com.java.vmian.data.remote.dto.PushPaymentRequestDto
import com.java.vmian.data.remote.dto.VmqApiResponse
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.model.PaymentPushPayload
import com.java.vmian.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class PaymentRepositoryImplTest {

    @Test
    fun sendHeartbeat_returnsSpecificMessageForRateLimit() = runBlocking {
        val repository = PaymentRepositoryImpl(
            apiService = FakePaymentApiService(
                heartbeatResponse = errorResponse(
                    429,
                    """{"code":-1,"msg":"请求过于频繁，请稍后重试","data":{"retryAfter":10}}"""
                )
            ),
            configRepository = FixedConfigRepository()
        )

        val result = repository.sendHeartbeat("terminal-a", 1710000000000L, "sign")

        assertEquals(
            ApiResponse.Error<String>("心跳请求过于频繁，等待下次心跳", 429),
            result
        )
    }

    @Test
    fun pushPayment_returnsManualCheckMessageForRateLimit() = runBlocking {
        val repository = PaymentRepositoryImpl(
            apiService = FakePaymentApiService(
                pushResponse = errorResponse(
                    429,
                    """{"code":-1,"msg":"请求过于频繁，请稍后重试","data":{"retryAfter":10}}"""
                )
            ),
            configRepository = FixedConfigRepository()
        )

        val result = repository.pushPayment(
            PaymentPushPayload(
                terminalCode = "terminal-a",
                type = 2,
                amountCents = 1000,
                timestamp = 1710000000000L,
                nonce = "nonce",
                eventId = "event",
                sign = "sign"
            )
        )

        assertEquals(
            ApiResponse.Error<String>("支付推送被服务端限流，请人工核对订单", 429),
            result
        )
    }

    @Test
    fun pushPayment_usesServerMessageForNonRateLimitHttpErrors() = runBlocking {
        val repository = PaymentRepositoryImpl(
            apiService = FakePaymentApiService(
                pushResponse = errorResponse(
                    500,
                    """{"code":-1,"msg":"服务器维护中","data":null}"""
                )
            ),
            configRepository = FixedConfigRepository()
        )

        val result = repository.pushPayment(
            PaymentPushPayload(
                terminalCode = "terminal-a",
                type = 2,
                amountCents = 1000,
                timestamp = 1710000000000L,
                nonce = "nonce",
                eventId = "event",
                sign = "sign"
            )
        )

        assertEquals(
            ApiResponse.Error<String>("推送失败: 服务器维护中", 500),
            result
        )
    }

    private fun errorResponse(code: Int, body: String): Response<VmqApiResponse<Any?>> {
        return Response.error(
            code,
            body.toResponseBody("application/json".toMediaType())
        )
    }

    private class FakePaymentApiService(
        private val heartbeatResponse: Response<VmqApiResponse<Any?>> = Response.success(
            VmqApiResponse(1, "success", null)
        ),
        private val pushResponse: Response<VmqApiResponse<Any?>> = Response.success(
            VmqApiResponse(1, "success", null)
        )
    ) : PaymentApiService {
        override suspend fun sendHeartbeat(
            url: String,
            terminalCode: String,
            timestamp: Long,
            sign: String
        ): Response<VmqApiResponse<Any?>> {
            return heartbeatResponse
        }

        override suspend fun pushPayment(
            url: String,
            body: PushPaymentRequestDto
        ): Response<VmqApiResponse<Any?>> {
            return pushResponse
        }
    }

    private class FixedConfigRepository : ConfigRepository {
        override suspend fun saveConfig(config: PaymentConfig) = Unit
        override suspend fun getConfig(): PaymentConfig {
            return PaymentConfig("https://example.com", "terminal-a", "secret", true)
        }
        override suspend fun clearConfig() = Unit
    }
}
