package com.java.vmian.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.java.vmian.VmqApplication
import com.java.vmian.presentation.ui.components.ConfigInfoCard
import com.java.vmian.presentation.ui.components.ImmersiveContent
import com.java.vmian.presentation.ui.components.UnifiedLogDisplayCard
import com.java.vmian.presentation.ui.components.ManualConfigDialog
import com.java.vmian.presentation.ui.components.PermissionCheckDialog
import com.java.vmian.presentation.ui.components.QrCodeScannerDialog
import com.java.vmian.presentation.ui.components.SimplePermissionCheckDialog
import com.java.vmian.presentation.viewmodel.MainViewModel
import com.java.vmian.presentation.viewmodel.MainViewModelFactory
import com.java.vmian.util.PermissionCheckManager
import com.java.vmian.util.PermissionCheckResult
import com.java.vmian.util.PermissionCheckUtils
import com.java.vmian.util.PermissionUtils

/**
 * 主界面
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onNavigateToPermissions: () -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as VmqApplication
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            application.container.configUseCase,
            application.container.paymentUseCase,
            application.container.logManager,
            application.container.pushLogManager
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    // 权限状态
    val cameraPermission = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    var showQrScanner by remember { mutableStateOf(false) }
    var showManualConfig by remember { mutableStateOf(false) }

    // 权限检查状态
    var showPermissionCheckDialog by remember { mutableStateOf(false) }
    var showSimplePermissionDialog by remember { mutableStateOf(false) }
    var missingPermissions by remember { mutableStateOf<List<com.java.vmian.domain.model.PermissionInfo>>(emptyList()) }
    var missingPermissionCount by remember { mutableStateOf(0) }

    val permissionCheckManager = remember { PermissionCheckManager(context) }

    // 应用启动时的权限检查 - 延迟执行以避免闪屏
    LaunchedEffect(Unit) {
        // 延迟200ms，确保UI先完成渲染
        kotlinx.coroutines.delay(200)

        // 在后台线程执行权限检查
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // 执行权限检查
            val result = PermissionCheckUtils.checkPermissions(context)

            // 切换回主线程更新UI
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                when (result) {
                    is PermissionCheckResult.NoCheckNeeded -> {
                        // 不需要显示权限检查对话框
                    }
                    is PermissionCheckResult.ShowDialog -> {
                        missingPermissions = result.missingPermissions
                        showPermissionCheckDialog = true
                    }
                    is PermissionCheckResult.ShowSimpleDialog -> {
                        missingPermissionCount = result.missingCount
                        showSimplePermissionDialog = true
                    }
                }
            }
        }
    }

    // 沉浸式背景容器
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // 使用Column直接处理系统栏，提供更好的控制
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
                )
        ) {
            // 状态栏占位
            Spacer(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Top)
                )
            )

            // 使用Column来控制间距和布局
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 减少间距从16dp到8dp
            ) {
            // 固定位置的标题栏设计
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "V免签监控端",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onNavigateToPermissions,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "权限设置",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 配置信息显示
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ConfigInfoCard(config = uiState.config)
            }

        // 操作按钮
        Button(
            onClick = {
                if (cameraPermission.status.isGranted) {
                    showQrScanner = true
                } else {
                    cameraPermission.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("扫码配置")
        }

        Button(
            onClick = { showManualConfig = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("手动配置")
        }

        Button(
            onClick = { viewModel.testHeartbeat() },
            enabled = uiState.isConfigured && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("检测心跳")
            }
        }

        Button(
            onClick = { PermissionUtils.sendTestNotification(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("检测监听")
        }

            // 统一日志显示区域
            UnifiedLogDisplayCard(
                logs = uiState.logs,
                pushLogs = uiState.pushLogs,
                onClearLogs = { viewModel.clearLogs() },
                onClearPushLogs = { viewModel.clearPushLogs() },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(
                        WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
                    )
                    .padding(bottom = 8.dp) // 额外的视觉间距
            )
            }
        }
    }

    // 二维码扫描器
    if (showQrScanner) {
        QrCodeScannerDialog(
            onQrCodeScanned = { qrContent ->
                viewModel.parseQrCode(qrContent)
                showQrScanner = false
            },
            onDismiss = { showQrScanner = false }
        )
    }

    // 手动配置对话框
    if (showManualConfig) {
        ManualConfigDialog(
            onConfigSaved = { host, key ->
                viewModel.saveConfig(host, key)
                showManualConfig = false
            },
            onDismiss = { showManualConfig = false }
        )
    }

    // 权限检查对话框
    if (showPermissionCheckDialog) {
        PermissionCheckDialog(
            missingPermissions = missingPermissions,
            onDismiss = { showPermissionCheckDialog = false },
            onGoToSettings = {
                permissionCheckManager.onGoToSettings()
                showPermissionCheckDialog = false
                onNavigateToPermissions()
            },
            onRemindLater = {
                permissionCheckManager.onRemindLater()
                showPermissionCheckDialog = false
            },
            onNeverRemind = {
                permissionCheckManager.onNeverRemind()
                showPermissionCheckDialog = false
            },
            showNeverRemindOption = permissionCheckManager.shouldShowNeverRemindOption()
        )
    }

    // 简化权限检查对话框
    if (showSimplePermissionDialog) {
        SimplePermissionCheckDialog(
            missingPermissionCount = missingPermissionCount,
            onDismiss = { showSimplePermissionDialog = false },
            onGoToSettings = {
                permissionCheckManager.onGoToSettings()
                showSimplePermissionDialog = false
                onNavigateToPermissions()
            },
            onRemindLater = {
                permissionCheckManager.onRemindLater()
                showSimplePermissionDialog = false
            }
        )
    }

    // 消息提示
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // 显示Toast
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }
}


