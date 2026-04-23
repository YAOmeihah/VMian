package com.java.vmian.domain.model

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val tagName: String,
    val apkUrl: String,
    val notes: String,
    val publishedAt: String,
    val sha256: String
)
