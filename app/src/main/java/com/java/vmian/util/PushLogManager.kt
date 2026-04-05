package com.java.vmian.util

import com.java.vmian.domain.model.PushLogEntry
import com.java.vmian.domain.model.PushLogType
import com.java.vmian.domain.repository.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 推送日志管理器 - 单例模式
 * 负责收集、存储和管理推送日志，支持持久化
 */
class PushLogManager private constructor(
    private val logRepository: LogRepository? = null
) {

    companion object {
        private const val MAX_LOG_COUNT = 500

        @Volatile
        private var INSTANCE: PushLogManager? = null

        /**
         * 获取PushLogManager实例
         * @param logRepository 日志持久化Repository，首次调用时必须提供
         */
        fun getInstance(logRepository: LogRepository? = null): PushLogManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PushLogManager(logRepository).also { INSTANCE = it }
            }
        }

        // 静态便捷方法，用于向后兼容
        fun logPushSuccess(paymentType: String, amount: Double, message: String = "推送成功") =
            getInstance().logPushSuccess(paymentType, amount, message)

        fun logPushFailed(paymentType: String, amount: Double, message: String) =
            getInstance().logPushFailed(paymentType, amount, message)

        fun clearLogs() = getInstance().clearLogs()
        val logs get() = getInstance().logs
    }

    // 协程作用域，用于异步持久化
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 使用线程安全的队列存储推送日志
    private val logQueue = ConcurrentLinkedQueue<PushLogEntry>()

    // 推送日志列表的StateFlow，供UI观察
    private val _logs = MutableStateFlow<List<PushLogEntry>>(emptyList())
    val logs: StateFlow<List<PushLogEntry>> = _logs.asStateFlow()

    init {
        // 应用启动时恢复历史日志
        restoreLogsFromStorage()
    }
    
    /**
     * 添加推送日志条目
     */
    fun addLog(
        type: PushLogType,
        paymentType: String,
        amount: Double,
        message: String,
        isSuccess: Boolean
    ) {
        val logEntry = PushLogEntry(
            type = type,
            paymentType = paymentType,
            amount = amount,
            message = message,
            isSuccess = isSuccess
        )

        // 添加到队列
        logQueue.offer(logEntry)

        // 限制日志数量，移除最旧的日志
        while (logQueue.size > MAX_LOG_COUNT) {
            logQueue.poll()
        }

        // 更新StateFlow
        val currentLogs = logQueue.toList().sortedByDescending { it.timestamp }
        _logs.value = currentLogs

        // 异步持久化到存储
        persistLogsToStorage(currentLogs)
    }
    
    /**
     * 清空所有推送日志
     */
    fun clearLogs() {
        logQueue.clear()
        _logs.value = emptyList()

        // 异步清空持久化存储
        coroutineScope.launch {
            try {
                logRepository?.clearPushLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取推送日志数量
     */
    fun getLogCount(): Int = logQueue.size
    
    /**
     * 从存储恢复推送日志
     */
    private fun restoreLogsFromStorage() {
        coroutineScope.launch {
            try {
                val savedLogs = logRepository?.loadPushLogs() ?: emptyList()
                if (savedLogs.isNotEmpty()) {
                    // 清空当前队列
                    logQueue.clear()

                    // 添加恢复的日志到队列
                    savedLogs.forEach { logQueue.offer(it) }

                    // 限制日志数量
                    while (logQueue.size > MAX_LOG_COUNT) {
                        logQueue.poll()
                    }

                    // 更新StateFlow
                    _logs.value = logQueue.toList().sortedByDescending { it.timestamp }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 异步持久化推送日志到存储
     */
    private fun persistLogsToStorage(logs: List<PushLogEntry>) {
        coroutineScope.launch {
            try {
                logRepository?.savePushLogs(logs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 便捷方法
    fun logPushSuccess(paymentType: String, amount: Double, message: String = "推送成功") {
        addLog(PushLogType.SUCCESS, paymentType, amount, message, true)
    }

    fun logPushFailed(paymentType: String, amount: Double, message: String) {
        addLog(PushLogType.FAILED, paymentType, amount, message, false)
    }
}
