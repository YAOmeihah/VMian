package com.java.vmian.util

import android.content.Context
import android.content.SharedPreferences
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo

/**
 * 权限检查管理器
 * 负责应用启动时的权限检查和用户偏好管理
 */
class PermissionCheckManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "permission_check_prefs"
        private const val KEY_NEVER_REMIND = "never_remind_permissions"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_REMIND_LATER_COUNT = "remind_later_count"
        
        // 稍后提醒的间隔时间（毫秒）
        private const val REMIND_LATER_INTERVAL = 24 * 60 * 60 * 1000L // 24小时
        
        // 最大稍后提醒次数
        private const val MAX_REMIND_LATER_COUNT = 3
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * 检查是否需要显示权限检查对话框
     */
    fun shouldShowPermissionCheck(): Boolean {
        // 如果用户选择了不再提醒，则不显示
        if (isNeverRemindEnabled()) {
            return false
        }
        
        // 检查是否在稍后提醒的时间间隔内
        if (isInRemindLaterInterval()) {
            return false
        }
        
        // 检查是否有必需权限未授权
        val permissionStatus = PermissionUtils.getAllPermissionStatus(context)
        return !permissionStatus.requiredPermissionsGranted
    }
    
    /**
     * 获取缺失的必需权限列表
     */
    fun getMissingRequiredPermissions(): List<PermissionInfo> {
        val permissionStatus = PermissionUtils.getAllPermissionStatus(context)
        return permissionStatus.allPermissions.filter { 
            it.importance == PermissionImportance.REQUIRED && !it.isGranted 
        }
    }
    
    /**
     * 检查是否有必需权限缺失
     */
    fun hasRequiredPermissionsMissing(): Boolean {
        val permissionStatus = PermissionUtils.getAllPermissionStatus(context)
        return !permissionStatus.requiredPermissionsGranted
    }
    
    /**
     * 用户选择稍后提醒
     */
    fun onRemindLater() {
        val currentTime = System.currentTimeMillis()
        val remindLaterCount = getRemindLaterCount() + 1
        
        prefs.edit()
            .putLong(KEY_LAST_CHECK_TIME, currentTime)
            .putInt(KEY_REMIND_LATER_COUNT, remindLaterCount)
            .apply()
        
        // 如果稍后提醒次数达到上限，自动设置为不再提醒
        if (remindLaterCount >= MAX_REMIND_LATER_COUNT) {
            setNeverRemind(true)
        }
    }
    
    /**
     * 用户选择不再提醒
     */
    fun onNeverRemind() {
        setNeverRemind(true)
    }
    
    /**
     * 用户前往设置页面
     */
    fun onGoToSettings() {
        // 重置稍后提醒计数，因为用户主动去设置了
        prefs.edit()
            .putInt(KEY_REMIND_LATER_COUNT, 0)
            .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * 重置权限检查状态（用于测试或重新启用提醒）
     */
    fun resetPermissionCheckState() {
        prefs.edit()
            .remove(KEY_NEVER_REMIND)
            .remove(KEY_LAST_CHECK_TIME)
            .remove(KEY_REMIND_LATER_COUNT)
            .apply()
    }
    
    /**
     * 检查是否启用了不再提醒
     */
    private fun isNeverRemindEnabled(): Boolean {
        return prefs.getBoolean(KEY_NEVER_REMIND, false)
    }
    
    /**
     * 设置不再提醒状态
     */
    private fun setNeverRemind(neverRemind: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NEVER_REMIND, neverRemind)
            .apply()
    }
    
    /**
     * 检查是否在稍后提醒的时间间隔内
     */
    private fun isInRemindLaterInterval(): Boolean {
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastCheckTime) < REMIND_LATER_INTERVAL
    }
    
    /**
     * 获取稍后提醒的次数
     */
    private fun getRemindLaterCount(): Int {
        return prefs.getInt(KEY_REMIND_LATER_COUNT, 0)
    }
    
    /**
     * 获取权限检查统计信息
     */
    fun getPermissionCheckStats(): PermissionCheckStats {
        return PermissionCheckStats(
            isNeverRemindEnabled = isNeverRemindEnabled(),
            remindLaterCount = getRemindLaterCount(),
            lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0),
            nextCheckTime = if (isInRemindLaterInterval()) {
                prefs.getLong(KEY_LAST_CHECK_TIME, 0) + REMIND_LATER_INTERVAL
            } else 0
        )
    }
    
    /**
     * 检查是否应该显示"不再提醒"选项
     */
    fun shouldShowNeverRemindOption(): Boolean {
        // 如果用户已经选择稍后提醒超过2次，显示不再提醒选项
        return getRemindLaterCount() >= 2
    }
    
    /**
     * 强制检查权限（忽略时间间隔和用户偏好）
     */
    fun forceCheckPermissions(): List<PermissionInfo> {
        return getMissingRequiredPermissions()
    }
}

/**
 * 权限检查统计信息
 */
data class PermissionCheckStats(
    val isNeverRemindEnabled: Boolean,
    val remindLaterCount: Int,
    val lastCheckTime: Long,
    val nextCheckTime: Long
)

/**
 * 权限检查结果
 */
sealed class PermissionCheckResult {
    object NoCheckNeeded : PermissionCheckResult()
    data class ShowDialog(val missingPermissions: List<PermissionInfo>) : PermissionCheckResult()
    data class ShowSimpleDialog(val missingCount: Int) : PermissionCheckResult()
}

/**
 * 权限检查工具扩展
 */
object PermissionCheckUtils {
    
    /**
     * 执行权限检查并返回结果
     */
    fun checkPermissions(context: Context): PermissionCheckResult {
        val manager = PermissionCheckManager(context)
        
        if (!manager.shouldShowPermissionCheck()) {
            return PermissionCheckResult.NoCheckNeeded
        }
        
        val missingPermissions = manager.getMissingRequiredPermissions()
        
        return when {
            missingPermissions.isEmpty() -> PermissionCheckResult.NoCheckNeeded
            missingPermissions.size <= 2 -> PermissionCheckResult.ShowSimpleDialog(missingPermissions.size)
            else -> PermissionCheckResult.ShowDialog(missingPermissions)
        }
    }
    
    /**
     * 获取权限检查的友好描述
     */
    fun getPermissionCheckDescription(missingPermissions: List<PermissionInfo>): String {
        return when (missingPermissions.size) {
            0 -> "所有必需权限已配置"
            1 -> "缺少 1 个必需权限：${missingPermissions.first().name}"
            else -> "缺少 ${missingPermissions.size} 个必需权限"
        }
    }
    
    /**
     * 检查特定权限是否为关键权限
     */
    fun isCriticalPermission(permissionId: String): Boolean {
        return when (permissionId) {
            "notification_listener" -> true  // 通知监听是最关键的
            "battery_optimization" -> true  // 电池优化也很重要
            else -> false
        }
    }
}
