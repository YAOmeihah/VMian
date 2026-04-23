package com.java.vmian.data.remote

import com.java.vmian.data.remote.dto.VmqApiResponse
import com.java.vmian.data.remote.dto.PushPaymentRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * 支付API服务接口
 */
interface PaymentApiService {

    /**
     * 发送心跳
     */
    @GET
    suspend fun sendHeartbeat(
        @Url url: String,
        @Query("terminalCode") terminalCode: String,
        @Query("t") timestamp: Long,
        @Query("sign") sign: String
    ): Response<VmqApiResponse<Any?>>

    /**
     * 推送支付数据
     */
    @POST
    suspend fun pushPayment(
        @Url url: String,
        @Body body: PushPaymentRequestDto
    ): Response<VmqApiResponse<Any?>>
}
