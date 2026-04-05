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
    private val keyKey = stringPreferencesKey("key")

    override suspend fun saveConfig(config: PaymentConfig) {
        dataStore.edit { preferences ->
            preferences[hostKey] = config.host
            preferences[keyKey] = config.key
        }
    }

    override suspend fun getConfig(): PaymentConfig? {
        return dataStore.data.map { preferences ->
            val host = preferences[hostKey] ?: ""
            val key = preferences[keyKey] ?: ""
            if (host.isNotEmpty() && key.isNotEmpty()) {
                PaymentConfig(host, key, true)
            } else null
        }.first()
    }

    override suspend fun clearConfig() {
        dataStore.edit { it.clear() }
    }
}
