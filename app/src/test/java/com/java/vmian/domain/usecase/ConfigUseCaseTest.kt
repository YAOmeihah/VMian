package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.repository.ConfigRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigUseCaseTest {

    @Test
    fun saveConfig_acceptsHostWithoutScheme() = runBlocking {
        val result = ConfigUseCase(InMemoryConfigRepository())
            .saveConfig("vpay.test", "secret")

        assertTrue(result.isSuccess)
    }

    @Test
    fun saveConfig_persistsMonitorKey() = runBlocking {
        val repository = InMemoryConfigRepository()
        val result = ConfigUseCase(repository)
            .saveConfig("https://example.com", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            PaymentConfig("https://example.com", "secret", true),
            repository.savedConfig
        )
    }

    @Test
    fun parseConfigFromQrCode_supportsHttpsHostAndMonitorKey() = runBlocking {
        val result = ConfigUseCase(InMemoryConfigRepository())
            .parseConfigFromQrCode("https://vpay.test/dfe4bf400c3a4b2006e37eea36b49e7d")

        assertTrue(result.isSuccess)
        assertEquals(
            Pair("https://vpay.test", "dfe4bf400c3a4b2006e37eea36b49e7d"),
            result.getOrThrow()
        )
    }

    private class InMemoryConfigRepository : ConfigRepository {
        var savedConfig: PaymentConfig? = null

        override suspend fun saveConfig(config: PaymentConfig) {
            savedConfig = config
        }

        override suspend fun getConfig(): PaymentConfig? = savedConfig

        override suspend fun clearConfig() = Unit
    }
}
