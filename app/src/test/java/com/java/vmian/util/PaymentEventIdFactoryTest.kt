package com.java.vmian.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PaymentEventIdFactoryTest {

    @Test
    fun create_returnsStableIdForSameNotificationFingerprint() {
        val first = PaymentEventIdFactory.create(
            packageName = "com.eg.android.AlipayGphone",
            notificationKey = "0|com.eg.android.AlipayGphone|100|null|10001",
            postTime = 1710000000000L,
            title = "收款成功",
            content = "收款10.23元"
        )

        val second = PaymentEventIdFactory.create(
            packageName = "com.eg.android.AlipayGphone",
            notificationKey = "0|com.eg.android.AlipayGphone|100|null|10001",
            postTime = 1710000000000L,
            title = "收款成功",
            content = "收款10.23元"
        )

        assertEquals(first, second)
    }

    @Test
    fun create_returnsDifferentIdWhenNotificationFingerprintChanges() {
        val first = PaymentEventIdFactory.create(
            packageName = "com.eg.android.AlipayGphone",
            notificationKey = "0|com.eg.android.AlipayGphone|100|null|10001",
            postTime = 1710000000000L,
            title = "收款成功",
            content = "收款10.23元"
        )

        val second = PaymentEventIdFactory.create(
            packageName = "com.eg.android.AlipayGphone",
            notificationKey = "0|com.eg.android.AlipayGphone|101|null|10001",
            postTime = 1710000000000L,
            title = "收款成功",
            content = "收款88.88元"
        )

        assertNotEquals(first, second)
    }
}
