package com.java.vmian.data.remote.dto

data class UpdateManifestDto(
    val versionCode: Int,
    val versionName: String,
    val tagName: String,
    val apkUrl: String,
    val notes: String,
    val publishedAt: String,
    val sha256: String
)
