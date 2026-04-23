package com.java.vmian.domain.model

data class PaymentPushPayload(
    val terminalCode: String,
    val type: Int,
    val amountCents: Long,
    val timestamp: Long,
    val nonce: String,
    val eventId: String,
    val sign: String
)
