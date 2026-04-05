package com.java.vmian.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilsTest {
    @Test
    fun toAmountCents_roundsToFenUsingHalfUp() {
        assertEquals(1L, MoneyUtils.toAmountCents(0.01))
        assertEquals(100L, MoneyUtils.toAmountCents(1.00))
        assertEquals(1023L, MoneyUtils.toAmountCents(10.23))
        assertEquals(1024L, MoneyUtils.toAmountCents(10.235))
    }
}
