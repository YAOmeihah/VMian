package com.java.vmian.domain.model

sealed interface UpdateCheckResult {
    data object UpToDate : UpdateCheckResult
    data object SkippedByCooldown : UpdateCheckResult
    data class Available(val info: AppUpdateInfo) : UpdateCheckResult
    data class Failed(val message: String) : UpdateCheckResult
}
