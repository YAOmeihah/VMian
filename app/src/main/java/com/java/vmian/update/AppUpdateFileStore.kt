package com.java.vmian.update

import android.content.Context
import com.java.vmian.domain.model.AppUpdateInfo
import java.io.File

object AppUpdateFileStore {
    private const val UPDATE_DIR = "updates"

    fun createTempApkFile(context: Context, info: AppUpdateInfo): File {
        val dir = File(requireNotNull(context.getExternalFilesDir(null)), UPDATE_DIR).apply { mkdirs() }
        return File(dir, "vmian-${info.versionCode}.part")
    }

    fun promoteTempFile(tempFile: File, info: AppUpdateInfo): File {
        val completedFile = File(tempFile.parentFile, "vmian-${info.versionCode}.apk")
        if (completedFile.exists()) {
            completedFile.delete()
        }
        check(tempFile.renameTo(completedFile)) { "无法完成更新文件重命名" }
        return completedFile
    }

    fun deleteQuietly(file: File) {
        if (file.exists()) {
            file.delete()
        }
    }
}
