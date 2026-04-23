package com.java.vmian.domain.usecase

import com.java.vmian.BuildConfig
import com.java.vmian.domain.model.UpdateCheckResult
import com.java.vmian.domain.repository.AppUpdatePreferencesRepository
import com.java.vmian.domain.repository.AppUpdateRepository

class CheckForUpdateUseCase(
    private val updateRepository: AppUpdateRepository,
    private val preferencesRepository: AppUpdatePreferencesRepository,
    private val nowProvider: () -> Long = System::currentTimeMillis,
    private val localVersionCode: Int = BuildConfig.VERSION_CODE,
    private val cooldownMs: Long = BuildConfig.UPDATE_CHECK_COOLDOWN_MS
) {
    suspend operator fun invoke(manual: Boolean): UpdateCheckResult {
        val now = nowProvider()
        val lastCheckAt = preferencesRepository.getLastCheckAt()
        if (!manual && lastCheckAt > 0 && now - lastCheckAt < cooldownMs) {
            return UpdateCheckResult.SkippedByCooldown
        }

        preferencesRepository.setLastCheckAt(now)

        return runCatching { updateRepository.fetchLatestUpdateInfo() }
            .fold(
                onSuccess = { info ->
                    when {
                        info == null -> UpdateCheckResult.UpToDate
                        info.versionCode <= localVersionCode -> UpdateCheckResult.UpToDate
                        preferencesRepository.getIgnoredVersionCode() == info.versionCode -> UpdateCheckResult.UpToDate
                        else -> UpdateCheckResult.Available(info)
                    }
                },
                onFailure = { UpdateCheckResult.Failed(it.message ?: "检查更新失败") }
            )
    }
}
