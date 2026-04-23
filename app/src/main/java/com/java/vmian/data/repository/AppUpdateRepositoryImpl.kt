package com.java.vmian.data.repository

import com.java.vmian.data.remote.GitHubReleaseApiService
import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.repository.AppUpdateRepository

class AppUpdateRepositoryImpl(
    private val apiService: GitHubReleaseApiService,
    private val owner: String,
    private val repo: String
) : AppUpdateRepository {
    override suspend fun fetchLatestUpdateInfo(): AppUpdateInfo? {
        val release = apiService.getLatestRelease(owner, repo)
        val manifestAssetUrl = release.assets
            .firstOrNull { it.name == "update.json" }
            ?.browserDownloadUrl
            ?: return null
        val manifest = apiService.getUpdateManifest(manifestAssetUrl)
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
