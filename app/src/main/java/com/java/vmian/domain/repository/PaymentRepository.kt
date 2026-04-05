package com.java.vmian.domain.repository

import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentPushPayload

/**
 * 支付相关Repository接口
 */
interface PaymentRepository {
    /**
     * 发送心跳
     */
    suspend fun sendHeartbeat(timestamp: Long, sign: String): ApiResponse<String>
    
    /**
     * 推送支付数据
     */
    suspend fun pushPayment(payload: PaymentPushPayload): ApiResponse<String>
}
