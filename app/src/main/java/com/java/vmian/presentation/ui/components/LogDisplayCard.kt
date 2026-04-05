package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.LogType
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.InfoBlue
import com.java.vmian.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志显示卡片组件
 */
@Composable
fun LogDisplayCard(
    logs: List<LogEntry>,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    // 自动滚动到最新日志
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight() // 让Card填充可用高度
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "运行日志 (${logs.size}/500)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                IconButton(
                    onClick = onClearLogs,
                    enabled = logs.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空日志",
                        tint = if (logs.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            HorizontalDivider()
            
            // 日志列表
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 占据Column中的剩余空间
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (logs.isEmpty()) {
                    // 空状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无日志",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(logs) { log ->
                            LogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个日志条目组件 - 紧凑纯文字版本
 */
@Composable
private fun LogItem(
    log: LogEntry,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildString {
            // 时间戳（简化格式）
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
            append("[$time]")

            // 日志类型（使用颜色标记）
            append(" [${log.type.prefix}] ")

            // 日志内容
            append(log.message)
        },
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = getLogTypeColor(log.type),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp),
        lineHeight = 14.sp
    )
}

/**
 * 根据日志类型获取对应的颜色
 */
@Composable
private fun getLogTypeColor(type: LogType): Color {
    return when (type) {
        LogType.HEARTBEAT -> SuccessGreen
        LogType.PAYMENT_ALIPAY -> InfoBlue
        LogType.PAYMENT_WECHAT -> SuccessGreen
        LogType.NETWORK -> MaterialTheme.colorScheme.secondary
        LogType.CONFIG -> MaterialTheme.colorScheme.tertiary
        LogType.SYSTEM -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        LogType.ERROR -> MaterialTheme.colorScheme.error
    }
}
