package com.java.vmian.presentation.ui.model

import com.java.vmian.domain.model.AppUpdateInfo
import com.java.vmian.domain.model.AppUpdateState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateDialogBehaviorTest {

    private val info = AppUpdateInfo(
        versionCode = 2,
        versionName = "1.1.0",
        tagName = "v1.1.0",
        apkUrl = "https://example.com/app.apk",
        notes = "Bug fixes",
        publishedAt = "2026-04-24T12:00:00Z",
        sha256 = "abc123"
    )

    @Test
    fun shouldAutoShowDialog_returnsTrue_whenEnteringDownloadingWithoutSuppression() {
        assertTrue(
            AppUpdateDialogBehavior.shouldAutoShowDialog(
                previousState = AppUpdateState.UpdateAvailable(info),
                currentState = downloadingState(progressPercent = 10),
                suppressDownloadingDialog = false
            )
        )
    }

    @Test
    fun shouldAutoShowDialog_returnsFalse_whenDownloadProgressUpdatesAfterBackgroundContinue() {
        assertFalse(
            AppUpdateDialogBehavior.shouldAutoShowDialog(
                previousState = downloadingState(progressPercent = 10),
                currentState = downloadingState(progressPercent = 25),
                suppressDownloadingDialog = true
            )
        )
    }

    @Test
    fun shouldAutoShowDialog_returnsTrue_whenDownloadCompletesAfterSuppressedProgressDialog() {
        assertTrue(
            AppUpdateDialogBehavior.shouldAutoShowDialog(
                previousState = downloadingState(progressPercent = 90),
                currentState = AppUpdateState.Downloaded(info, filePath = "/tmp/app.apk"),
                suppressDownloadingDialog = true
            )
        )
    }

    private fun downloadingState(progressPercent: Int): AppUpdateState.Downloading {
        return AppUpdateState.Downloading(
            info = info,
            downloadedBytes = progressPercent * 10_000L,
            totalBytes = 1_000_000L,
            progressPercent = progressPercent,
            bytesPerSecond = 256_000L,
            etaSeconds = 10L,
            filePath = "/tmp/app.apk"
        )
    }
}
