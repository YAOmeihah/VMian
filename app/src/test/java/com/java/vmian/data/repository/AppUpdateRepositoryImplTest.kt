package com.java.vmian.data.repository

import com.java.vmian.data.remote.GitHubReleaseApiService
import com.java.vmian.data.remote.dto.UpdateManifestDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

class AppUpdateRepositoryImplTest {

    @Test
    fun fetchLatestUpdateInfo_readsManifestFromConfiguredUrl() = runBlocking {
        val manifest = UpdateManifestDto(
            versionCode = 6,
            versionName = "1.1.4",
            tagName = "v1.1.4",
            apkUrl = "https://github.com/YAOmeihah/VMian/releases/download/v1.1.4/vmian-v1.1.4.apk",
            notes = "Bug fixes",
            publishedAt = "2026-05-03T08:00:00Z",
            sha256 = "abc123"
        )
        val apiService = FakeGitHubReleaseApiService(manifest = manifest)
        val repository = AppUpdateRepositoryImpl(
            apiService = apiService,
            manifestUrl = "https://example.com/update.json"
        )

        val result = repository.fetchLatestUpdateInfo()

        assertEquals(manifest.versionCode, result?.versionCode)
        assertEquals(manifest.versionName, result?.versionName)
        assertEquals(manifest.tagName, result?.tagName)
        assertEquals(manifest.apkUrl, result?.apkUrl)
        assertEquals(manifest.notes, result?.notes)
        assertEquals(manifest.publishedAt, result?.publishedAt)
        assertEquals(manifest.sha256, result?.sha256)
        assertEquals("https://example.com/update.json", apiService.lastManifestUrl)
    }

    @Test
    fun fetchLatestUpdateInfo_returnsNullWhenManifestUrlIsBlank() = runBlocking {
        val apiService = FakeGitHubReleaseApiService(
            manifest = UpdateManifestDto(
                versionCode = 1,
                versionName = "1.0.0",
                tagName = "v1.0.0",
                apkUrl = "https://example.com/app.apk",
                notes = "Initial",
                publishedAt = "2026-05-03T08:00:00Z",
                sha256 = "hash"
            )
        )
        val repository = AppUpdateRepositoryImpl(
            apiService = apiService,
            manifestUrl = "   "
        )

        val result = repository.fetchLatestUpdateInfo()

        assertNull(result)
        assertNull(apiService.lastManifestUrl)
    }

    @Test
    fun fetchLatestUpdateInfo_returnsNullWhenManifestIsMissing() = runBlocking {
        val apiService = FakeGitHubReleaseApiService(
            failure = HttpException(
                Response.error<String>(
                    404,
                    "Not Found".toResponseBody("text/plain".toMediaType())
                )
            )
        )
        val repository = AppUpdateRepositoryImpl(
            apiService = apiService,
            manifestUrl = "https://example.com/update.json"
        )

        val result = repository.fetchLatestUpdateInfo()

        assertNull(result)
        assertEquals("https://example.com/update.json", apiService.lastManifestUrl)
    }

    private class FakeGitHubReleaseApiService(
        private val manifest: UpdateManifestDto? = null,
        private val failure: Throwable? = null
    ) : GitHubReleaseApiService {
        var lastManifestUrl: String? = null

        override suspend fun getUpdateManifest(manifestUrl: String): UpdateManifestDto {
            lastManifestUrl = manifestUrl
            failure?.let { throw it }
            return requireNotNull(manifest)
        }
    }
}
