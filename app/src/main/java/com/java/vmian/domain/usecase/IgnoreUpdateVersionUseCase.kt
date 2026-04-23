package com.java.vmian.domain.usecase

import com.java.vmian.domain.repository.AppUpdatePreferencesRepository

class IgnoreUpdateVersionUseCase(
    private val preferencesRepository: AppUpdatePreferencesRepository
) {
    suspend operator fun invoke(versionCode: Int?) {
        preferencesRepository.setIgnoredVersionCode(versionCode)
    }
}
