package com.java.vmian.domain.usecase

import com.java.vmian.domain.repository.AppUpdatePreferencesRepository

class GetStoredUpdateStateUseCase(
    private val preferencesRepository: AppUpdatePreferencesRepository
) {
    suspend operator fun invoke(): StoredUpdateState {
        return StoredUpdateState(
            pendingApkPath = preferencesRepository.getPendingApkPath(),
            pendingVersionCode = preferencesRepository.getPendingVersionCode(),
            awaitingInstallPermission = preferencesRepository.isAwaitingInstallPermission()
        )
    }
}

data class StoredUpdateState(
    val pendingApkPath: String?,
    val pendingVersionCode: Int?,
    val awaitingInstallPermission: Boolean
)
