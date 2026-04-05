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
            PaymentConfig("https://example.com", "secret", true)
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
                type = 2,
                amountCents = 1023L,
                timestamp = 1710000000000L,
                nonce = "nonce_123",
                eventId = "evt_123",
                sign = "725b13fe5235b39bb0051647a4e9f1edf3732839002303b3f71f06423974744a"
            ),
            paymentRepository.lastPayload
        )
    }

    private class RecordingPaymentRepository : PaymentRepository {
        var lastPayload: PaymentPushPayload? = null

        override suspend fun sendHeartbeat(timestamp: Long, sign: String): ApiResponse<String> {
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
