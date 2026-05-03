package com.java.vmian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.java.vmian.util.HeartbeatScheduler
import com.java.vmian.util.KeepAliveController
import com.java.vmian.util.LogManager
import com.java.vmian.util.PermissionUtils

/**
 * 开机启动广播接收器
 * 在设备重启后自动恢复心跳调度
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "收到系统启动/应用更新广播: ${intent.action}")
                LogManager.logSystem("BootReceiver: 收到广播 ${intent.action}")

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    restoreKeepAliveComponents(context)
                }, 10_000)
                
                // 检查通知监听权限是否已开启
                if (PermissionUtils.isNotificationListenerEnabled(context)) {
                    // 延迟启动心跳调度，确保系统完全启动
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startHeartbeatScheduler(context)
                    }, 10_000) // 延迟10秒
                } else {
                    Log.w(TAG, "通知监听权限未开启，跳过心跳调度启动")
                    LogManager.logError("开机启动检查: 通知监听权限未开启")
                }
            }
        }
    }
    
    /**
     * 启动心跳调度器
     */
    private fun startHeartbeatScheduler(context: Context) {
        try {
            HeartbeatScheduler.startHeartbeat(context)
            Log.d(TAG, "开机启动心跳调度成功")
            LogManager.logSystem("开机启动心跳调度成功")
        } catch (e: Exception) {
            Log.e(TAG, "开机启动心跳调度失败", e)
            LogManager.logError("开机启动心跳调度失败: ${e.message}")
        }
    }

    private fun restoreKeepAliveComponents(context: Context) {
        try {
            KeepAliveController.applyStoredSettings(context, allowMediaPlayback = false)
            Log.d(TAG, "开机恢复保活组件完成")
            LogManager.logSystem("开机恢复保活组件完成")
        } catch (e: Exception) {
            Log.e(TAG, "开机恢复保活组件失败", e)
            LogManager.logError("开机恢复保活组件失败: ${e.message}")
        }
    }
}
