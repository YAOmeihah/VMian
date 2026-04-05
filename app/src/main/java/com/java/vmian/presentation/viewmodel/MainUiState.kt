package com.java.vmian.presentation.viewmodel

import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.PaymentConfig
import com.java.vmian.domain.model.PushLogEntry

/**
 * 主界面UI状态
 */
data class MainUiState(
    val config: PaymentConfig? = null,
    val isConfigured: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null,
    val logs: List<LogEntry> = emptyList(),
    val pushLogs: List<PushLogEntry> = emptyList()
)
