package com.java.vmian.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.java.vmian.receiver.HeartbeatAlarmReceiver

/**
 * 心跳调度器
 * 使用 AlarmManager 调度心跳任务，能够穿透 Doze 模式
 */
object HeartbeatScheduler {
    
    private const val TAG = "HeartbeatScheduler"
    private const val HEARTBEAT_REQUEST_CODE = 1001
    private const val DEFAULT_HEARTBEAT_INTERVAL = 30_000L // 默认30秒
    
    // 动态心跳间隔配置
    private var currentInterval = DEFAULT_HEARTBEAT_INTERVAL
    private var failureCount = 0
    
    /**
     * 开始心跳调度
     * 立即执行第一次心跳，然后按间隔调度后续心跳
     */
    fun startHeartbeat(context: Context) {
        Log.d(TAG, "开始心跳调度")
        LogManager.logSystem("HeartbeatScheduler: 开始心跳调度")
        
        // 重置状态
        resetHeartbeatState()
        
        // 立即执行第一次心跳
        scheduleImmediateHeartbeat(context)
    }
    
    /**
     * 停止心跳调度
     */
    fun stopHeartbeat(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = createHeartbeatPendingIntent(context)
            
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            
            Log.d(TAG, "心跳调度已停止")
            LogManager.logSystem("HeartbeatScheduler: 心跳调度已停止")
            
        } catch (e: Exception) {
            Log.e(TAG, "停止心跳调度失败", e)
            LogManager.logError("停止心跳调度失败: ${e.message}")
        }
    }
    
    /**
     * 调度下一次心跳
     * 在每次心跳完成后调用
     */
    fun scheduleNextHeartbeat(context: Context, isSuccess: Boolean) {
        // 根据成功/失败状态调整间隔
        adjustHeartbeatInterval(isSuccess)
        
        val nextTime = System.currentTimeMillis() + currentInterval
        scheduleHeartbeatAt(context, nextTime)
        
        Log.d(TAG, "已调度下次心跳，间隔: ${currentInterval / 1000}秒")
        LogManager.logHeartbeat("下次心跳调度: ${currentInterval / 1000}秒后")
    }
    
    /**
     * 立即调度心跳
     */
    private fun scheduleImmediateHeartbeat(context: Context) {
        val immediateTime = System.currentTimeMillis() + 1000 // 1秒后执行
        scheduleHeartbeatAt(context, immediateTime)
    }
    
    /**
     * 在指定时间调度心跳
     */
    private fun scheduleHeartbeatAt(context: Context, triggerTime: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = createHeartbeatPendingIntent(context)
            
            // 使用 setExactAndAllowWhileIdle 确保在 Doze 模式下也能执行
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            
            val delaySeconds = (triggerTime - System.currentTimeMillis()) / 1000
            Log.d(TAG, "心跳已调度，${delaySeconds}秒后执行")
            
        } catch (e: Exception) {
            Log.e(TAG, "调度心跳失败", e)
            LogManager.logError("调度心跳失败: ${e.message}")
        }
    }
    
    /**
     * 创建心跳 PendingIntent
     */
    private fun createHeartbeatPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, HeartbeatAlarmReceiver::class.java).apply {
            action = HeartbeatAlarmReceiver.ACTION_HEARTBEAT
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        return PendingIntent.getBroadcast(
            context,
            HEARTBEAT_REQUEST_CODE,
            intent,
            flags
        )
    }
    
    /**
     * 根据成功/失败状态调整心跳间隔
     */
    private fun adjustHeartbeatInterval(isSuccess: Boolean) {
        if (isSuccess) {
            // 成功时重置失败计数，逐渐恢复到正常间隔
            failureCount = 0
            currentInterval = DEFAULT_HEARTBEAT_INTERVAL
        } else {
            // 失败时增加失败计数，延长心跳间隔
            failureCount++
            currentInterval = when {
                failureCount < 3 -> DEFAULT_HEARTBEAT_INTERVAL // 前3次失败保持正常间隔
                failureCount < 5 -> 60_000L // 1分钟
                failureCount < 10 -> 120_000L // 2分钟
                else -> 300_000L // 最大5分钟
            }
        }
        
        LogManager.logSystem("心跳间隔调整为: ${currentInterval / 1000}秒 (失败次数: $failureCount)")
    }
    
    /**
     * 重置心跳状态
     */
    private fun resetHeartbeatState() {
        currentInterval = DEFAULT_HEARTBEAT_INTERVAL
        failureCount = 0
    }
    
    /**
     * 获取当前心跳间隔
     */
    fun getCurrentInterval(): Long = currentInterval
    
    /**
     * 获取失败次数
     */
    fun getFailureCount(): Int = failureCount
}
