package com.java.vmian.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.java.vmian.R
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.WarningOrange
import com.java.vmian.domain.model.PermissionStatus
import com.java.vmian.presentation.ui.components.PermissionGuideDialog
import com.java.vmian.presentation.ui.components.PermissionItemCard
import com.java.vmian.presentation.ui.components.PermissionSummaryHero
import com.java.vmian.presentation.ui.components.AppCardDefaults
import com.java.vmian.presentation.ui.model.KeepAliveControlUiModel
import com.java.vmian.util.KeepAliveController
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
    val resources = context.resources
    val snackbarHostState = remember { SnackbarHostState() }
    var permissionStatus by remember { mutableStateOf<PermissionStatus?>(null) }
    var showGuideDialog by remember { mutableStateOf(false) }
    var selectedPermission by remember { mutableStateOf<PermissionInfo?>(null) }
    var transientMessage by remember { mutableStateOf<String?>(null) }
    var keepAliveModel by remember { mutableStateOf(KeepAliveController.currentUiModel(context)) }
    var waitingForOverlayPermission by remember { mutableStateOf(false) }
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

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
    val refreshKeepAliveStatus = {
        keepAliveModel = KeepAliveController.currentUiModel(context)
    }
    val handlePermissionClick: (PermissionInfo) -> Unit = { permission ->
        if (permission.settingsAction?.startsWith("manufacturer_") == true) {
            selectedPermission = permission
            showGuideDialog = true
        } else {
            handlePermissionSettings(
                context = context,
                permission = permission,
                cameraPermission = cameraPermission,
                notificationPermission = notificationPermission,
                resources = resources,
                onFeedback = { transientMessage = it }
            )
        }
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
                refreshKeepAliveStatus()
                if (waitingForOverlayPermission) {
                    waitingForOverlayPermission = false
                    transientMessage = if (KeepAliveController.canDrawOverlays(context)) {
                        context.getString(R.string.keepalive_permission_prefix, "已授权")
                    } else {
                        context.getString(R.string.keepalive_overlay_missing_permission_feedback)
                    }
                }
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
                    refreshKeepAliveStatus()
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

    transientMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            transientMessage = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.permission_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .windowInsetsPadding(
                        WindowInsets.systemBars.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        )
                    )
            ) {
                permissionStatus?.let { status ->
                        val requiredPermissions = status.allPermissions.filter {
                            it.importance == PermissionImportance.REQUIRED
                        }
                        val recommendedPermissions = status.allPermissions.filter {
                            it.importance == PermissionImportance.RECOMMENDED
                        }
                        val optionalPermissions = status.allPermissions.filter {
                            it.importance == PermissionImportance.OPTIONAL
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                PermissionSummaryHero(status = status)
                            }

                            item {
                                PermissionSectionHeader(
                                    title = stringResource(
                                        R.string.permission_group_required_count,
                                        requiredPermissions.size
                                    ),
                                    importance = PermissionImportance.REQUIRED
                                )
                            }

                            items(requiredPermissions) { permission ->
                                PermissionItemCard(
                                    permission = permission,
                                    onSettingsClick = { handlePermissionClick(permission) }
                                )
                            }

                            item {
                                PermissionSectionHeader(
                                    title = stringResource(
                                        R.string.permission_group_recommended_count,
                                        recommendedPermissions.size
                                    ),
                                    importance = PermissionImportance.RECOMMENDED
                                )
                            }

                            items(recommendedPermissions) { permission ->
                                PermissionItemCard(
                                    permission = permission,
                                    onSettingsClick = { handlePermissionClick(permission) }
                                )
                            }

                            item {
                                AccessibilityPermissionCard(
                                    enabled = accessibilityServiceEnabled,
                                    checked = accessibilityServiceChecked,
                                    onSettingsClick = {
                                        PermissionUtils.openAccessibilitySettings(context)
                                    },
                                    onRefresh = {
                                        PermissionUtils.clearAccessibilityServiceCache()
                                        accessibilityServiceChecked = false
                                        refreshAccessibilityService = !refreshAccessibilityService
                                    }
                                )
                            }

                            item {
                                KeepAliveControlsCard(
                                    model = keepAliveModel,
                                    onMediaChanged = { enabled ->
                                        keepAliveModel = KeepAliveController.setMediaEnabled(context, enabled)
                                        transientMessage = context.getString(
                                            if (enabled) {
                                                R.string.keepalive_media_enabled_feedback
                                            } else {
                                                R.string.keepalive_media_disabled_feedback
                                            }
                                        )
                                    },
                                    onOpenOverlayPermission = {
                                        waitingForOverlayPermission = true
                                        transientMessage = context.getString(
                                            R.string.keepalive_overlay_permission_feedback
                                        )
                                        KeepAliveController.openOverlayPermissionSettings(context)
                                    },
                                    onOverlayChanged = { enabled ->
                                        if (enabled && !KeepAliveController.canDrawOverlays(context)) {
                                            transientMessage = context.getString(
                                                R.string.keepalive_overlay_missing_permission_feedback
                                            )
                                            keepAliveModel = KeepAliveController.currentUiModel(context)
                                        } else {
                                            keepAliveModel = KeepAliveController.setOverlayEnabled(context, enabled)
                                            transientMessage = context.getString(
                                                if (enabled) {
                                                    R.string.keepalive_overlay_enabled_feedback
                                                } else {
                                                    R.string.keepalive_overlay_disabled_feedback
                                                }
                                            )
                                        }
                                    },
                                    onRefresh = {
                                        refreshKeepAliveStatus()
                                        transientMessage = context.getString(R.string.keepalive_status_refreshed)
                                    }
                                )
                            }

                            if (optionalPermissions.isNotEmpty()) {
                                item {
                                    PermissionSectionHeader(
                                        title = stringResource(
                                            R.string.permission_group_optional_count,
                                            optionalPermissions.size
                                        ),
                                        importance = PermissionImportance.OPTIONAL
                                    )
                                }

                                items(optionalPermissions) { permission ->
                                PermissionItemCard(
                                    permission = permission,
                                        onSettingsClick = { handlePermissionClick(permission) }
                                    )
                                }
                            }
                        }
                    } ?: Box(
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



/**
 * 权限分组标题 - 简约设计版本
 */
@Composable
private fun PermissionSectionHeader(
    title: String,
    importance: PermissionImportance
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Surface(
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = importance.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontWeight = FontWeight.Medium
            )
        }
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
    resources: android.content.res.Resources,
    permission: PermissionInfo,
    permissionState: com.google.accompanist.permissions.PermissionState,
    androidPermission: String,
    permissionDescription: String,
    onFeedback: (String) -> Unit
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
            handleGrantedPermission(context, resources, permission, androidPermission, onFeedback)
        }
        isPermanentlyDenied -> {
            // 权限被永久拒绝的处理
            handlePermanentlyDeniedPermission(
                context,
                resources,
                permission,
                permissionState,
                androidPermission,
                permissionDescription,
                onFeedback
            )
        }
        permissionState.status.shouldShowRationale -> {
            // 需要显示权限说明，然后申请权限
            onFeedback(resources.getString(R.string.permission_rationale_message, permissionDescription))
            permissionState.launchPermissionRequest()
        }
        else -> {
            // 首次申请权限
            onFeedback(
                resources.getString(
                    R.string.permission_request_message,
                    permission.name,
                    permissionDescription
                )
            )
            permissionState.launchPermissionRequest()
        }
    }
}

