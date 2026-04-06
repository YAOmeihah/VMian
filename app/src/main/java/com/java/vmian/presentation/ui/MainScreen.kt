package com.java.vmian.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.java.vmian.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.java.vmian.VmqApplication
import com.java.vmian.presentation.ui.components.ConfigInfoCard
import com.java.vmian.presentation.ui.components.MainStatusCard
import com.java.vmian.presentation.ui.components.ManualConfigDialog
import com.java.vmian.presentation.ui.components.PermissionCheckDialog
import com.java.vmian.presentation.ui.components.QrCodeScannerDialog
import com.java.vmian.presentation.ui.components.SimplePermissionCheckDialog
import com.java.vmian.presentation.ui.components.UnifiedLogDisplayCard
import com.java.vmian.presentation.ui.model.LogPanelLayout
import com.java.vmian.presentation.ui.model.MainScreenStage
import com.java.vmian.presentation.ui.model.MainScreenUiModel
import com.java.vmian.presentation.viewmodel.MainViewModel
import com.java.vmian.presentation.viewmodel.MainViewModelFactory
import com.java.vmian.util.PermissionCheckManager
import com.java.vmian.util.PermissionCheckResult
import com.java.vmian.util.PermissionCheckUtils
import com.java.vmian.util.PermissionUtils

/**
 * 主界面
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
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

    val cameraPermission = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    var showQrScanner by remember { mutableStateOf(false) }
    var showManualConfig by remember { mutableStateOf(false) }
    var showConfigMethodSheet by remember { mutableStateOf(false) }

    var showPermissionCheckDialog by remember { mutableStateOf(false) }
    var showSimplePermissionDialog by remember { mutableStateOf(false) }
    var missingPermissions by remember { mutableStateOf<List<com.java.vmian.domain.model.PermissionInfo>>(emptyList()) }
    var missingPermissionCount by remember { mutableStateOf(0) }

    val permissionCheckManager = remember { PermissionCheckManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val unresolvedPermissionCount = maxOf(missingPermissions.size, missingPermissionCount)
    val screenModel = MainScreenUiModel.from(
        uiState = uiState,
        missingPermissionCount = unresolvedPermissionCount
    )
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    val logPanelHeight = LogPanelLayout.resolveCardHeightDp(screenHeightDp).dp

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val result = PermissionCheckUtils.checkPermissions(context)

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                when (result) {
                    is PermissionCheckResult.NoCheckNeeded -> Unit
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.main_title),
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
                                contentDescription = stringResource(R.string.permission_settings),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        MainStatusCard(model = screenModel)
                    }
                }

                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ConfigInfoCard(config = uiState.config)
                    }
                }

                item {
                    Button(
                        onClick = {
                            when (screenModel.stage) {
                                MainScreenStage.Setup -> showConfigMethodSheet = true
                                MainScreenStage.PermissionsRequired -> onNavigateToPermissions()
                                MainScreenStage.Ready -> viewModel.testHeartbeat()
                            }
                        },
                        enabled = screenModel.stage != MainScreenStage.Ready || !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (screenModel.stage == MainScreenStage.Ready && uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(screenModel.primaryActionLabel)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { PermissionUtils.sendTestNotification(context) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.test_listener))
                        }

                        OutlinedButton(
                            onClick = onNavigateToPermissions,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.permission_settings))
                        }
                    }
                }

                if (uiState.isConfigured) {
                    item {
                        TextButton(
                            onClick = { showConfigMethodSheet = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(stringResource(R.string.edit_config))
                        }
                    }
                }

                item {
                    UnifiedLogDisplayCard(
                        logs = uiState.logs,
                        pushLogs = uiState.pushLogs,
                        onClearLogs = { viewModel.clearLogs() },
                        onClearPushLogs = { viewModel.clearPushLogs() },
                        panelHeight = logPanelHeight,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding()
                    )
                }

            }
        }
    }

    if (showQrScanner) {
        QrCodeScannerDialog(
            onQrCodeScanned = { qrContent ->
                viewModel.parseQrCode(qrContent)
                showQrScanner = false
            },
            onDismiss = { showQrScanner = false }
        )
    }

    if (showManualConfig) {
        ManualConfigDialog(
            onConfigSaved = { host, monitorKey ->
                viewModel.saveConfig(host, monitorKey)
                showManualConfig = false
            },
            onDismiss = { showManualConfig = false }
        )
    }

    if (showConfigMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfigMethodSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.config_method_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(R.string.config_method_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        showConfigMethodSheet = false
                        if (cameraPermission.status.isGranted) {
                            showQrScanner = true
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.scan_config))
                }
                OutlinedButton(
                    onClick = {
                        showConfigMethodSheet = false
                        showManualConfig = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.manual_config))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

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

    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }
}
