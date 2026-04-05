package com.java.vmian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.java.vmian.VmqApplication
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.util.HeartbeatScheduler
import com.java.vmian.util.LogManager
import com.java.vmian.util.WakeLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 心跳闹钟广播接收器
 * 接收 AlarmManager 发送的心跳信号，执行一次性心跳任务
 */
class HeartbeatAlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "HeartbeatAlarmReceiver"
        const val ACTION_HEARTBEAT = "com.java.vmian.ACTION_HEARTBEAT"
    }
    
    // 使用应用级协程作用域，确保任务能够完成
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_HEARTBEAT) {
            return
        }
        
        Log.d(TAG, "收到心跳闹钟信号")
        LogManager.logHeartbeat("HeartbeatAlarmReceiver: 收到心跳信号")
        
        // 获取 WakeLock 确保任务执行期间设备保持唤醒
        WakeLockManager.acquireWakeLock(context)
        
        // 执行心跳任务
        performHeartbeat(context)
    }
    
    /**
     * 执行心跳任务
     */
    private fun performHeartbeat(context: Context) {
        receiverScope.launch {
            var isSuccess = false
            
            try {
                Log.d(TAG, "开始执行心跳任务")
                LogManager.logHeartbeat("开始执行心跳任务")
                
                // 获取应用容器和用例
                val application = context.applicationContext as VmqApplication
                val paymentUseCase = application.container.paymentUseCase
                
                // 执行心跳请求
                val result = paymentUseCase.sendHeartbeat()
                
                when (result) {
                    is ApiResponse.Success -> {
                        isSuccess = true
                        Log.d(TAG, "心跳成功: ${result.data}")
                        LogManager.logHeartbeat("心跳成功: ${result.data}")
                        
                        // 更新前台服务通知（如果服务正在运行）
                        updateServiceNotification(context, "服务运行正常 - ${result.data}")
                    }
                    
                    is ApiResponse.Error -> {
                        isSuccess = false
                        Log.e(TAG, "心跳失败: ${result.message}")
                        LogManager.logError("心跳失败: ${result.message}")
                        
                        // 更新前台服务通知
                        updateServiceNotification(context, "连接异常: ${result.message}")
                    }
                    
                    else -> {
                        isSuccess = false
                        Log.w(TAG, "心跳返回未知结果")
                        LogManager.logError("心跳返回未知结果")
                    }
                }
                
            } catch (e: Exception) {
                isSuccess = false
                Log.e(TAG, "心跳任务异常", e)
                LogManager.logError("心跳任务异常: ${e.message}")
                
                // 更新前台服务通知
                updateServiceNotification(context, "网络异常: ${e.message?.take(20) ?: "未知错误"}")
                
            } finally {
                // 任务完成后立即释放 WakeLock
                WakeLockManager.releaseWakeLock()
                
                // 调度下一次心跳
                HeartbeatScheduler.scheduleNextHeartbeat(context, isSuccess)
                
                Log.d(TAG, "心跳任务完成，成功: $isSuccess")
                LogManager.logHeartbeat("心跳任务完成，成功: $isSuccess")
            }
        }
    }
    
    /**
     * 更新前台服务通知
     * 如果 PaymentNotificationService 正在运行，更新其通知内容
     */
    private fun updateServiceNotification(context: Context, message: String) {
        try {
            // 发送广播给 PaymentNotificationService 更新通知
            val updateIntent = Intent("com.java.vmian.UPDATE_NOTIFICATION").apply {
                putExtra("message", message)
                setPackage(context.packageName)
            }
            context.sendBroadcast(updateIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "更新服务通知失败", e)
        }
    }
}
