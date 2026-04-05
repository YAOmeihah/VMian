package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.java.vmian.domain.model.PermissionInfo

/**
 * 权限检查对话框
 * 在应用启动时检查必需权限并引导用户设置
 */
@Composable
fun PermissionCheckDialog(
    missingPermissions: List<PermissionInfo>,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
    onRemindLater: () -> Unit,
    onNeverRemind: () -> Unit,
    showNeverRemindOption: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题和图标
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "权限配置提醒",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // 说明文字
                Text(
                    text = "V免签需要以下权限才能正常监听收款通知，请完成权限配置以确保应用稳定运行：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 缺失权限列表
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(missingPermissions) { permission ->
                            MissingPermissionItem(permission = permission)
                        }
                    }
                }

                // 重要性说明
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "为什么需要这些权限？",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• 通知监听权限：监听支付宝和微信的收款通知\n" +
                                    "• 通知权限：显示前台服务保持应用运行\n" +
                                    "• 电池优化白名单：防止系统杀死后台服务",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 操作按钮
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 主要操作按钮
                    Button(
                        onClick = onGoToSettings,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "立即设置权限",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 次要操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRemindLater,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "稍后提醒",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (showNeverRemindOption) {
                            TextButton(
                                onClick = onNeverRemind,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "不再提醒",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 底部提示
                Text(
                    text = "提示：这些权限是应用正常运行的必要条件，建议立即完成配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 缺失权限项目组件
 */
@Composable
private fun MissingPermissionItem(
    permission: PermissionInfo
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 状态指示器
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(8.dp)
        ) {}

        // 权限信息
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = permission.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = permission.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 简化版权限检查对话框
 * 用于只有少量权限缺失的情况
 */
@Composable
fun SimplePermissionCheckDialog(
    missingPermissionCount: Int,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
    onRemindLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "权限配置提醒",
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = "检测到 $missingPermissionCount 个必需权限未配置，这可能影响应用的正常运行。\n\n建议立即前往权限设置页面完成配置。"
            )
        },
        confirmButton = {
            Button(
                onClick = onGoToSettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("立即设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onRemindLater) {
                Text("稍后提醒")
            }
        }
    )
}
