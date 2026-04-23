package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.model.PaymentNotification
import com.java.vmian.domain.model.PaymentPushPayload
import com.java.vmian.domain.model.PaymentType
import com.java.vmian.domain.repository.ConfigRepository
import com.java.vmian.domain.repository.PaymentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class PaymentUseCaseTest {

    @Test
    fun pushPayment_buildsSignedPayloadWithNonceAndEventId() = runBlocking {
        val paymentRepository = RecordingPaymentRepository()
        val configRepository = FixedConfigRepository(
            PaymentConfig("https://example.com", "terminal-a", "secret", true)
        )
        val useCase = PaymentUseCase(
            paymentRepository = paymentRepository,
            configRepository = configRepository,
            currentTimeMillis = { 1710000000000L },
            nonceFactory = { "nonce_123" }
        )

        val notification = PaymentNotification(
            type = PaymentType.ALIPAY,
            amount = 10.23,
            timestamp = 1709999999000L,
            packageName = "com.eg.android.AlipayGphone",
            title = "收款成功",
            content = "收款10.23元",
            eventId = "evt_123"
        )

        useCase.pushPayment(notification)

        assertEquals(
            PaymentPushPayload(
                terminalCode = "terminal-a",
                type = 2,
                amountCents = 1023L,
                timestamp = 1710000000000L,
                nonce = "nonce_123",
                eventId = "evt_123",
                sign = "e36b63cd0318113fec11a54fe766d11e37be005bc465dea012ee40edf06cf6d2"
            ),
            paymentRepository.lastPayload
        )
    }

    @Test
    fun sendHeartbeat_usesTerminalCodeAndMonitorKeySignature() = runBlocking {
        val paymentRepository = RecordingPaymentRepository()
        val configRepository = FixedConfigRepository(
            PaymentConfig("https://example.com", "terminal-a", "secret", true)
        )
        val useCase = PaymentUseCase(
            paymentRepository = paymentRepository,
            configRepository = configRepository,
            currentTimeMillis = { 1710000000000L },
            nonceFactory = { "nonce_123" }
        )

        useCase.sendHeartbeat()

        assertEquals("terminal-a", paymentRepository.lastHeartbeatTerminalCode)
        assertEquals(1710000000000L, paymentRepository.lastHeartbeatTimestamp)
        assertEquals("6ff502c2e0fa13a9b5440a8ba44ff820", paymentRepository.lastHeartbeatSign)
    }

    private class RecordingPaymentRepository : PaymentRepository {
        var lastPayload: PaymentPushPayload? = null
        var lastHeartbeatTerminalCode: String? = null
        var lastHeartbeatTimestamp: Long? = null
        var lastHeartbeatSign: String? = null

        override suspend fun sendHeartbeat(
            terminalCode: String,
            timestamp: Long,
            sign: String
        ): ApiResponse<String> {
            lastHeartbeatTerminalCode = terminalCode
            lastHeartbeatTimestamp = timestamp
            lastHeartbeatSign = sign
            return ApiResponse.Success("ok")
        }

        override suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String> {
            lastPayload = payload
            return ApiResponse.Success("ok")
        }
    }

    private class FixedConfigRepository(
        private val config: PaymentConfig
    ) : ConfigRepository {
        override suspend fun saveConfig(config: PaymentConfig) = Unit
        override suspend fun getConfig(): PaymentConfig = config
        override suspend fun clearConfig() = Unit
    }
}
