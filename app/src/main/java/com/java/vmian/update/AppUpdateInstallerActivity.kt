package com.java.vmian.update

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.io.File

class AppUpdateInstallerActivity : ComponentActivity() {
    private var pendingApkPath: String? = null
    private var launchedPermissionPage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingApkPath = intent.getStringExtra(AppUpdateIntentFactory.EXTRA_APK_PATH)
        continueInstallFlow()
    }

    override fun onResume() {
        super.onResume()
        if (launchedPermissionPage) {
            continueInstallFlow()
        }
    }

    private fun continueInstallFlow() {
        val apkPath = pendingApkPath
        if (apkPath.isNullOrBlank() || !File(apkPath).exists()) {
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            launchedPermissionPage = true
            startActivity(AppUpdateIntentFactory.createUnknownSourcesIntent(this))
            return
        }

        launchedPermissionPage = false
        val installIntent = AppUpdateIntentFactory.createPackageInstallerIntent(this, apkPath)
        startActivity(installIntent)
        finish()
    }
}
