package com.java.vmian.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.PushLogEntry
import com.java.vmian.domain.repository.LogRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 日志持久化Repository实现类
 * 使用DataStore和JSON序列化来持久化日志数据
 */
class LogRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : LogRepository {

    private val gson = Gson()
    private val logsKey = stringPreferencesKey("app_logs")
    private val pushLogsKey = stringPreferencesKey("push_logs")

    override suspend fun saveLogs(logs: List<LogEntry>) {
        try {
            val jsonString = gson.toJson(logs)
            dataStore.edit { preferences ->
                preferences[logsKey] = jsonString
            }
        } catch (e: Exception) {
            // 静默处理序列化错误，避免影响应用正常运行
            e.printStackTrace()
        }
    }

    override suspend fun loadLogs(): List<LogEntry> {
        return try {
            dataStore.data.map { preferences ->
                val jsonString = preferences[logsKey] ?: ""
                if (jsonString.isNotEmpty()) {
                    val type = object : TypeToken<List<LogEntry>>() {}.type
                    gson.fromJson<List<LogEntry>>(jsonString, type) ?: emptyList()
                } else {
                    emptyList()
                }
            }.first()
        } catch (e: Exception) {
            // 反序列化失败时返回空列表，避免应用崩溃
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun savePushLogs(pushLogs: List<PushLogEntry>) {
        try {
            val jsonString = gson.toJson(pushLogs)
            dataStore.edit { preferences ->
                preferences[pushLogsKey] = jsonString
            }
        } catch (e: Exception) {
            // 静默处理序列化错误，避免影响应用正常运行
            e.printStackTrace()
        }
    }

    override suspend fun loadPushLogs(): List<PushLogEntry> {
        return try {
            dataStore.data.map { preferences ->
                val jsonString = preferences[pushLogsKey] ?: ""
                if (jsonString.isNotEmpty()) {
                    val type = object : TypeToken<List<PushLogEntry>>() {}.type
                    gson.fromJson<List<PushLogEntry>>(jsonString, type) ?: emptyList()
                } else {
                    emptyList()
                }
            }.first()
        } catch (e: Exception) {
            // 反序列化失败时返回空列表，避免应用崩溃
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun clearLogs() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(logsKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun clearPushLogs() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(pushLogsKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
