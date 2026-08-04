package com.java.vmian.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.java.vmian.MainActivity
import com.java.vmian.R
import com.java.vmian.VmqApplication
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentNotification
import com.java.vmian.domain.usecase.PaymentUseCase
import com.java.vmian.util.HeartbeatScheduler
import com.java.vmian.util.LogManager
import com.java.vmian.util.PaymentEventIdFactory
import com.java.vmian.util.PushLogManager
import com.java.vmian.util.WakeLockManager
import kotlinx.coroutines.*

/**
 * 支付通知监听服务
 */
class PaymentNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "PaymentNotificationService"
        private const val FOREGROUND_NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "vmq_service_channel"
    }

    private lateinit var paymentUseCase: PaymentUseCase
    private lateinit var logManager: LogManager
    private lateinit var pushLogManager: PushLogManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 通知更新广播接收器
    private val notificationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.java.vmian.UPDATE_NOTIFICATION") {
                val message = intent.getStringExtra("message") ?: return
                updateForegroundNotification(message)
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "通知监听服务已连接")

        // 初始化依赖
        val application = applicationContext as VmqApplication
        paymentUseCase = application.container.paymentUseCase
        logManager = application.container.logManager
        pushLogManager = application.container.pushLogManager

        // 启动前台服务
        startForegroundService()

        // 注册通知更新广播接收器
        registerNotificationUpdateReceiver()

        // 启动新的心跳调度机制
        startHeartbeat()
        showToast("监听服务开启成功！")
        logManager.logSystem("通知监听服务已启动")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val notification = sbn.notification ?: return
        val packageName = sbn.packageName
        val extras = notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val content = extras.getString(Notification.EXTRA_TEXT, "")

        Log.d(TAG, "收到通知 - 包名: $packageName, 标题: $title, 内容: $content")

        // 处理测试通知
        if (packageName == "com.java.vmian" &&
            content == "这是一条测试推送信息，如果程序正常，则会提示监听权限正常") {
            showToast("监听正常，如无法正常回调请联系作者反馈！")
            if (::logManager.isInitialized) {
                logManager.logSystem("通知监听测试成功")
            }
            return
        }

        // 识别支付类型
        val paymentType = paymentUseCase.identifyPaymentType(packageName, title, content)
        if (paymentType != null) {
            // 使用新的金额提取方法，同时检查标题和内容
            val amount = paymentUseCase.extractAmountFromNotification(title, content)
            if (amount != null) {
                val paymentNotification = PaymentNotification(
                    type = paymentType,
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    title = title,
                    content = content,
                    eventId = PaymentEventIdFactory.create(
                        packageName = packageName,
                        notificationKey = sbn.key.orEmpty(),
                        postTime = sbn.postTime,
                        title = title,
                        content = content
                    )
                )

                Log.d(TAG, "识别到${paymentType.name}收款: $amount 元")

                // 记录收款日志
                when (paymentType.name) {
                    "ALIPAY" -> logManager.logPaymentAlipay("检测到收款: ¥$amount")
                    "WECHAT" -> logManager.logPaymentWechat("检测到收款: ¥$amount")
                    else -> logManager.logSystem("检测到${paymentType.name}收款: ¥$amount")
                }

                // 更新前台通知显示最新收款信息
                val paymentTypeName = when (paymentType.name) {
                    "ALIPAY" -> "支付宝"
                    "WECHAT" -> "微信"
                    else -> paymentType.name
                }
                updateForegroundNotification("检测到${paymentTypeName}收款: ¥$amount")

                serviceScope.launch {
                    val result = paymentUseCase.pushPayment(paymentNotification)
                    when (result) {
                        is ApiResponse.Success -> {
                            Log.d(TAG, "推送成功: ${result.data}")
                            logManager.logNetwork("推送支付数据成功: ¥$amount")
                            // 记录推送成功日志
                            pushLogManager.logPushSuccess(paymentTypeName, amount, result.data)
                        }
                        is ApiResponse.Error -> {
                            Log.e(TAG, "推送失败: ${result.message}")
                            logManager.logError("推送支付数据失败: ${result.message}")
                            // 记录推送失败日志
                            pushLogManager.logPushFailed(paymentTypeName, amount, result.message)
                        }
                        else -> {}
                    }
                }
            } else {
                showToast("监听到${paymentType.name}消息但未匹配到金额！")
                if (::logManager.isInitialized) {
                    logManager.logError("监听到${paymentType.name}消息但未匹配到金额")
                }
            }
        }
    }

    /**
     * 启动前台服务
     */
    private fun startForegroundService() {
        createNotificationChannel()
        val notification = createForegroundNotification()

        // Android 14+ 需要指定前台服务类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }

        if (::logManager.isInitialized) {
            logManager.logSystem("前台服务已启动")
        }
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "V免签监听服务",
                NotificationManager.IMPORTANCE_LOW // 低重要性，减少打扰
            ).apply {
                description = "V免签支付监听服务运行状态"
                setShowBadge(false) // 不显示角标
                enableLights(false) // 不闪灯
                enableVibration(false) // 不震动
                setSound(null, null) // 无声音
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    private fun createForegroundNotification(): Notification {
        // 点击通知打开主界面的Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V免签监听服务")
            .setContentText("正在监听支付宝/微信收款通知")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // 设置为持续通知，用户无法滑动删除
            .setAutoCancel(false) // 点击后不自动取消
            .setPriority(NotificationCompat.PRIORITY_LOW) // 低优先级，减少打扰
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * 更新前台服务通知内容
     */
    private fun updateForegroundNotification(content: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V免签监听服务")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
    }

    /**
     * 启动心跳调度
     * 使用 AlarmManager 替代 Coroutines + delay()，能够穿透 Doze 模式
     */
    private fun startHeartbeat() {
        Log.d(TAG, "启动心跳调度机制")
        if (::logManager.isInitialized) {
            logManager.logSystem("启动基于 AlarmManager 的心跳调度")
        }

        // 使用新的心跳调度器
        HeartbeatScheduler.startHeartbeat(applicationContext)
    }

    /**
     * 停止心跳调度
     */
    private fun stopHeartbeat() {
        Log.d(TAG, "停止心跳调度机制")
        if (::logManager.isInitialized) {
            logManager.logSystem("停止心跳调度")
        }

        HeartbeatScheduler.stopHeartbeat(applicationContext)
    }

    /**
     * 注册通知更新广播接收器
     */
    private fun registerNotificationUpdateReceiver() {
        val filter = IntentFilter("com.java.vmian.UPDATE_NOTIFICATION")
        ContextCompat.registerReceiver(
            this,
            notificationUpdateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        Log.d(TAG, "通知更新广播接收器已注册")
    }

    /**
     * 注销通知更新广播接收器
     */
    private fun unregisterNotificationUpdateReceiver() {
        try {
            unregisterReceiver(notificationUpdateReceiver)
            Log.d(TAG, "通知更新广播接收器已注销")
        } catch (e: Exception) {
            Log.e(TAG, "注销通知更新广播接收器失败", e)
        }
    }

    /**
     * 显示Toast消息
     */
    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // 停止心跳调度
        stopHeartbeat()

        // 注销广播接收器
        unregisterNotificationUpdateReceiver()

        // 强制清理 WakeLock
        WakeLockManager.forceCleanup()

        // 取消协程作用域
        serviceScope.cancel()

        // 停止前台服务并移除通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        if (::logManager.isInitialized) {
            logManager.logSystem("通知监听服务已停止")
        }
    }


}
