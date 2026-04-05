package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.WarningOrange

/**
 * 权限项目卡片组件
 */
@Composable
fun PermissionItemCard(
    permission: PermissionInfo,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主要信息行：状态图标 + 权限名称 + 状态标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 状态图标
                    Icon(
                        imageVector = when {
                            permission.isGranted -> Icons.Default.CheckCircle
                            permission.importance == PermissionImportance.REQUIRED -> Icons.Default.Close
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            permission.isGranted -> SuccessGreen
                            permission.importance == PermissionImportance.REQUIRED -> MaterialTheme.colorScheme.error
                            else -> WarningOrange
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 权限名称
                        Text(
                            text = permission.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 权限描述
                        Text(
                            text = permission.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            // 操作按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 刷新按钮
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "刷新",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 设置按钮
                Button(
                    onClick = onSettingsClick,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            permission.id.contains("auto_start") -> WarningOrange
                            permission.isGranted -> MaterialTheme.colorScheme.secondary
                            permission.importance == PermissionImportance.REQUIRED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        text = when {
                            permission.id.contains("auto_start") -> "查看指导"
                            permission.isGranted && permission.canRequest -> "重新申请"
                            permission.isGranted -> "管理权限"
                            permission.canRequest -> "申请权限"
                            else -> "设置"
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }


        }
    }
}

/**
 * 状态标签组件 - 简约设计
 */
@Composable
private fun StatusChip(
    isGranted: Boolean,
    importance: PermissionImportance,
    isAutoStart: Boolean
) {
    val (text, containerColor, contentColor) = when {
        isGranted -> Triple(
            "已授权",
            SuccessGreen.copy(alpha = 0.1f),
            SuccessGreen
        )
        isAutoStart -> Triple(
            "需设置",
            WarningOrange.copy(alpha = 0.1f),
            WarningOrange
        )
        importance == PermissionImportance.REQUIRED -> Triple(
            "必需",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        importance == PermissionImportance.RECOMMENDED -> Triple(
            "推荐",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Triple(
            "可选",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium
        )
    }
}
