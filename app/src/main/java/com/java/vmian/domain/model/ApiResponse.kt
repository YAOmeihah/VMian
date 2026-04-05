package com.java.vmian.domain.model

/**
 * API响应封装类
 */
sealed class ApiResponse<T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error<T>(val message: String, val code: Int? = null) : ApiResponse<T>()
    data class Loading<T>(val isLoading: Boolean = true) : ApiResponse<T>()
}
