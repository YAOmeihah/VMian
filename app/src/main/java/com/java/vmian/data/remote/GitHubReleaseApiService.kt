package com.java.vmian.data.remote

import com.java.vmian.data.remote.dto.GitHubReleaseDto
import com.java.vmian.data.remote.dto.UpdateManifestDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface GitHubReleaseApiService {
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubReleaseDto

    @GET
    suspend fun getUpdateManifest(@Url manifestUrl: String): UpdateManifestDto
}
