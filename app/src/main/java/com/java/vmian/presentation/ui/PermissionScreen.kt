package com.java.vmian.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.WarningOrange
import com.java.vmian.domain.model.PermissionStatus
import com.java.vmian.presentation.ui.components.ImmersiveContent
import com.java.vmian.presentation.ui.components.PermissionGuideDialog
import com.java.vmian.presentation.ui.components.PermissionItemCard
import com.java.vmian.util.PermissionUtils

/**
 * 权限设置页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionStatus by remember { mutableStateOf<PermissionStatus?>(null) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var selectedPermission by remember { mutableStateOf<PermissionInfo?>(null) }

    // 无障碍权限状态（独立管理，不影响启动）
    var accessibilityServiceEnabled by remember { mutableStateOf(false) }
    var accessibilityServiceChecked by remember { mutableStateOf(false) }
    var refreshAccessibilityService by remember { mutableStateOf(false) }

    // 运行时权限状态管理
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val notificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    } else null

    // 刷新权限状态的函数
    val refreshPermissions = {
        permissionStatus = PermissionUtils.getAllPermissionStatus(context)
    }

    // 初始化权限状态
    LaunchedEffect(Unit) {
        refreshPermissions()
    }

    // 监听页面生命周期，当从系统设置返回时自动刷新权限状态
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 页面恢复时刷新权限状态，这样用户从系统设置返回时能看到最新状态
                refreshPermissions()
                // 清除无障碍权限缓存，确保获取最新状态
                PermissionUtils.clearAccessibilityServiceCache()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 延迟检测无障碍权限状态，完全独立于启动流程
    LaunchedEffect(Unit) {
        // 延迟5秒后检测无障碍权限，确保页面完全加载
        kotlinx.coroutines.delay(5000)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // 在后台线程检测无障碍权限状态
            val enabled = PermissionUtils.updateAccessibilityServiceStatus(context)

            // 更新无障碍权限状态
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                accessibilityServiceEnabled = enabled
                accessibilityServiceChecked = true
            }
        }
    }

    // 响应刷新请求的检测
    LaunchedEffect(refreshAccessibilityService) {
        if (refreshAccessibilityService && !accessibilityServiceChecked) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                // 在后台线程检测无障碍权限状态
                val enabled = PermissionUtils.updateAccessibilityServiceStatus(context)

                // 更新无障碍权限状态
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    accessibilityServiceEnabled = enabled
                    accessibilityServiceChecked = true
                }
            }
        }
    }

    // 监听页面生命周期，当页面重新获得焦点时刷新权限状态
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 页面重新获得焦点时刷新权限状态
                    refreshPermissions()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 监听权限状态变化，实时更新UI
    LaunchedEffect(cameraPermission.status, notificationPermission?.status) {
        // 当Accompanist权限状态发生变化时，同步刷新PermissionUtils的状态
        refreshPermissions()
    }

    // 沉浸式背景容器
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ImmersiveContent(
            includeStatusBar = false,
            includeNavigationBar = true
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // 状态栏占位
            Spacer(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Top)
                )
            )

            // 固定位置的标题栏设计（与主页面完全一致）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "权限设置",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            permissionStatus?.let { status ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 必需权限
                    item {
                        PermissionSectionHeader(
                            title = "必需权限",
                            subtitle = "应用核心功能必需的权限",
                            importance = PermissionImportance.REQUIRED
                        )
                    }

                    items(
                        status.allPermissions.filter { it.importance == PermissionImportance.REQUIRED }
                    ) { permission ->
                        PermissionItemCard(
                            permission = permission,
                            onSettingsClick = {
                                if (permission.settingsAction?.startsWith("manufacturer_") == true) {
                                    selectedPermission = permission
                                    showGuideDialog = true
                                } else {
                                    handlePermissionSettings(
                                        context = context,
                                        permission = permission,
                                        cameraPermission = cameraPermission,
                                        notificationPermission = notificationPermission,
                                        onRefresh = refreshPermissions
                                    )
                                }
                            },
                            onRefresh = refreshPermissions
                        )
                    }

                    // 推荐权限
                    item {
                        PermissionSectionHeader(
                            title = "推荐权限",
                            subtitle = "提升应用体验的重要权限",
                            importance = PermissionImportance.RECOMMENDED
                        )
                    }

                    items(
                        status.allPermissions.filter { it.importance == PermissionImportance.RECOMMENDED }
                    ) { permission ->
                        PermissionItemCard(
                            permission = permission,
                            onSettingsClick = {
                                if (permission.settingsAction?.startsWith("manufacturer_") == true) {
                                    selectedPermission = permission
                                    showGuideDialog = true
                                } else {
                                    handlePermissionSettings(
                                        context = context,
                                        permission = permission,
                                        cameraPermission = cameraPermission,
                                        notificationPermission = notificationPermission,
                                        onRefresh = refreshPermissions
                                    )
                                }
                            },
                            onRefresh = refreshPermissions
                        )
                    }

                    // 无障碍权限（独立显示，不影响启动）
                    item {
                        AccessibilityPermissionCard(
                            enabled = accessibilityServiceEnabled,
                            checked = accessibilityServiceChecked,
                            onSettingsClick = {
                                PermissionUtils.openAccessibilitySettings(context)
                            },
                            onRefresh = {
                                // 清除缓存
                                PermissionUtils.clearAccessibilityServiceCache()
                                // 重置状态为检测中
                                accessibilityServiceChecked = false
                                // 触发重新检测（通过设置一个标志）
                                refreshAccessibilityService = !refreshAccessibilityService
                            }
                        )
                    }

                    // 可选权限
                    val optionalPermissions = status.allPermissions.filter { 
                        it.importance == PermissionImportance.OPTIONAL 
                    }
                    
                    if (optionalPermissions.isNotEmpty()) {
                        item {
                            PermissionSectionHeader(
                                title = "可选权限",
                                subtitle = "增强功能的可选权限",
                                importance = PermissionImportance.OPTIONAL
                            )
                        }

                        items(optionalPermissions) { permission ->
                            PermissionItemCard(
                                permission = permission,
                                onSettingsClick = {
                                    if (permission.settingsAction?.startsWith("manufacturer_") == true) {
                                        selectedPermission = permission
                                        showGuideDialog = true
                                    } else {
                                        handlePermissionSettings(
                                            context = context,
                                            permission = permission,
                                            cameraPermission = cameraPermission,
                                            notificationPermission = notificationPermission,
                                            onRefresh = refreshPermissions
                                        )
                                    }
                                },
                                onRefresh = refreshPermissions
                            )
                        }
                    }
                }
            } ?: run {
                // 加载状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // 权限设置指导对话框
    if (showGuideDialog && selectedPermission != null) {
        PermissionGuideDialog(
            permission = selectedPermission!!,
            onDismiss = { showGuideDialog = false },
            onOpenSettings = { permission ->
                PermissionUtils.openManufacturerSettings(context, permission.id)
                showGuideDialog = false
            }
        )
    }
    }
}



/**
 * 权限分组标题 - 简约设计版本
 */
