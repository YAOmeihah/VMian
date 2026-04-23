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
            .saveConfig("vpay.test", "terminal-a", "secret")

        assertTrue(result.isSuccess)
    }

    @Test
    fun saveConfig_persistsTerminalCodeAndMonitorKey() = runBlocking {
        val repository = InMemoryConfigRepository()
        val result = ConfigUseCase(repository)
            .saveConfig("https://example.com", "terminal-a", "secret")

        assertTrue(result.isSuccess)
        assertEquals(
            PaymentConfig("https://example.com", "terminal-a", "secret", true),
            repository.savedConfig
        )
    }

    @Test
    fun parseConfigFromQrCode_supportsTerminalQueryPayload() = runBlocking {
        val result = ConfigUseCase(InMemoryConfigRepository())
            .parseConfigFromQrCode("https://vpay.test/monitor-bind?terminalCode=terminal-a&monitorKey=dfe4bf400c3a4b2006e37eea36b49e7d")

        assertTrue(result.isSuccess)
        val payload = result.getOrThrow()
        assertEquals("https://vpay.test", payload.host)
        assertEquals("terminal-a", payload.terminalCode)
        assertEquals("dfe4bf400c3a4b2006e37eea36b49e7d", payload.monitorKey)
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
