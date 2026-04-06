package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.java.vmian.R
import com.java.vmian.domain.model.LogEntry
import com.java.vmian.domain.model.LogType
import com.java.vmian.domain.model.PushLogEntry
import com.java.vmian.domain.model.PushLogType
import com.java.vmian.presentation.ui.model.LogListBehavior
import com.java.vmian.presentation.ui.model.LogPanelLayout
import com.java.vmian.ui.theme.InfoBlue
import com.java.vmian.ui.theme.SuccessGreen

/**
 * 统一日志显示卡片组件 - 支持运行日志和推送日志切换
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnifiedLogDisplayCard(
    logs: List<LogEntry>,
    pushLogs: List<PushLogEntry>,
    onClearLogs: () -> Unit,
    onClearPushLogs: () -> Unit,
    panelHeight: Dp,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val hasSelectedTabContent = if (selectedTab == 0) logs.isNotEmpty() else pushLogs.isNotEmpty()

    LaunchedEffect(logs.size, pushLogs.size, selectedTab) {
        if (LogListBehavior.shouldScrollToLatestOnUpdate(hasSelectedTabContent)) {
            listState.animateScrollToItem(0)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(panelHeight),
        shape = RoundedCornerShape(16.dp),
        colors = AppCardDefaults.colors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val bodyHeight = LogPanelLayout.resolveBodyHeightDp(panelHeight.value.toInt()).dp

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.logs_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (selectedTab == 0) {
                                stringResource(R.string.log_tab_runtime, logs.size)
                            } else {
                                stringResource(R.string.log_tab_push, pushLogs.size)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                            contentDescription = stringResource(R.string.clear_logs),
                            tint = if ((selectedTab == 0 && logs.isNotEmpty()) || (selectedTab == 1 && pushLogs.isNotEmpty())) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedTab = 0 },
                        label = { Text(stringResource(R.string.log_tab_runtime, logs.size), fontSize = 12.sp) },
                        selected = selectedTab == 0
                    )
                    FilterChip(
                        onClick = { selectedTab = 1 },
                        label = { Text(stringResource(R.string.log_tab_push, pushLogs.size), fontSize = 12.sp) },
                        selected = selectedTab == 1
                    )
                }
            }

            HorizontalDivider()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bodyHeight)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (logs.isEmpty()) {
                            EmptyLogState(stringResource(R.string.runtime_log_empty))
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 6.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(logs, key = { it.id }) { log ->
                                    RunningLogItem(log = log)
                                }
                            }
                        }
                    }

                    1 -> {
                        if (pushLogs.isEmpty()) {
                            EmptyLogState(stringResource(R.string.push_log_empty))
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 6.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                items(pushLogs, key = { it.id }) { log ->
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

@Composable
private fun RunningLogItem(
    log: LogEntry,
    modifier: Modifier = Modifier
) {
    Text(
        text = LogListBehavior.buildRunningLogLine(log),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = getLogTypeColor(log.type),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        lineHeight = 18.sp
    )
}

@Composable
private fun PushLogItem(
    log: PushLogEntry,
    modifier: Modifier = Modifier
) {
    Text(
        text = LogListBehavior.buildPushLogLine(log),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = getPushLogTypeColor(log.type),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        lineHeight = 18.sp
    )
}

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

@Composable
private fun getPushLogTypeColor(type: PushLogType): Color {
    return when (type) {
        PushLogType.SUCCESS -> SuccessGreen
        PushLogType.FAILED -> MaterialTheme.colorScheme.error
    }
}
