package com.java.vmian.domain.repository

import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.PushLogEntry

/**
 * 日志持久化Repository接口
 * 负责日志数据的持久化存储和恢复
 */
interface LogRepository {
    
    /**
     * 保存运行日志列表
     * @param logs 要保存的日志列表
     */
    suspend fun saveLogs(logs: List<LogEntry>)
    
    /**
     * 加载运行日志列表
     * @return 保存的日志列表，如果没有数据则返回空列表
     */
    suspend fun loadLogs(): List<LogEntry>
    
    /**
     * 保存推送日志列表
     * @param pushLogs 要保存的推送日志列表
     */
    suspend fun savePushLogs(pushLogs: List<PushLogEntry>)
    
    /**
     * 加载推送日志列表
     * @return 保存的推送日志列表，如果没有数据则返回空列表
     */
    suspend fun loadPushLogs(): List<PushLogEntry>
    
    /**
     * 清空所有运行日志
     */
    suspend fun clearLogs()
    
    /**
     * 清空所有推送日志
     */
    suspend fun clearPushLogs()
}