/**
 * 处理已授权权限的逻辑
 */
private fun handleGrantedPermission(
    context: android.content.Context,
    resources: android.content.res.Resources,
    permission: PermissionInfo,
    androidPermission: String,
    onFeedback: (String) -> Unit
) {
    when (androidPermission) {
        android.Manifest.permission.CAMERA -> {
            // 相机权限已授权，提示用户可以重新申请以确保状态同步
            onFeedback(
                resources.getString(
                    R.string.permission_granted_manage_message,
                    permission.name
                )
            )
            // 对于相机权限，即使已授权也可以重新申请，不需要跳转设置
        }
        else -> {
            // 其他权限已授权，跳转到设置页面进行管理
            onFeedback(
                resources.getString(
                    R.string.permission_granted_redirect_message,
                    permission.name
                )
            )
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
    resources: android.content.res.Resources,
    permission: PermissionInfo,
    permissionState: com.google.accompanist.permissions.PermissionState,
    androidPermission: String,
    permissionDescription: String,
    onFeedback: (String) -> Unit
) {
    when (androidPermission) {
        android.Manifest.permission.CAMERA -> {
            // 相机权限被永久拒绝，但仍然可以尝试直接申请
            onFeedback(
                resources.getString(
                    R.string.permission_needs_reauth_message,
                    permission.name,
                    permissionDescription
                )
            )
            // 相机权限即使被永久拒绝也可以重新申请，不需要强制跳转设置
            permissionState.launchPermissionRequest()
        }
        else -> {
            // 其他权限被永久拒绝，跳转到系统设置页面
            onFeedback(
                resources.getString(
                    R.string.permission_needs_manual_enable_message,
                    permission.name
                )
            )
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
    resources: android.content.res.Resources,
    onFeedback: (String) -> Unit
) {
    when {
        // 处理运行时权限申请
        permission.canRequest && permission.id == "camera" -> {
            handleRuntimePermissionRequest(
                context = context,
                resources = resources,
                permission = permission,
                permissionState = cameraPermission,
                androidPermission = android.Manifest.permission.CAMERA,
                permissionDescription = resources.getString(R.string.camera_permission_description),
                onFeedback = onFeedback
            )
        }

        permission.canRequest && permission.id == "post_notifications" -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermission?.let { notifPerm ->
                    handleRuntimePermissionRequest(
                        context = context,
                        resources = resources,
                        permission = permission,
                        permissionState = notifPerm,
                        androidPermission = android.Manifest.permission.POST_NOTIFICATIONS,
                        permissionDescription = resources.getString(R.string.notification_permission_description),
                        onFeedback = onFeedback
                    )
                } ?: run {
                    // 降级到设置页面
                    onFeedback(resources.getString(R.string.notification_settings_device_redirect))
                    PermissionUtils.openNotificationPermissionSettings(context)
                }
            } else {
                // Android 13以下版本跳转到应用详情
                onFeedback(resources.getString(R.string.notification_settings_legacy_redirect))
                PermissionUtils.openAppDetailsSettings(context)
            }
        }

        // 处理系统设置跳转
        permission.settingsAction == "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS" -> {
            onFeedback(resources.getString(R.string.open_notification_listener_settings))
            PermissionUtils.openNotificationListenerSettings(context)
        }

        permission.settingsAction == "android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" -> {
            onFeedback(resources.getString(R.string.request_ignore_battery_optimization))
            PermissionUtils.requestIgnoreBatteryOptimization(context)
        }

        permission.settingsAction == "android.settings.APP_NOTIFICATION_SETTINGS" -> {
            onFeedback(resources.getString(R.string.open_notification_settings))
            PermissionUtils.openNotificationPermissionSettings(context)
        }

        permission.settingsAction == "android.settings.ACCESSIBILITY_SETTINGS" -> {
            onFeedback(resources.getString(R.string.open_accessibility_settings))
            PermissionUtils.openAccessibilitySettings(context)
        }

        else -> {
            // 默认跳转到应用详情页面
            onFeedback(resources.getString(R.string.open_app_details))
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
    val neutralContainer = MaterialTheme.colorScheme.surfaceContainerHigh
    val neutralContent = MaterialTheme.colorScheme.onSurfaceVariant
    val statusText = when {
        !checked -> stringResource(R.string.accessibility_status_checking)
        enabled -> stringResource(R.string.accessibility_status_enabled)
        else -> stringResource(R.string.accessibility_status_disabled)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.infoColors(),
        shape = MaterialTheme.shapes.medium,
        elevation = AppCardDefaults.sectionElevation()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
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
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.accessibility_service),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        StatusBadge(
                            text = statusText,
                            containerColor = when {
                                enabled -> neutralContainer
                                else -> neutralContainer
                            },
                            contentColor = when {
                                enabled -> SuccessGreen
                                !checked -> neutralContent
                                else -> neutralContent
                            }
                        )
                    }

                    Text(
                        text = stringResource(R.string.accessibility_compact_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (enabled && checked) 1 else 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onRefresh,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(stringResource(R.string.refresh_status))
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = onSettingsClick,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (enabled) {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = if (enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onPrimary
                                }
                            )
                        ) {
                            Text(
                                text = if (enabled) {
                                    stringResource(R.string.manage_permission)
                                } else {
                                    stringResource(R.string.setup)
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeepAliveControlsCard(
    model: KeepAliveControlUiModel,
    onMediaChanged: (Boolean) -> Unit,
    onOpenOverlayPermission: () -> Unit,
    onOverlayChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.infoColors(),
        shape = MaterialTheme.shapes.medium,
        elevation = AppCardDefaults.sectionElevation()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.keepalive_controls_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.keepalive_controls_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.keepalive_refresh_status))
                }
            }

            KeepAliveSwitchRow(
                title = stringResource(R.string.keepalive_media_switch),
                description = stringResource(R.string.keepalive_media_description),
                checked = model.media.isChecked,
                enabled = model.media.canToggle,
                statusText = model.media.statusText,
                onCheckedChange = onMediaChanged
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            KeepAliveOverlayPermissionRow(
                permissionText = model.overlay.permissionText.orEmpty(),
                onOpenOverlayPermission = onOpenOverlayPermission
            )

            KeepAliveSwitchRow(
                title = stringResource(R.string.keepalive_overlay_switch),
                description = stringResource(R.string.keepalive_overlay_description),
                checked = model.overlay.isChecked,
                enabled = model.overlay.canToggle,
                statusText = model.overlay.statusText,
                onCheckedChange = onOverlayChanged
            )
        }
    }
}

@Composable
private fun KeepAliveOverlayPermissionRow(
    permissionText: String,
    onOpenOverlayPermission: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.keepalive_overlay_permission),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.keepalive_permission_prefix, permissionText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onOpenOverlayPermission,
            modifier = Modifier.heightIn(min = 36.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.keepalive_open_overlay_permission),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun KeepAliveSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    statusText: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Text(
                text = stringResource(R.string.keepalive_status_prefix, statusText),
                style = MaterialTheme.typography.labelSmall,
                color = if (checked) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}
