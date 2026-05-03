package com.java.vmian.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.java.vmian.presentation.ui.model.KeepAliveControlUiModel
import com.java.vmian.service.KeepAliveMediaService
import com.java.vmian.service.KeepAliveOverlayService

object KeepAliveController {

    fun applyStoredSettings(context: Context, allowMediaPlayback: Boolean = true) {
        if (allowMediaPlayback && KeepAliveSettingsStore.isMediaEnabled(context)) {
            KeepAliveMediaService.start(context)
        }
        if (KeepAliveSettingsStore.isOverlayEnabled(context) && canDrawOverlays(context)) {
            KeepAliveOverlayService.start(context)
        }
    }

    fun currentUiModel(context: Context): KeepAliveControlUiModel {
        return KeepAliveControlUiModel.from(
            mediaPreferenceEnabled = KeepAliveSettingsStore.isMediaEnabled(context),
            mediaServiceRunning = KeepAliveMediaService.isServiceRunning(),
            overlayPermissionGranted = canDrawOverlays(context),
            overlayPreferenceEnabled = KeepAliveSettingsStore.isOverlayEnabled(context),
            overlayServiceRunning = KeepAliveOverlayService.isServiceRunning()
        )
    }

    fun setMediaEnabled(context: Context, enabled: Boolean): KeepAliveControlUiModel {
        KeepAliveSettingsStore.setMediaEnabled(context, enabled)
        if (enabled) {
            KeepAliveMediaService.start(context)
        } else {
            KeepAliveMediaService.stop(context)
        }
        return currentUiModel(context)
    }

    fun setOverlayEnabled(context: Context, enabled: Boolean): KeepAliveControlUiModel {
        if (enabled && !canDrawOverlays(context)) {
            KeepAliveSettingsStore.setOverlayEnabled(context, false)
            return currentUiModel(context)
        }

        KeepAliveSettingsStore.setOverlayEnabled(context, enabled)
        if (enabled) {
            KeepAliveOverlayService.start(context)
        } else {
            KeepAliveOverlayService.stop(context)
        }
        return currentUiModel(context)
    }

    fun openOverlayPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { PermissionUtils.openAppDetailsSettings(context) }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    }
}
