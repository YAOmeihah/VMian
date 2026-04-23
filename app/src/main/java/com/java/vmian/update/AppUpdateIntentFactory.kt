package com.java.vmian.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

object AppUpdateIntentFactory {
    const val EXTRA_APK_PATH = "extra_apk_path"

    fun createInstallerActivityIntent(context: Context, apkPath: String): Intent {
        return Intent(context, AppUpdateInstallerActivity::class.java).apply {
            putExtra(EXTRA_APK_PATH, apkPath)
        }
    }

    fun createPackageInstallerIntent(context: Context, apkPath: String): Intent {
        val apkFile = File(apkPath)
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun createUnknownSourcesIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
    }
}
