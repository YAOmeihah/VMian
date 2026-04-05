package com.java.vmian.util

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * 心跳测试辅助工具
 * 用于验证新的心跳机制是否正常工作
 */
object HeartbeatTestHelper {
    
    private const val TAG = "HeartbeatTestHelper"
    
    /**
     * 检查系统是否支持精确闹钟
     */
    fun checkExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "精确闹钟权限检查: $canSchedule")
            LogManager.logSystem("精确闹钟权限: $canSchedule")
            canSchedule
        } else {
            Log.d(TAG, "Android 版本 < 12，无需检查精确闹钟权限")
            true
        }
    }
    
    /**
     * 检查电池优化状态
     */
    fun checkBatteryOptimization(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            Log.d(TAG, "电池优化忽略状态: $isIgnoring")
            LogManager.logSystem("电池优化忽略: $isIgnoring")
            isIgnoring
        } else {
            Log.d(TAG, "Android 版本 < 6.0，无电池优化限制")
            true
        }
    }
    
    /**
     * 获取心跳机制状态报告
     */
    fun getHeartbeatStatusReport(context: Context): String {
        val report = StringBuilder()
        
        report.appendLine("=== 心跳机制状态报告 ===")
        report.appendLine("时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
        report.appendLine()
        
        // 权限检查
        report.appendLine("权限状态:")
        report.appendLine("- 通知监听权限: ${PermissionUtils.isNotificationListenerEnabled(context)}")
        report.appendLine("- 精确闹钟权限: ${checkExactAlarmPermission(context)}")
        report.appendLine("- 电池优化忽略: ${checkBatteryOptimization(context)}")
        report.appendLine()
        
        // 心跳调度状态
        report.appendLine("心跳调度状态:")
        report.appendLine("- 当前间隔: ${HeartbeatScheduler.getCurrentInterval() / 1000}秒")
        report.appendLine("- 失败次数: ${HeartbeatScheduler.getFailureCount()}")
        report.appendLine("- WakeLock状态: ${WakeLockManager.isWakeLockHeld()}")
        report.appendLine()
        
        // 系统信息
        report.appendLine("系统信息:")
        report.appendLine("- Android版本: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        report.appendLine("- 设备制造商: ${Build.MANUFACTURER}")
        report.appendLine("- 设备型号: ${Build.MODEL}")
        report.appendLine()
        
        // Doze 模式相关
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            report.appendLine("Doze 模式信息:")
            report.appendLine("- 设备空闲状态: ${powerManager.isDeviceIdleMode}")
            // isLightDeviceIdleMode 在某些 Android 版本中可能不可用，使用 try-catch 处理
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val isLightIdle = powerManager.javaClass.getMethod("isLightDeviceIdleMode").invoke(powerManager) as Boolean
                    report.appendLine("- 轻度Doze状态: $isLightIdle")
                }
            } catch (e: Exception) {
                report.appendLine("- 轻度Doze状态: 无法获取")
            }
            report.appendLine()
        }
        
        report.appendLine("=== 报告结束 ===")
        
        val reportString = report.toString()
        Log.d(TAG, reportString)
        LogManager.logSystem("心跳状态报告已生成")
        
        return reportString
    }
    
    /**
     * 立即触发一次心跳测试
     */
    fun triggerImmediateHeartbeat(context: Context) {
        Log.d(TAG, "触发立即心跳测试")
        LogManager.logSystem("手动触发心跳测试")
        
        // 停止当前调度
        HeartbeatScheduler.stopHeartbeat(context)
        
        // 延迟1秒后重新开始
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            HeartbeatScheduler.startHeartbeat(context)
        }, 1000)
    }
    
    /**
     * 模拟 Doze 模式测试
     * 注意：这只是一个提示，实际的 Doze 模式测试需要使用 adb 命令
     */
    fun getDozeModeTestInstructions(): String {
        return """
            === Doze 模式测试说明 ===
            
            要测试心跳机制在 Doze 模式下的表现，请使用以下 adb 命令：
            
            1. 进入 Doze 模式：
               adb shell dumpsys deviceidle force-idle
            
            2. 退出 Doze 模式：
               adb shell dumpsys deviceidle unforce
            
            3. 查看 Doze 状态：
               adb shell dumpsys deviceidle
            
            4. 查看应用白名单：
               adb shell dumpsys deviceidle whitelist
            
            测试步骤：
            1. 启动应用并开启通知监听
            2. 观察心跳日志正常工作
            3. 使用 adb 命令进入 Doze 模式
            4. 等待 30-60 秒，观察心跳是否仍然执行
            5. 退出 Doze 模式，确认心跳恢复正常
            
            预期结果：
            - 在 Doze 模式下，心跳应该能够正常执行
            - AlarmManager.setExactAndAllowWhileIdle() 应该能够唤醒设备
            - WakeLock 应该确保网络请求完成
        """.trimIndent()
    }
}