@Composable
private fun PermissionSectionHeader(
    title: String,
    subtitle: String,
    importance: PermissionImportance
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 重要性指示器
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = when (importance) {
                    PermissionImportance.REQUIRED -> MaterialTheme.colorScheme.errorContainer
                    PermissionImportance.RECOMMENDED -> MaterialTheme.colorScheme.primaryContainer
                    PermissionImportance.OPTIONAL -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = importance.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (importance) {
                        PermissionImportance.REQUIRED -> MaterialTheme.colorScheme.onErrorContainer
                        PermissionImportance.RECOMMENDED -> MaterialTheme.colorScheme.onPrimaryContainer
                        PermissionImportance.OPTIONAL -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 通用的运行时权限申请处理函数
 * 解决Accompanist状态缓存问题，确保权限申请的可靠性
 * 正确处理Android权限永久拒绝状态
 * 支持不同权限类型的差异化处理
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun handleRuntimePermissionRequest(
    context: android.content.Context,
    permission: PermissionInfo,
    permissionState: com.google.accompanist.permissions.PermissionState,
    androidPermission: String,
    permissionDescription: String
) {
    // 使用实时权限状态检查，避免Accompanist缓存问题
    val actualPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        androidPermission
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    // 检测权限是否被永久拒绝
    // 永久拒绝的条件：权限未授权 且 不需要显示说明理由
    val isPermanentlyDenied = !actualPermissionGranted &&
                             !permissionState.status.shouldShowRationale

    // 添加调试信息
    android.util.Log.d("PermissionScreen",
        "${permission.name}状态 - Accompanist: ${permissionState.status.isGranted}, " +
        "实际状态: $actualPermissionGranted, " +
        "shouldShowRationale: ${permissionState.status.shouldShowRationale}, " +
        "永久拒绝: $isPermanentlyDenied"
    )

    // 根据权限类型和状态选择最佳的处理策略
    when {
        actualPermissionGranted -> {
            // 权限已授权的处理
            handleGrantedPermission(context, permission, androidPermission)
        }
        isPermanentlyDenied -> {
            // 权限被永久拒绝的处理
            handlePermanentlyDeniedPermission(context, permission, permissionState, androidPermission, permissionDescription)
        }
        permissionState.status.shouldShowRationale -> {
            // 需要显示权限说明，然后申请权限
            android.widget.Toast.makeText(
                context,
                "$permissionDescription，请允许权限申请",
                android.widget.Toast.LENGTH_LONG
            ).show()
            permissionState.launchPermissionRequest()
        }
        else -> {
            // 首次申请权限
            android.widget.Toast.makeText(
                context,
                "正在申请${permission.name}，$permissionDescription",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            permissionState.launchPermissionRequest()
        }
    }
}

/**
 * 处理已授权权限的逻辑
 */
private fun handleGrantedPermission(
    context: android.content.Context,
    permission: PermissionInfo,
    androidPermission: String
) {
    when (androidPermission) {
        android.Manifest.permission.CAMERA -> {
            // 相机权限已授权，提示用户可以重新申请以确保状态同步
            android.widget.Toast.makeText(
                context,
                "${permission.name}已授权，正在重新申请以确保状态同步",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            // 对于相机权限，即使已授权也可以重新申请，不需要跳转设置
        }
        else -> {
            // 其他权限已授权，跳转到设置页面进行管理
            android.widget.Toast.makeText(
                context,
                "${permission.name}已授权，正在跳转到设置页面进行管理",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            jumpToPermissionSettings(context, androidPermission)
        }
    }
}

/**
 * 处理被永久拒绝权限的逻辑
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun handlePermanentlyDeniedPermission(
    context: android.content.Context,
    permission: PermissionInfo,
    permissionState: com.google.accompanist.permissions.PermissionState,
    androidPermission: String,
    permissionDescription: String
) {
    when (androidPermission) {
        android.Manifest.permission.CAMERA -> {
            // 相机权限被永久拒绝，但仍然可以尝试直接申请
            android.widget.Toast.makeText(
                context,
                "${permission.name}需要重新授权，$permissionDescription",
                android.widget.Toast.LENGTH_LONG
            ).show()
            // 相机权限即使被永久拒绝也可以重新申请，不需要强制跳转设置
            permissionState.launchPermissionRequest()
        }
        else -> {
            // 其他权限被永久拒绝，跳转到系统设置页面
            android.widget.Toast.makeText(
                context,
                "${permission.name}需要在系统设置中手动开启，正在跳转到设置页面",
                android.widget.Toast.LENGTH_LONG
            ).show()
            jumpToPermissionSettings(context, androidPermission)
        }
    }
}

/**
 * 跳转到特定权限的系统设置页面
 */
private fun jumpToPermissionSettings(context: android.content.Context, androidPermission: String) {
    when (androidPermission) {
        android.Manifest.permission.CAMERA -> {
            PermissionUtils.openAppDetailsSettings(context)
        }
        android.Manifest.permission.POST_NOTIFICATIONS -> {
            PermissionUtils.openNotificationPermissionSettings(context)
        }
        else -> {
            PermissionUtils.openAppDetailsSettings(context)
        }
    }
}

/**
 * 处理权限设置点击
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun handlePermissionSettings(
    context: android.content.Context,
    permission: PermissionInfo,
    cameraPermission: com.google.accompanist.permissions.PermissionState,
    notificationPermission: com.google.accompanist.permissions.PermissionState?,
    onRefresh: () -> Unit
) {
    when {
        // 处理运行时权限申请
        permission.canRequest && permission.id == "camera" -> {
            handleRuntimePermissionRequest(
                context = context,
                permission = permission,
                permissionState = cameraPermission,
                androidPermission = android.Manifest.permission.CAMERA,
                permissionDescription = "相机权限用于扫描二维码配置"
            )
        }

        permission.canRequest && permission.id == "post_notifications" -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermission?.let { notifPerm ->
                    handleRuntimePermissionRequest(
                        context = context,
                        permission = permission,
                        permissionState = notifPerm,
                        androidPermission = android.Manifest.permission.POST_NOTIFICATIONS,
                        permissionDescription = "通知权限用于显示前台服务状态"
                    )
                } ?: run {
                    // 降级到设置页面
                    PermissionUtils.openNotificationPermissionSettings(context)
                }
            } else {
                // Android 13以下版本跳转到应用详情
                PermissionUtils.openAppDetailsSettings(context)
            }
        }

        // 处理系统设置跳转
        permission.settingsAction == "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" -> {
            PermissionUtils.openNotificationListenerSettings(context)
        }

        permission.settingsAction == "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> {
            PermissionUtils.requestIgnoreBatteryOptimization(context)
        }

        permission.settingsAction == "android.settings.APP_NOTIFICATION_SETTINGS" -> {
            PermissionUtils.openNotificationPermissionSettings(context)
        }

        permission.settingsAction == "android.settings.ACCESSIBILITY_SETTINGS" -> {
            PermissionUtils.openAccessibilitySettings(context)
        }

        else -> {
            // 默认跳转到应用详情页面
            PermissionUtils.openAppDetailsSettings(context)
        }
    }
}

/**
 * 无障碍权限卡片（独立组件，不影响启动）
 * 设计与标准权限卡片保持一致
 */
@Composable
private fun AccessibilityPermissionCard(
    enabled: Boolean,
    checked: Boolean,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 权限标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 状态图标
                    Icon(
                        imageVector = when {
                            checked && enabled -> Icons.Default.CheckCircle
                            checked -> Icons.Default.Warning
                            else -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        tint = when {
                            checked && enabled -> SuccessGreen
                            checked -> WarningOrange
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    // 权限名称
                    Text(
                        text = "无障碍服务",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 重要性标签
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "推荐",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }

            // 权限描述
            Text(
                text = "提高应用保活成功率，确保支付监听服务稳定运行。该服务仅用于保活，不会读取或处理任何用户数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 状态和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态文本
                Text(
                    text = when {
                        !checked -> "检测中..."
                        enabled -> "已启用"
                        else -> "未启用"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        !checked -> MaterialTheme.colorScheme.primary
                        enabled -> SuccessGreen
                        else -> WarningOrange
                    },
                    fontWeight = FontWeight.Medium
                )

                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 刷新按钮
                    TextButton(
                        onClick = onRefresh,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "刷新",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // 设置按钮
                    Button(
                        onClick = onSettingsClick,
                        enabled = checked,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (enabled) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (enabled) "管理权限" else "设置",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 特殊提示
            if (checked && !enabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "首次启用时系统会自动回到桌面，这是正常行为",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
