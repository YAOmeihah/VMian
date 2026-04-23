package com.java.vmian.data.remote.dto

data class PushPaymentRequestDto(
    val terminalCode: String,
    val type: Int,
    val amountCents: Long,
    val ts: Long,
    val nonce: String,
    val eventId: String,
    val sign: String
)
