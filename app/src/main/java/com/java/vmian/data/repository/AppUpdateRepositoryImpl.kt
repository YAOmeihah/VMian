package com.java.vmian.data.repository

import com.java.vmian.data.remote.GitHubReleaseApiService
import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.repository.AppUpdateRepository
import retrofit2.HttpException

class AppUpdateRepositoryImpl(
    private val apiService: GitHubReleaseApiService,
    private val manifestUrl: String
) : AppUpdateRepository {
    override suspend fun fetchLatestUpdateInfo(): AppUpdateInfo? {
        if (manifestUrl.isBlank()) return null

        val manifest = try {
            apiService.getUpdateManifest(manifestUrl)
        } catch (error: HttpException) {
            if (error.code() == 404) return null
            throw error
        }
        return AppUpdateInfo(
            versionCode = manifest.versionCode,
            versionName = manifest.versionName,
            tagName = manifest.tagName,
            apkUrl = manifest.apkUrl,
            notes = manifest.notes,
            publishedAt = manifest.publishedAt,
            sha256 = manifest.sha256
        )
    }
}
