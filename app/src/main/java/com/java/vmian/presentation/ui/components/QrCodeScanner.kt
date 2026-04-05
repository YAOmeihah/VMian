package com.java.vmian.presentation.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.java.vmian.util.QrCodeAnalyzer

/**
 * 二维码扫描器组件
 * 集成CameraX和ML Kit实现二维码扫描
 */
@Composable
fun QrCodeScanner(
    onQrCodeScanned: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanned by remember { mutableStateOf(false) }
    
    // 创建QrCodeAnalyzer实例
    val qrCodeAnalyzer = remember {
        QrCodeAnalyzer { qrContent ->
            if (!isScanned) {
                isScanned = true
                // 震动反馈
                vibrate(context)
                // 回调扫描结果
                onQrCodeScanned(qrContent)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 相机预览
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // 预览用例
                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    // 图像分析用例
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(context),
                                qrCodeAnalyzer
                            )
                        }

                    // 相机选择器（后置摄像头）
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        // 解绑所有用例
                        cameraProvider.unbindAll()

                        // 绑定用例到生命周期
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        android.util.Log.e("QrCodeScanner", "相机绑定失败", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
        
        // 扫描覆盖层
        ScanningOverlay(
            onCloseClick = onCloseClick
        )
    }
    
    // 重置扫描状态
    LaunchedEffect(isScanned) {
        if (isScanned) {
            kotlinx.coroutines.delay(1000) // 延迟1秒后允许再次扫描
            isScanned = false
            qrCodeAnalyzer.resetScanning()
        }
    }
}

/**
 * 震动反馈
 */
private fun vibrate(context: Context) {
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    } catch (e: Exception) {
        android.util.Log.e("QrCodeScanner", "震动失败", e)
    }
}
