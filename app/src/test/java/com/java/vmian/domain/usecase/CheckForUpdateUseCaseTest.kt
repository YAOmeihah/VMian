package com.java.vmian.domain.usecase

import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.model.UpdateCheckResult
import com.java.vmian.domain.repository.AppUpdatePreferencesRepository
import com.java.vmian.domain.repository.AppUpdateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckForUpdateUseCaseTest {

    @Test
    fun invoke_returnsAvailable_whenRemoteVersionIsHigherAndNotIgnored() = runBlocking {
        val remoteRepository = FakeAppUpdateRepository(
            AppUpdateInfo(
                versionCode = 2,
                versionName = "1.1.0",
                tagName = "v1.1.0",
                apkUrl = "https://github.com/YAOmeihah/VMian/releases/download/v1.1.0/vmian-v1.1.0.apk",
                notes = "Bug fixes",
                publishedAt = "2026-04-24T12:00:00Z",
                sha256 = "abc123"
            )
        )
        val preferencesRepository = FakeAppUpdatePreferencesRepository(
            lastCheckAt = 0L,
            ignoredVersionCode = null
        )

        val result = CheckForUpdateUseCase(
            updateRepository = remoteRepository,
            preferencesRepository = preferencesRepository,
            nowProvider = { 1_000L },
            localVersionCode = 1,
            cooldownMs = 43_200_000L
        ).invoke(manual = false)

        assertTrue(result is UpdateCheckResult.Available)
    }

    @Test
    fun invoke_returnsSkippedByCooldown_whenAutomaticCheckRunsTooSoon() = runBlocking {
        val result = CheckForUpdateUseCase(
            updateRepository = FakeAppUpdateRepository(null),
            preferencesRepository = FakeAppUpdatePreferencesRepository(lastCheckAt = 900L),
            nowProvider = { 1_000L },
            localVersionCode = 1,
            cooldownMs = 500L
        ).invoke(manual = false)

        assertEquals(UpdateCheckResult.SkippedByCooldown, result)
    }

    @Test
    fun invoke_returnsUpToDate_whenRemoteVersionIsIgnored() = runBlocking {
        val result = CheckForUpdateUseCase(
            updateRepository = FakeAppUpdateRepository(
                AppUpdateInfo(
                    versionCode = 2,
                    versionName = "1.1.0",
                    tagName = "v1.1.0",
                    apkUrl = "https://github.com/YAOmeihah/VMian/releases/download/v1.1.0/vmian-v1.1.0.apk",
                    notes = "Bug fixes",
                    publishedAt = "2026-04-24T12:00:00Z",
                    sha256 = "abc123"
                )
            ),
            preferencesRepository = FakeAppUpdatePreferencesRepository(ignoredVersionCode = 2),
            nowProvider = { 1_000L },
            localVersionCode = 1,
            cooldownMs = 43_200_000L
        ).invoke(manual = false)

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun invoke_returnsUpToDate_whenManualCheckFindsSameVersion() = runBlocking {
        val result = CheckForUpdateUseCase(
            updateRepository = FakeAppUpdateRepository(
                AppUpdateInfo(
                    versionCode = 1,
                    versionName = "1.0",
                    tagName = "v1.0",
                    apkUrl = "https://github.com/YAOmeihah/VMian/releases/download/v1.0/vmian-v1.0.apk",
                    notes = "Current release",
                    publishedAt = "2026-04-24T12:00:00Z",
                    sha256 = "abc123"
                )
            ),
            preferencesRepository = FakeAppUpdatePreferencesRepository(lastCheckAt = 999L),
            nowProvider = { 1_000L },
            localVersionCode = 1,
            cooldownMs = 43_200_000L
        ).invoke(manual = true)

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    private class FakeAppUpdateRepository(
        private val updateInfo: AppUpdateInfo?
    ) : AppUpdateRepository {
        override suspend fun fetchLatestUpdateInfo(): AppUpdateInfo? = updateInfo
    }

    private class FakeAppUpdatePreferencesRepository(
        private var lastCheckAt: Long = 0L,
        private var ignoredVersionCode: Int? = null
    ) : AppUpdatePreferencesRepository {
        override suspend fun getLastCheckAt(): Long = lastCheckAt

        override suspend fun setLastCheckAt(timestamp: Long) {
            lastCheckAt = timestamp
        }

        override suspend fun getIgnoredVersionCode(): Int? = ignoredVersionCode

        override suspend fun setIgnoredVersionCode(versionCode: Int?) {
            ignoredVersionCode = versionCode
        }

        override suspend fun getPendingApkPath(): String? = null

        override suspend fun setPendingApkPath(path: String?) = Unit

        override suspend fun getPendingVersionCode(): Int? = null

        override suspend fun setPendingVersionCode(versionCode: Int?) = Unit

        override suspend fun isAwaitingInstallPermission(): Boolean = false

        override suspend fun setAwaitingInstallPermission(awaiting: Boolean) = Unit
    }
}
