package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.model.AppUpdateState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateUiModelTest {

    @Test
    fun from_returnsDownloadingModel_withRoundedEtaAndDialogActions() {
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
                etaSeconds = 8L,
                filePath = "/tmp/app.apk"
            )
        )

        assertEquals("正在下载更新", model.title)
        assertEquals("关闭后会继续在通知栏下载", model.body)
        assertEquals("40%", model.progressLabel)
        assertTrue(model.speedLabel.orEmpty().contains("MB/s"))
        assertEquals("约 10 秒", model.etaLabel)
        assertEquals("后台继续", model.primaryActionLabel)
        assertEquals("取消下载", model.secondaryActionLabel)
    }

    @Test
    fun from_returnsFailedModel_withRetryAndCloseActions() {
        val model = AppUpdateUiModel.from(
            AppUpdateState.Failed(message = "下载失败")
        )

        assertEquals("更新失败", model.title)
        assertEquals("重新下载", model.primaryActionLabel)
        assertEquals("关闭", model.secondaryActionLabel)
    }
}
