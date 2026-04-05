package com.java.vmian.domain.model

/**
 * 权限信息数据类
 */
data class PermissionInfo(
    val id: String,
    val name: String,
    val description: String,
    val importance: PermissionImportance,
    val isGranted: Boolean,
    val canRequest: Boolean = true,
    val settingsAction: String? = null
)

/**
 * 权限重要性级别
 */
enum class PermissionImportance(
    val displayName: String,
    val description: String
) {
    REQUIRED("必需", "应用核心功能必需的权限"),
    RECOMMENDED("推荐", "提升应用体验的重要权限"),
    OPTIONAL("可选", "增强功能的可选权限")
}

/**
 * 权限状态
 */
data class PermissionStatus(
    val allPermissions: List<PermissionInfo>,
    val requiredPermissionsGranted: Boolean,
    val recommendedPermissionsGranted: Boolean,
    val allPermissionsGranted: Boolean
) {
    val grantedCount: Int = allPermissions.count { it.isGranted }
    val totalCount: Int = allPermissions.size
    val grantedPercentage: Float = if (totalCount > 0) grantedCount.toFloat() / totalCount else 0f
}

/**
 * 厂商系统类型
 */
enum class ManufacturerType(
    val displayName: String,
    val packageNames: List<String>
) {
    XIAOMI("小米/红米", listOf("com.miui.securitycenter", "com.miui.powerkeeper")),
    HUAWEI("华为/荣耀", listOf("com.huawei.systemmanager", "com.huawei.powergenie")),
    OPPO("OPPO", listOf("com.coloros.safecenter", "com.oppo.safe")),
    VIVO("vivo", listOf("com.iqoo.secure", "com.vivo.permissionmanager")),
    SAMSUNG("三星", listOf("com.samsung.android.sm", "com.samsung.android.sm_cn")),
    ONEPLUS("一加", listOf("com.oneplus.security", "com.oplus.battery")),
    MEIZU("魅族", listOf("com.meizu.safe", "com.meizu.flyme.launcher")),
    GENERIC("其他", emptyList())
}

/**
 * 权限设置指导信息
 */
data class PermissionGuide(
    val title: String,
    val steps: List<String>,
    val notes: List<String> = emptyList(),
    val warningMessage: String? = null
)
