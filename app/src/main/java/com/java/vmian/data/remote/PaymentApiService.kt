package com.java.vmian.data.remote

import com.java.vmian.data.remote.dto.VmqApiResponse
import retrofit2.Response
import retrofit2.http.GET
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
        @Query("t") timestamp: Long,
        @Query("sign") sign: String
    ): Response<VmqApiResponse<Any?>>

    /**
     * 推送支付数据
     */
    @GET
    suspend fun pushPayment(
        @Url url: String,
        @Query("t") timestamp: Long,
        @Query("type") type: Int,
        @Query("price") price: Double,
        @Query("sign") sign: String
    ): Response<VmqApiResponse<Any?>>
}
