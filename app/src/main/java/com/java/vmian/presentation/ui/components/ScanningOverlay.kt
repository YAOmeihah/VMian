package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 扫描覆盖层组件
 * 显示扫描框和提示信息
 */
@Composable
fun ScanningOverlay(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    val density = LocalDensity.current
    
    Box(modifier = modifier.fillMaxSize()) {
        // 半透明背景和扫描框
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawScanningOverlay(this)
        }
        
        // 顶部标题和关闭按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "扫描二维码",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
        }
        
        // 底部提示信息
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "将二维码放入框内",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "支持格式: host/key",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 绘制扫描覆盖层
 */
private fun drawScanningOverlay(drawScope: DrawScope) {
    with(drawScope) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 扫描框尺寸
        val scanBoxSize = minOf(canvasWidth, canvasHeight) * 0.6f
        val scanBoxLeft = (canvasWidth - scanBoxSize) / 2
        val scanBoxTop = (canvasHeight - scanBoxSize) / 2

        // 绘制半透明背景
        drawRect(
            color = Color.Black.copy(alpha = 0.5f),
            size = Size(canvasWidth, canvasHeight)
        )

        // 清除扫描框区域
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // 绘制扫描框边框
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(scanBoxLeft, scanBoxTop),
            size = Size(scanBoxSize, scanBoxSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )

        // 绘制四个角的装饰线
        val cornerLength = 30.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerColor = Color.Green

        // 左上角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop + cornerLength),
            end = Offset(scanBoxLeft, scanBoxTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop),
            strokeWidth = cornerStroke
        )

        // 右上角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + cornerLength),
            strokeWidth = cornerStroke
        )

        // 左下角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize - cornerLength),
            end = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + cornerLength, scanBoxTop + scanBoxSize),
            strokeWidth = cornerStroke
        )

        // 右下角
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize - cornerLength, scanBoxTop + scanBoxSize),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            strokeWidth = cornerStroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize - cornerLength),
            end = Offset(scanBoxLeft + scanBoxSize, scanBoxTop + scanBoxSize),
            strokeWidth = cornerStroke
        )
    }
}
