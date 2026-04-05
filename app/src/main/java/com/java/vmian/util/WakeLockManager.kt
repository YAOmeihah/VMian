package com.java.vmian.util

import android.content.Context
import android.os.PowerManager
import android.util.Log

/**
 * WakeLock 管理器
 * 用于在心跳网络请求期间保持 CPU 唤醒，确保在 Doze 模式下也能正常执行
 */
object WakeLockManager {
    
    private const val TAG = "WakeLockManager"
    private const val WAKE_LOCK_TAG = "VMian:HeartbeatWakeLock"
    private const val WAKE_LOCK_TIMEOUT = 30_000L // 30秒超时，防止忘记释放
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    /**
     * 获取 WakeLock
     * 在心跳任务开始前调用
     */
    fun acquireWakeLock(context: Context) {
        try {
            // 如果已经持有 WakeLock，先释放
            releaseWakeLock()
            
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                // 设置超时时间，防止忘记释放导致电量消耗
                acquire(WAKE_LOCK_TIMEOUT)
            }
            
            Log.d(TAG, "WakeLock 已获取")
            LogManager.logSystem("WakeLock 已获取，确保心跳任务执行")
            
        } catch (e: Exception) {
            Log.e(TAG, "获取 WakeLock 失败", e)
            LogManager.logError("获取 WakeLock 失败: ${e.message}")
        }
    }
    
    /**
     * 释放 WakeLock
     * 在心跳任务完成后立即调用
     */
    fun releaseWakeLock() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "WakeLock 已释放")
                    LogManager.logSystem("WakeLock 已释放")
                }
            }
            wakeLock = null
            
        } catch (e: Exception) {
            Log.e(TAG, "释放 WakeLock 失败", e)
            LogManager.logError("释放 WakeLock 失败: ${e.message}")
        }
    }
    
    /**
     * 检查 WakeLock 状态
     */
    fun isWakeLockHeld(): Boolean {
        return wakeLock?.isHeld ?: false
    }
    
    /**
     * 强制清理 WakeLock
     * 在应用退出或服务销毁时调用
     */
    fun forceCleanup() {
        try {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                    Log.d(TAG, "强制释放 WakeLock")
                    LogManager.logSystem("强制释放 WakeLock")
                }
            }
            wakeLock = null
            
        } catch (e: Exception) {
            Log.e(TAG, "强制清理 WakeLock 失败", e)
            LogManager.logError("强制清理 WakeLock 失败: ${e.message}")
        }
    }
}
