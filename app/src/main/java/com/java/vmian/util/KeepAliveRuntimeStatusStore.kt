package com.java.vmian.util

import android.content.Context
import android.content.Intent

object KeepAliveRuntimeStatusStore {
    const val ACTION_HEARTBEAT_STATUS_CHANGED = "com.java.vmian.ACTION_HEARTBEAT_STATUS_CHANGED"

    private const val PREFS_NAME = "keep_alive_runtime_status"
    private const val KEY_LAST_HEARTBEAT_AT = "last_heartbeat_at"

    fun getLastHeartbeatAt(context: Context): Long? {
        val value = prefs(context).getLong(KEY_LAST_HEARTBEAT_AT, 0L)
        return value.takeIf { it > 0L }
    }

    fun recordHeartbeat(context: Context, timestampMillis: Long = System.currentTimeMillis()) {
        prefs(context).edit().putLong(KEY_LAST_HEARTBEAT_AT, timestampMillis).apply()
        context.sendBroadcast(
            Intent(ACTION_HEARTBEAT_STATUS_CHANGED).apply {
                setPackage(context.packageName)
                putExtra(KEY_LAST_HEARTBEAT_AT, timestampMillis)
            }
        )
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
}
