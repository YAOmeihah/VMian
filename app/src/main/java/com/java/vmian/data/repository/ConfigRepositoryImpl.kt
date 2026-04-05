package com.java.vmian.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.repository.ConfigRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 配置Repository实现类
 */
class ConfigRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ConfigRepository {

    private val hostKey = stringPreferencesKey("host")
    private val monitorKeyKey = stringPreferencesKey("monitor_key")
    private val legacyKeyKey = stringPreferencesKey("key")

    override suspend fun saveConfig(config: PaymentConfig) {
        dataStore.edit { preferences ->
            preferences[hostKey] = config.host
            preferences[monitorKeyKey] = config.monitorKey
        }
    }

    override suspend fun getConfig(): PaymentConfig? {
        return dataStore.data.map { preferences ->
            val host = preferences[hostKey] ?: ""
            val monitorKey = preferences[monitorKeyKey] ?: preferences[legacyKeyKey] ?: ""
            if (host.isNotEmpty() && monitorKey.isNotEmpty()) {
                PaymentConfig(host, monitorKey, true)
            } else null
        }.first()
    }

    override suspend fun clearConfig() {
        dataStore.edit { it.clear() }
    }
}
