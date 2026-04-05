package com.java.vmian.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class SecureEndpointBuilderTest {

    @Test
    fun build_acceptsExplicitHttpsHost() {
        assertEquals(
            "https://example.com/appPush",
            SecureEndpointBuilder.build(host = "https://example.com", path = "/appPush")
        )
    }

    @Test
    fun build_acceptsExplicitHttpHost() {
        assertEquals(
            "http://example.com/appPush",
            SecureEndpointBuilder.build(host = "http://example.com", path = "/appPush")
        )
    }

    @Test
    fun build_addsHttpToHostWithoutScheme() {
        assertEquals(
            "http://example.com/appPush",
            SecureEndpointBuilder.build(host = "example.com", path = "/appPush")
        )
    }
}
