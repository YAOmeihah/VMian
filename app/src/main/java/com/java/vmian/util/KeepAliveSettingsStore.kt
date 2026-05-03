package com.java.vmian.util

import android.content.Context

object KeepAliveSettingsStore {
    private const val PREFS_NAME = "keep_alive_settings"
    private const val KEY_MEDIA_ENABLED = "media_enabled"
    private const val KEY_OVERLAY_ENABLED = "overlay_enabled"

    fun isMediaEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_MEDIA_ENABLED, false)
    }

    fun setMediaEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MEDIA_ENABLED, enabled).apply()
    }

    fun isOverlayEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_OVERLAY_ENABLED, false)
    }

    fun setOverlayEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
}
