package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.model.AppUpdateState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateUiModelTest {

    @Test
    fun from_returnsDownloadingModel_withPercentSpeedAndEta() {
        val model = AppUpdateUiModel.from(
            AppUpdateState.Downloading(
                info = AppUpdateInfo(
                    versionCode = 2,
                    versionName = "1.1.0",
                    tagName = "v1.1.0",
                    apkUrl = "https://github.com/YAOmeihah/VMian/releases/download/v1.1.0/vmian-v1.1.0.apk",
                    notes = "Bug fixes",
                    publishedAt = "2026-04-24T12:00:00Z",
                    sha256 = "abc123"
                ),
                downloadedBytes = 2_000_000L,
                totalBytes = 5_000_000L,
                progressPercent = 40,
                bytesPerSecond = 1_000_000L,
                etaSeconds = 3L,
                filePath = "/tmp/app.apk"
            )
        )

        assertEquals("正在下载更新", model.title)
        assertEquals("40%", model.progressLabel)
        assertTrue(model.speedLabel.orEmpty().contains("MB/s"))
        assertTrue(model.etaLabel.orEmpty().contains("3"))
    }
}
