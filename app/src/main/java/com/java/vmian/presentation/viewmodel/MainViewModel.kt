package com.java.vmian.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.java.vmian.domain.model.ApiResponse
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.usecase.ConfigUseCase
import com.java.vmian.domain.usecase.PaymentUseCase
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
    private val pushLogManager: PushLogManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeLogs()
        observePushLogs()
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
    fun saveConfig(host: String, monitorKey: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = configUseCase.saveConfig(host, monitorKey)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        config = PaymentConfig(host.trim(), monitorKey.trim(), true),
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
            val (host, monitorKey) = result.getOrThrow()
            saveConfig(host, monitorKey)
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
