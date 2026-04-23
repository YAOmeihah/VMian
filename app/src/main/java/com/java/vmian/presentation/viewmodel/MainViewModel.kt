package com.java.vmian.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.AppUpdateState
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.model.UpdateCheckResult
import com.java.vmian.domain.usecase.CheckForUpdateUseCase
import com.java.vmian.domain.usecase.ConfigUseCase
import com.java.vmian.domain.usecase.IgnoreUpdateVersionUseCase
import com.java.vmian.domain.usecase.PaymentUseCase
import com.java.vmian.service.AppUpdateDownloadService
import com.java.vmian.update.AppUpdateCoordinator
import com.java.vmian.util.HeartbeatScheduler
import com.java.vmian.util.HeartbeatTestHelper
import com.java.vmian.util.LogManager
import com.java.vmian.util.PushLogManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 主界面ViewModel
 */
class MainViewModel(
    private val configUseCase: ConfigUseCase,
    private val paymentUseCase: PaymentUseCase,
    private val logManager: LogManager,
    private val pushLogManager: PushLogManager,
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val ignoreUpdateVersionUseCase: IgnoreUpdateVersionUseCase,
    private val appUpdateCoordinator: AppUpdateCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeLogs()
        observePushLogs()
        observeUpdateState()
        logManager.logSystem("应用启动")
        // 初始化时加载配置
        loadConfig()
    }

    /**
     * 观察日志变化
     */
    private fun observeLogs() {
        viewModelScope.launch {
            logManager.logs.collect { logs ->
                _uiState.update { it.copy(logs = logs) }
            }
        }
    }

    /**
     * 观察推送日志变化
     */
    private fun observePushLogs() {
        viewModelScope.launch {
            pushLogManager.logs.collect { pushLogs ->
                _uiState.update { it.copy(pushLogs = pushLogs) }
            }
        }
    }

    private fun observeUpdateState() {
        viewModelScope.launch {
            appUpdateCoordinator.state.collect { updateState ->
                _uiState.update { it.copy(updateState = updateState) }
            }
        }
    }

    /**
     * 加载配置
     */
    fun loadConfig() {
        viewModelScope.launch {
            val config = configUseCase.getConfig()
            _uiState.update {
                it.copy(
                    config = config,
                    isConfigured = config?.isConfigured == true
                )
            }
            logManager.logConfig("配置加载完成")
        }
    }

    /**
     * 保存配置
     */
    fun saveConfig(host: String, terminalCode: String, monitorKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = configUseCase.saveConfig(host, terminalCode, monitorKey)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        config = PaymentConfig(
                            host = host.trim(),
                            terminalCode = terminalCode.trim(),
                            monitorKey = monitorKey.trim(),
                            isConfigured = true
                        ),
                        isConfigured = true,
                        message = "配置保存成功"
                    )
                }
                logManager.logConfig("配置保存成功: $host")
            } else {
                val errorMsg = "配置保存失败: ${result.exceptionOrNull()?.message}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = errorMsg
                    )
                }
                logManager.logError(errorMsg)
            }
        }
    }

    /**
     * 解析二维码
     */
    fun parseQrCode(qrContent: String) {
        val result = configUseCase.parseConfigFromQrCode(qrContent)
        if (result.isSuccess) {
            val payload = result.getOrThrow()
            saveConfig(payload.host, payload.terminalCode, payload.monitorKey)
        } else {
            _uiState.update {
                it.copy(message = "二维码格式错误，请扫描正确的配置二维码")
            }
        }
    }

    /**
     * 测试心跳
     */
    fun testHeartbeat() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logManager.logHeartbeat("开始心跳检测...")

            val result = paymentUseCase.sendHeartbeat()
            when (result) {
                is ApiResponse.Success -> {
                    val successMsg = "心跳测试成功: ${result.data}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = successMsg
                        )
                    }
                    logManager.logHeartbeat("心跳检测成功: ${result.data}")
                }
                is ApiResponse.Error -> {
                    val errorMsg = "心跳测试失败: ${result.message}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = errorMsg
                        )
                    }
                    logManager.logError("心跳检测失败: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * 清除消息
     */
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun checkForUpdates(manual: Boolean) {
        viewModelScope.launch {
            if (manual) {
                _uiState.update { it.copy(isLoading = true) }
            }
            appUpdateCoordinator.markChecking()
            when (val result = checkForUpdateUseCase(manual)) {
                is UpdateCheckResult.Available -> {
                    appUpdateCoordinator.setAvailable(result.info)
                    _uiState.update { it.copy(isLoading = false) }
                }
                UpdateCheckResult.UpToDate -> {
                    appUpdateCoordinator.reset()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = if (manual) "当前已是最新版本" else it.message
                        )
                    }
                }
                is UpdateCheckResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = if (manual) result.message else it.message
                        )
                    }
                    appUpdateCoordinator.onDownloadFailed(result.message)
                }
                UpdateCheckResult.SkippedByCooldown -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun ignoreCurrentUpdate() {
        viewModelScope.launch {
            val updateState = uiState.value.updateState
            if (updateState is AppUpdateState.UpdateAvailable) {
                ignoreUpdateVersionUseCase(updateState.info.versionCode)
            }
            appUpdateCoordinator.reset()
            _uiState.update { it.copy(message = "已忽略当前版本") }
        }
    }

    fun dismissUpdatePrompt() {
        appUpdateCoordinator.reset()
    }

    fun startUpdateDownload(context: Context) {
        ContextCompat.startForegroundService(
            context,
            AppUpdateDownloadService.createStartIntent(context)
        )
    }

    fun retryUpdateDownload(context: Context) {
        startUpdateDownload(context)
    }

    fun cancelUpdateDownload(context: Context) {
        context.startService(AppUpdateDownloadService.createCancelIntent(context))
        appUpdateCoordinator.reset()
        _uiState.update { it.copy(message = "已取消下载") }
    }

    fun installDownloadedUpdate(context: Context) {
        val state = uiState.value.updateState
        val filePath = when (state) {
            is AppUpdateState.Downloaded -> state.filePath
            is AppUpdateState.Installing -> state.filePath
            else -> null
        } ?: return
        context.startActivity(
            com.java.vmian.update.AppUpdateIntentFactory.createInstallerActivityIntent(context, filePath).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    /**
     * 清空日志
     */
    fun clearLogs() {
        logManager.clearLogs()
        logManager.logSystem("日志已清空")
    }

    /**
     * 测试新的心跳调度机制
     */
    fun testNewHeartbeatMechanism(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 生成状态报告
                val report = HeartbeatTestHelper.getHeartbeatStatusReport(context)

                // 触发立即心跳测试
                HeartbeatTestHelper.triggerImmediateHeartbeat(context)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "新心跳机制测试已启动\n\n$report"
                    )
                }

                logManager.logSystem("新心跳机制测试完成")

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "心跳机制测试失败: ${e.message}"
                    )
                }
                logManager.logError("心跳机制测试失败: ${e.message}")
            }
        }
    }

    /**
     * 获取 Doze 模式测试说明
     */
    fun getDozeModeTestInstructions(): String {
        return HeartbeatTestHelper.getDozeModeTestInstructions()
    }

    /**
     * 手动启动心跳调度
     */
    fun startHeartbeatScheduler(context: Context) {
        HeartbeatScheduler.startHeartbeat(context)
        _uiState.update { it.copy(message = "心跳调度已启动") }
        logManager.logSystem("手动启动心跳调度")
    }

    /**
     * 手动停止心跳调度
     */
    fun stopHeartbeatScheduler(context: Context) {
        HeartbeatScheduler.stopHeartbeat(context)
        _uiState.update { it.copy(message = "心跳调度已停止") }
        logManager.logSystem("手动停止心跳调度")
    }

    /**
     * 清空推送日志
     */
    fun clearPushLogs() {
        pushLogManager.clearLogs()
        logManager.logSystem("推送日志已清空")
    }
}
