package com.java.vmian.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.java.vmian.util.LogManager

/**
 * 保活无障碍服务 - 优化版
 * 
 * 这是一个轻量级的无障碍服务，专门用于提高应用的保活成功率。
 * 该服务不会干扰用户的正常使用，仅用于保持应用在后台运行。
 * 
 * 优化特性：
 * 1. 最小化配置，减少系统资源占用
 * 2. 不处理任何无障碍事件，专注保活功能
 * 3. 简化生命周期管理，提高稳定性
 */
class KeepAliveAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KeepAliveAccessibility"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "保活无障碍服务已连接")
        LogManager.logSystem("保活无障碍服务已启动")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 这是一个保活专用的无障碍服务，不处理任何无障碍事件
        // 仅用于保持服务运行状态，提高应用保活成功率
        // 不进行任何事件处理，确保对用户使用无影响
    }

    override fun onInterrupt() {
        Log.d(TAG, "保活无障碍服务被中断")
        LogManager.logSystem("保活无障碍服务被中断")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "保活无障碍服务解绑")
        LogManager.logSystem("保活无障碍服务已停止")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "保活无障碍服务销毁")
        LogManager.logSystem("保活无障碍服务已销毁")
    }
}
