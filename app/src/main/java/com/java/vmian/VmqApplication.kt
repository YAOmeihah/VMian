package com.java.vmian

import android.app.Application
import com.java.vmian.di.AppContainer
import com.java.vmian.util.KeepAliveController

/**
 * 应用程序入口类
 */
class VmqApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        KeepAliveController.applyStoredSettings(this)
    }
}
