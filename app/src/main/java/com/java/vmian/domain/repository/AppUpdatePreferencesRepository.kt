package com.java.vmian.domain.repository

interface AppUpdatePreferencesRepository {
    suspend fun getLastCheckAt(): Long
    suspend fun setLastCheckAt(timestamp: Long)
    suspend fun getIgnoredVersionCode(): Int?
    suspend fun setIgnoredVersionCode(versionCode: Int?)
    suspend fun getPendingApkPath(): String?
    suspend fun setPendingApkPath(path: String?)
    suspend fun getPendingVersionCode(): Int?
    suspend fun setPendingVersionCode(versionCode: Int?)
    suspend fun isAwaitingInstallPermission(): Boolean
    suspend fun setAwaitingInstallPermission(awaiting: Boolean)
}
