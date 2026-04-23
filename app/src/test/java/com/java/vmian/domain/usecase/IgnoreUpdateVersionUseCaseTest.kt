package com.java.vmian.domain.usecase

import com.java.vmian.domain.repository.AppUpdatePreferencesRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class IgnoreUpdateVersionUseCaseTest {

    @Test
    fun invoke_persistsIgnoredVersionCode() = runBlocking {
        val repository = FakeAppUpdatePreferencesRepository()

        IgnoreUpdateVersionUseCase(repository).invoke(12)

        assertEquals(12, repository.ignoredVersionCode)
    }

    private class FakeAppUpdatePreferencesRepository : AppUpdatePreferencesRepository {
        var ignoredVersionCode: Int? = null

        override suspend fun getLastCheckAt(): Long = 0L

        override suspend fun setLastCheckAt(timestamp: Long) = Unit

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
