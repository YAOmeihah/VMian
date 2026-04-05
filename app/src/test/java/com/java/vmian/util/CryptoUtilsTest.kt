package com.java.vmian.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoUtilsTest {
    @Test
    fun generateHmacSha256_returnsExpectedHexDigest() {
        val digest = CryptoUtils.generateHmacSha256(
            input = "2|123|1710000000000|nonce_123|evt_123",
            key = "secret"
        )

        assertEquals(
            "4c953a2a71c3bc222652b18e6b28db64f0b1a2bf86a354e54689d3a3977236bb",
            digest
        )
    }
}
