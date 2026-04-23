package com.java.vmian.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GitHubReleaseDto(
    @SerializedName("tag_name")
    val tagName: String,
    val assets: List<GitHubReleaseAssetDto> = emptyList()
)

data class GitHubReleaseAssetDto(
    val name: String,
    @SerializedName("browser_download_url")
    val browserDownloadUrl: String
)
