package com.java.vmian.data.remote

import com.java.vmian.data.remote.dto.UpdateManifestDto
import retrofit2.http.GET
import retrofit2.http.Url

interface GitHubReleaseApiService {
    @GET
    suspend fun getUpdateManifest(@Url manifestUrl: String): UpdateManifestDto
}
