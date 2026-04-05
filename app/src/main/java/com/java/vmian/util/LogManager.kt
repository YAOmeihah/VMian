package com.java.vmian.util

import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.LogType
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
 * 日志管理器 - 单例模式
 * 负责收集、存储和管理应用日志，支持持久化
 */
class LogManager private constructor(
    private val logRepository: LogRepository? = null
) {

    companion object {
        private const val MAX_LOG_COUNT = 500

        @Volatile
        private var INSTANCE: LogManager? = null

        /**
         * 获取LogManager实例
         * @param logRepository 日志持久化Repository，首次调用时必须提供
         */
        fun getInstance(logRepository: LogRepository? = null): LogManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogManager(logRepository).also { INSTANCE = it }
            }
        }

        // 静态便捷方法，用于向后兼容
        fun logHeartbeat(message: String) = getInstance().logHeartbeat(message)
        fun logPaymentAlipay(message: String) = getInstance().logPaymentAlipay(message)
        fun logPaymentWechat(message: String) = getInstance().logPaymentWechat(message)
        fun logNetwork(message: String) = getInstance().logNetwork(message)
        fun logConfig(message: String) = getInstance().logConfig(message)
        fun logSystem(message: String) = getInstance().logSystem(message)
        fun logError(message: String) = getInstance().logError(message)
        fun clearLogs() = getInstance().clearLogs()
        val logs get() = getInstance().logs
    }

    // 协程作用域，用于异步持久化
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 使用线程安全的队列存储日志
    private val logQueue = ConcurrentLinkedQueue<LogEntry>()

    // 日志列表的StateFlow，供UI观察
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    init {
        // 应用启动时恢复历史日志
        restoreLogsFromStorage()
    }
    
    /**
     * 添加日志条目
     */
    fun addLog(type: LogType, message: String) {
        val logEntry = LogEntry(type = type, message = message)

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
     * 清空所有日志
     */
    fun clearLogs() {
        logQueue.clear()
        _logs.value = emptyList()

        // 异步清空持久化存储
        coroutineScope.launch {
            try {
                logRepository?.clearLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取日志数量
     */
    fun getLogCount(): Int = logQueue.size
    
    /**
     * 从存储恢复日志
     */
    private fun restoreLogsFromStorage() {
        coroutineScope.launch {
            try {
                val savedLogs = logRepository?.loadLogs() ?: emptyList()
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
     * 异步持久化日志到存储
     */
    private fun persistLogsToStorage(logs: List<LogEntry>) {
        coroutineScope.launch {
            try {
                logRepository?.saveLogs(logs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 便捷方法
    fun logHeartbeat(message: String) = addLog(LogType.HEARTBEAT, message)
    fun logPaymentAlipay(message: String) = addLog(LogType.PAYMENT_ALIPAY, message)
    fun logPaymentWechat(message: String) = addLog(LogType.PAYMENT_WECHAT, message)
    fun logNetwork(message: String) = addLog(LogType.NETWORK, message)
    fun logConfig(message: String) = addLog(LogType.CONFIG, message)
    fun logSystem(message: String) = addLog(LogType.SYSTEM, message)
    fun logError(message: String) = addLog(LogType.ERROR, message)
}
