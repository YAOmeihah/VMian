package com.java.vmian.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.java.vmian.domain.model.ManufacturerType
import com.java.vmian.domain.model.PermissionGuide
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.domain.model.PermissionStatus


/**
 * 权限管理工具类
 */
object PermissionUtils {
    
    /**
     * 检查通知监听权限是否已开启
     */
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )

        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null && packageName == componentName.packageName) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 打开通知监听设置页面
     */
    fun openNotificationListenerSettings(context: Context) {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent().apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                    )
                    putExtra(":settings:show_fragment", "NotificationAccessSettings")
                }
                context.startActivity(intent)
            } catch (e1: Exception) {
                // 最后尝试打开通用设置页面
                try {
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                    Toast.makeText(context, "请在设置中查找通知使用权或通知访问权限", Toast.LENGTH_LONG).show()
                } catch (e2: Exception) {
                    Toast.makeText(context, "无法打开通知监听设置", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 打开通知权限设置页面 (Android 13+)
     */
    fun openNotificationPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // 降级到应用详情页面
                openAppDetailsSettings(context)
            }
        } else {
            // Android 13以下版本通知权限默认授予，跳转到应用详情
            openAppDetailsSettings(context)
        }
    }

    /**
     * 打开应用详情设置页面
     */
    fun openAppDetailsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开应用设置", Toast.LENGTH_SHORT).show()
        }
    }



    // 无障碍权限检测结果缓存
    private var accessibilityServiceCache: Boolean? = null
    private var lastAccessibilityCheckTime: Long = 0
    private const val ACCESSIBILITY_CACHE_DURATION = 5000L // 5秒缓存

    /**
     * 检查无障碍服务是否已启用 - 优化版
     * 使用缓存机制，避免频繁的系统调用
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        // 检查缓存是否有效
        val currentTime = System.currentTimeMillis()
        if (accessibilityServiceCache != null &&
            currentTime - lastAccessibilityCheckTime < ACCESSIBILITY_CACHE_DURATION) {
            return accessibilityServiceCache!!
        }

        // 执行检测
        val result = try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
                as android.view.accessibility.AccessibilityManager

            // 使用AccessibilityManager API，比Settings.Secure更高效
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )

            // 简化的服务名称检查
            val packageName = context.packageName
            val targetServiceName = "KeepAliveAccessibilityService"

            enabledServices.any { serviceInfo ->
                serviceInfo.id.contains(packageName) &&
                serviceInfo.id.contains(targetServiceName)
            }
        } catch (e: Exception) {
            // 简化错误处理，返回false
            false
        }

        // 更新缓存
        accessibilityServiceCache = result
        lastAccessibilityCheckTime = currentTime

        return result
    }

    /**
     * 清除无障碍权限检测缓存
     * 在权限状态可能发生变化时调用
     */
    fun clearAccessibilityServiceCache() {
        accessibilityServiceCache = null
        lastAccessibilityCheckTime = 0
    }

    /**
     * 打开无障碍服务设置页面
     */
    fun openAccessibilitySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                Toast.makeText(context, "请在设置中查找无障碍功能", Toast.LENGTH_LONG).show()
            } catch (e2: Exception) {
                Toast.makeText(context, "无法打开无障碍设置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 发送测试通知
     */
    fun sendTestNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "test_channel",
                "测试通道",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "test_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("V免签测试推送")
            .setContentText("这是一条测试推送信息，如果程序正常，则会提示监听权限正常")
            .build()

        notificationManager.notify(1001, notification)
    }

    // ==================== 完整权限管理系统 ====================

    /**
     * 获取所有权限状态
     */
    fun getAllPermissionStatus(context: Context): PermissionStatus {
        val permissions = getAllPermissions(context)
        val requiredGranted = permissions.filter { it.importance == PermissionImportance.REQUIRED }
            .all { it.isGranted }
        val recommendedGranted = permissions.filter { it.importance == PermissionImportance.RECOMMENDED }
            .all { it.isGranted }
        val allGranted = permissions.all { it.isGranted }

        return PermissionStatus(
            allPermissions = permissions,
            requiredPermissionsGranted = requiredGranted,
            recommendedPermissionsGranted = recommendedGranted,
            allPermissionsGranted = allGranted
        )
    }

    /**
     * 延迟检测无障碍权限状态
     * 在应用完全启动后调用，避免影响启动性能
     */
    suspend fun updateAccessibilityServiceStatus(context: Context): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // 在后台线程执行检测
            isAccessibilityServiceEnabled(context)
        }
    }

    /**
     * 获取所有权限信息
     */
    private fun getAllPermissions(context: Context): List<PermissionInfo> {
        val manufacturer = detectManufacturer(context)

        return listOf(
            // 必需权限
            PermissionInfo(
                id = "notification_listener",
                name = "通知监听权限",
                description = "监听支付宝和微信的收款通知，这是应用的核心功能",
                importance = PermissionImportance.REQUIRED,
                isGranted = isNotificationListenerEnabled(context),
                canRequest = false,
                settingsAction = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
            ),
            PermissionInfo(
                id = "post_notifications",
                name = "通知权限",
                description = "显示前台服务通知，保持应用在后台运行",
                importance = PermissionImportance.REQUIRED,
                isGranted = isNotificationPermissionGranted(context),
                canRequest = true,
                settingsAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    "android.settings.APP_NOTIFICATION_SETTINGS"
                } else null
            ),
            PermissionInfo(
                id = "battery_optimization",
                name = "电池优化白名单",
                description = "防止系统在后台杀死应用，确保收款监听稳定运行。包含后台运行、省电策略等保护。",
                importance = PermissionImportance.REQUIRED,
                isGranted = isBatteryOptimizationIgnored(context),
                canRequest = false,
                settingsAction = "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"
            ),

            // 推荐权限
            PermissionInfo(
                id = "camera",
                name = "相机权限",
                description = "用于扫描二维码进行快速配置",
                importance = PermissionImportance.RECOMMENDED,
                isGranted = isCameraPermissionGranted(context),
                canRequest = true
            ),
            PermissionInfo(
                id = "auto_start",
                name = "自启动管理",
                description = "允许应用开机自启动，确保重启后自动恢复监听。注意：此项无法自动检测，请手动确认设置状态。",
                importance = PermissionImportance.RECOMMENDED,
                isGranted = false, // 无法检测，始终显示为需要设置
                canRequest = false,
                settingsAction = "manufacturer_auto_start"
            )
        )
    }

    /**
     * 检查通知权限是否已授权
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Android 13以下默认有通知权限
        }
    }

    /**
     * 检查相机权限是否已授权
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查是否在电池优化白名单中
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Android 6.0以下没有电池优化
        }
    }

    /**
     * 请求忽略电池优化
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // 如果直接请求失败，跳转到电池优化设置页面
                openBatteryOptimizationSettings(context)
            }
        }
    }

    /**
     * 打开电池优化设置页面
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开电池优化设置", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== 厂商系统适配 ====================

    /**
     * 检测当前设备的厂商类型
     */
    fun detectManufacturer(context: Context): ManufacturerType {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val brand = Build.BRAND.lowercase()

        return when {
            manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
            brand.contains("redmi") || brand.contains("poco") -> ManufacturerType.XIAOMI

            manufacturer.contains("huawei") || brand.contains("huawei") ||
            brand.contains("honor") -> ManufacturerType.HUAWEI

            manufacturer.contains("oppo") || brand.contains("oppo") ||
            brand.contains("oneplus") -> {
                if (brand.contains("oneplus")) ManufacturerType.ONEPLUS else ManufacturerType.OPPO
            }

            manufacturer.contains("vivo") || brand.contains("vivo") ||
            brand.contains("iqoo") -> ManufacturerType.VIVO

            manufacturer.contains("samsung") || brand.contains("samsung") -> ManufacturerType.SAMSUNG

            manufacturer.contains("meizu") || brand.contains("meizu") -> ManufacturerType.MEIZU

            else -> ManufacturerType.GENERIC
        }
    }

    /**
     * 获取厂商特定的权限设置指导
     */
    fun getManufacturerGuide(context: Context, permissionId: String): PermissionGuide? {
        val manufacturer = detectManufacturer(context)

        return when (permissionId) {
            "auto_start" -> getAutoStartGuide(manufacturer)
            "battery_optimization" -> getBatteryOptimizationGuide(manufacturer)
            else -> null
        }
    }

    /**
     * 获取自启动权限设置指导
     */
    private fun getAutoStartGuide(manufacturer: ManufacturerType): PermissionGuide {
        return when (manufacturer) {
            ManufacturerType.XIAOMI -> PermissionGuide(
                title = "小米/红米自启动设置",
                steps = listOf(
                    "打开「安全中心」应用",
                    "选择「应用管理」",
                    "点击「自启动管理」",
                    "找到「V免签」应用",
                    "开启自启动开关"
                ),
                notes = listOf("部分MIUI版本路径可能略有不同"),
                warningMessage = "关闭自启动可能导致重启后需要手动开启应用"
            )

            ManufacturerType.HUAWEI -> PermissionGuide(
                title = "华为/荣耀自启动设置",
                steps = listOf(
                    "打开「手机管家」应用",
                    "选择「应用启动管理」",
                    "找到「V免签」应用",
                    "关闭「自动管理」",
                    "开启「允许自启动」、「允许关联启动」、「允许后台活动」"
                ),
                warningMessage = "必须关闭自动管理才能手动设置启动权限"
            )

            ManufacturerType.OPPO -> PermissionGuide(
                title = "OPPO自启动设置",
                steps = listOf(
                    "打开「手机管家」应用",
                    "选择「权限隐私」",
                    "点击「自启动管理」",
                    "找到「V免签」应用",
                    "开启自启动开关"
                )
            )

            ManufacturerType.VIVO -> PermissionGuide(
                title = "vivo自启动设置",
                steps = listOf(
                    "打开「i管家」应用",
                    "选择「应用管理」",
                    "点击「自启动管理」",
                    "找到「V免签」应用",
                    "开启自启动开关"
                )
            )

            ManufacturerType.SAMSUNG -> PermissionGuide(
                title = "三星自启动设置",
                steps = listOf(
                    "打开「设置」应用",
                    "选择「设备保养」",
                    "点击「电池」",
                    "选择「应用电源管理」",
                    "找到「V免签」应用",
                    "设置为「不优化」"
                )
            )

            else -> PermissionGuide(
                title = "通用自启动设置",
                steps = listOf(
                    "打开「设置」应用",
                    "查找「应用管理」或「应用权限」",
                    "找到「V免签」应用",
                    "开启相关的自启动或后台运行权限"
                ),
                notes = listOf("不同厂商的设置路径可能有所不同")
            )
        }
    }



    /**
     * 获取电池优化设置指导
     */
    private fun getBatteryOptimizationGuide(manufacturer: ManufacturerType): PermissionGuide {
        return when (manufacturer) {
            ManufacturerType.XIAOMI -> PermissionGuide(
                title = "小米电池优化和后台限制设置",
                steps = listOf(
                    "方法一：电池优化白名单",
                    "1. 点击下方按钮跳转到电池优化设置",
                    "2. 找到「V免签」应用，选择「不优化」",
                    "",
                    "方法二：应用省电策略（推荐）",
                    "1. 打开「设置」→「应用设置」→「应用管理」",
                    "2. 找到「V免签」应用",
                    "3. 点击「省电策略」，选择「无限制」"
                ),
                notes = listOf("建议两种方法都设置，确保应用稳定运行"),
                warningMessage = "小米系统的电池优化较为严格，请务必完成设置"
            )

            ManufacturerType.HUAWEI -> PermissionGuide(
                title = "华为电池优化和后台管理设置",
                steps = listOf(
                    "方法一：电池优化白名单",
                    "1. 点击下方按钮跳转到电池优化设置",
                    "2. 找到「V免签」应用，选择「不优化」",
                    "",
                    "方法二：应用电池管理",
                    "1. 打开「设置」→「应用和服务」→「应用管理」",
                    "2. 找到「V免签」应用",
                    "3. 点击「电池」，选择「不限制」"
                ),
                warningMessage = "华为系统需要同时设置电池优化和应用启动管理"
            )

            else -> PermissionGuide(
                title = "电池优化白名单设置",
                steps = listOf(
                    "1. 点击下方按钮跳转到电池优化设置",
                    "2. 在列表中找到「V免签」应用",
                    "3. 选择「不优化」或「允许」",
                    "4. 确认设置并返回"
                ),
                notes = listOf("此设置包含了大部分后台运行保护"),
                warningMessage = "电池优化可能导致应用在后台被系统杀死"
            )
        }
    }

    /**
     * 尝试打开厂商特定的设置页面
     */
    fun openManufacturerSettings(context: Context, permissionId: String) {
        val manufacturer = detectManufacturer(context)

        when (permissionId) {
            "auto_start" -> openAutoStartSettings(context, manufacturer)
            else -> {
                Toast.makeText(context, "请手动前往系统设置进行配置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 打开自启动设置页面
     */
    private fun openAutoStartSettings(context: Context, manufacturer: ManufacturerType) {
        val intents = when (manufacturer) {
            ManufacturerType.XIAOMI -> listOf(
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.securitycenter.permission.AppPermissionsEditorActivity"))
            )

            ManufacturerType.HUAWEI -> listOf(
                Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
                Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"))
            )

            ManufacturerType.OPPO -> listOf(
                Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
                Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"))
            )

            ManufacturerType.VIVO -> listOf(
                Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
                Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"))
            )

            else -> emptyList()
        }

        var success = false
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                success = true
                break
            } catch (e: Exception) {
                // 继续尝试下一个Intent
            }
        }

        if (!success) {
            Toast.makeText(context, "无法打开自启动设置，请手动前往系统设置", Toast.LENGTH_LONG).show()
        }
    }

}
