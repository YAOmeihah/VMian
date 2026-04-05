package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 二维码扫描对话框
 * 全屏显示，提供更好的扫描体验
 */
@Composable
fun QrCodeScannerDialog(
    onQrCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // 全屏显示
        )
    ) {
        QrCodeScanner(
            onQrCodeScanned = onQrCodeScanned,
            onCloseClick = onDismiss,
            modifier = Modifier.fillMaxSize()
        )
    }
}
