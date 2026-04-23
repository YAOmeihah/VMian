package com.java.vmian.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.java.vmian.domain.repository.AppUpdatePreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AppUpdatePreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : AppUpdatePreferencesRepository {

    private val lastCheckAtKey = longPreferencesKey("app_update_last_check_at")
    private val ignoredVersionCodeKey = intPreferencesKey("app_update_ignored_version_code")
    private val pendingApkPathKey = stringPreferencesKey("app_update_pending_apk_path")
    private val pendingVersionCodeKey = intPreferencesKey("app_update_pending_version_code")
    private val awaitingInstallPermissionKey = booleanPreferencesKey("app_update_awaiting_install_permission")

    override suspend fun getLastCheckAt(): Long {
        return dataStore.data.map { it[lastCheckAtKey] ?: 0L }.first()
    }

    override suspend fun setLastCheckAt(timestamp: Long) {
        dataStore.edit { it[lastCheckAtKey] = timestamp }
    }

    override suspend fun getIgnoredVersionCode(): Int? {
        return dataStore.data.map { it[ignoredVersionCodeKey] }.first()
    }

    override suspend fun setIgnoredVersionCode(versionCode: Int?) {
        dataStore.edit { preferences ->
            if (versionCode == null) {
                preferences.remove(ignoredVersionCodeKey)
            } else {
                preferences[ignoredVersionCodeKey] = versionCode
            }
        }
    }

    override suspend fun getPendingApkPath(): String? {
        return dataStore.data.map { it[pendingApkPathKey] }.first()
    }

    override suspend fun setPendingApkPath(path: String?) {
        dataStore.edit { preferences ->
            if (path == null) {
                preferences.remove(pendingApkPathKey)
            } else {
                preferences[pendingApkPathKey] = path
            }
        }
    }

    override suspend fun getPendingVersionCode(): Int? {
        return dataStore.data.map { it[pendingVersionCodeKey] }.first()
    }

    override suspend fun setPendingVersionCode(versionCode: Int?) {
        dataStore.edit { preferences ->
            if (versionCode == null) {
                preferences.remove(pendingVersionCodeKey)
            } else {
                preferences[pendingVersionCodeKey] = versionCode
            }
        }
    }

    override suspend fun isAwaitingInstallPermission(): Boolean {
        return dataStore.data.map { it[awaitingInstallPermissionKey] ?: false }.first()
    }

    override suspend fun setAwaitingInstallPermission(awaiting: Boolean) {
        dataStore.edit { it[awaitingInstallPermissionKey] = awaiting }
    }
}
