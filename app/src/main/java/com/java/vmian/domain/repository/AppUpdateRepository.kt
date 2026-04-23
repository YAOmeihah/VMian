package com.java.vmian.domain.repository

import com.java.vmian.domain.model.AppUpdateInfo

interface AppUpdateRepository {
    suspend fun fetchLatestUpdateInfo(): AppUpdateInfo?
}
