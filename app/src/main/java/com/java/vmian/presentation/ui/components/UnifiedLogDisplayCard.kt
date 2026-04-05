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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.LogType
import com.java.vmian.domain.model.PushLogEntry
import com.java.vmian.domain.model.PushLogType
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.InfoBlue
import com.java.vmian.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.*

/**
 * 统一日志显示卡片组件 - 支持运行日志和推送日志切换
 */
@Composable
fun UnifiedLogDisplayCard(
    logs: List<LogEntry>,
    pushLogs: List<PushLogEntry>,
    onClearLogs: () -> Unit,
    onClearPushLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: 运行日志, 1: 推送日志
    val listState = rememberLazyListState()
    
    // 自动滚动到最新日志
    LaunchedEffect(logs.size, pushLogs.size, selectedTab) {
        if ((selectedTab == 0 && logs.isNotEmpty()) || (selectedTab == 1 && pushLogs.isNotEmpty())) {
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
                .fillMaxHeight()
        ) {
            // 标题栏和Tab切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab切换按钮
                Row {
                    FilterChip(
                        onClick = { selectedTab = 0 },
                        label = { Text("运行日志 (${logs.size}/500)", fontSize = 12.sp) },
                        selected = selectedTab == 0,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        onClick = { selectedTab = 1 },
                        label = { Text("推送日志 (${pushLogs.size}/500)", fontSize = 12.sp) },
                        selected = selectedTab == 1
                    )
                }
                
                // 清空按钮
                IconButton(
                    onClick = {
                        if (selectedTab == 0) {
                            onClearLogs()
                        } else {
                            onClearPushLogs()
                        }
                    },
                    enabled = if (selectedTab == 0) logs.isNotEmpty() else pushLogs.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "清空日志",
                        tint = if ((selectedTab == 0 && logs.isNotEmpty()) || (selectedTab == 1 && pushLogs.isNotEmpty())) {
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
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                when (selectedTab) {
                    0 -> {
                        // 运行日志
                        if (logs.isEmpty()) {
                            EmptyLogState("暂无运行日志")
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(logs) { log ->
                                    RunningLogItem(log = log)
                                }
                            }
                        }
                    }
                    1 -> {
                        // 推送日志
                        if (pushLogs.isEmpty()) {
                            EmptyLogState("暂无推送日志")
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(pushLogs) { log ->
                                    PushLogItem(log = log)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 空状态显示
 */
@Composable
private fun EmptyLogState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 运行日志条目组件
 */
@Composable
private fun RunningLogItem(
    log: LogEntry,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildString {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
            append("[$time] [${log.type.prefix}] ${log.message}")
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
 * 推送日志条目组件
 */
@Composable
private fun PushLogItem(
    log: PushLogEntry,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildString {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
            append("[$time] [${log.type.prefix}] ${log.paymentType} ¥${log.amount}")
            if (log.message.isNotEmpty()) {
                append(" - ${log.message}")
            }
        },
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = getPushLogTypeColor(log.type),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 1.dp),
        lineHeight = 14.sp
    )
}

/**
 * 根据运行日志类型获取对应的颜色
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

/**
 * 根据推送日志类型获取对应的颜色
 */
@Composable
private fun getPushLogTypeColor(type: PushLogType): Color {
    return when (type) {
        PushLogType.SUCCESS -> SuccessGreen
        PushLogType.FAILED -> MaterialTheme.colorScheme.error
    }
}
