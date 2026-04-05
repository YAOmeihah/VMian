package com.java.vmian.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * V免签API标准响应格式
 */
data class VmqApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("msg")
    val message: String,
    
    @SerializedName("data")
    val data: T?
) {
    /**
     * 判断请求是否成功
     * 根据V免签API规范，code=1表示成功
     */
    fun isSuccess(): Boolean = code == 1
    
    /**
     * 获取错误信息
     */
    fun getErrorMessage(): String = message
}
