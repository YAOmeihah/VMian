package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.java.vmian.R
import com.java.vmian.domain.model.PermissionImportance
import com.java.vmian.domain.model.PermissionInfo
import com.java.vmian.presentation.ui.model.PermissionCardAction
import com.java.vmian.presentation.ui.model.PermissionCardStatus
import com.java.vmian.presentation.ui.model.PermissionCardUiModel
import com.java.vmian.ui.theme.SuccessGreen
import com.java.vmian.ui.theme.WarningOrange

/**
 * 权限项目卡片组件
 */
@Composable
fun PermissionItemCard(
    permission: PermissionInfo,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model = PermissionCardUiModel.from(permission)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.colors(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
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
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = permission.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            StatusChip(
                                status = model.status,
                                importance = permission.importance
                            )
                        }

                        if (model.showDescription) {
                            Text(
                                text = permission.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            val actionContainerColor = when (model.action) {
                                PermissionCardAction.Manage,
                                PermissionCardAction.ReRequest -> MaterialTheme.colorScheme.surfaceContainerHighest
                                else -> MaterialTheme.colorScheme.primary
                            }
                            val actionContentColor = when (model.action) {
                                PermissionCardAction.Manage,
                                PermissionCardAction.ReRequest -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onPrimary
                            }

                            Button(
                                onClick = onSettingsClick,
                                modifier = Modifier.heightIn(min = 36.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = actionContainerColor,
                                    contentColor = actionContentColor
                                )
                            ) {
                                Text(
                                    text = actionText(model.action),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
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
    status: PermissionCardStatus,
    importance: PermissionImportance
) {
    val neutralContainer = MaterialTheme.colorScheme.surfaceContainerHigh
    val neutralContent = MaterialTheme.colorScheme.onSurfaceVariant

    val (text, containerColor, contentColor) = when {
        status == PermissionCardStatus.Granted -> Triple(
            stringResource(R.string.permission_status_granted),
            neutralContainer,
            SuccessGreen
        )
        status == PermissionCardStatus.NeedsSetup -> Triple(
            stringResource(R.string.permission_status_needs_setup),
            neutralContainer,
            MaterialTheme.colorScheme.primary
        )
        importance == PermissionImportance.REQUIRED -> Triple(
            stringResource(R.string.permission_status_required),
            neutralContainer,
            neutralContent
        )
        importance == PermissionImportance.RECOMMENDED -> Triple(
            stringResource(R.string.permission_status_recommended),
            neutralContainer,
            neutralContent
        )
        else -> Triple(
            stringResource(R.string.permission_status_optional),
            neutralContainer,
            neutralContent
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

@Composable
private fun actionText(action: PermissionCardAction): String {
    return when (action) {
        PermissionCardAction.OpenGuide -> stringResource(R.string.permission_action_guide)
        PermissionCardAction.ReRequest -> stringResource(R.string.permission_action_rerequest)
        PermissionCardAction.Manage -> stringResource(R.string.permission_action_manage)
        PermissionCardAction.Request -> stringResource(R.string.permission_action_request)
        PermissionCardAction.OpenSettings -> stringResource(R.string.permission_action_open_settings)
    }
}
