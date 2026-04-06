package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.java.vmian.R
import com.java.vmian.domain.model.PaymentConfig

/**
 * 配置信息卡片
 */
@Composable
fun ConfigInfoCard(config: PaymentConfig?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = AppCardDefaults.colors()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.config_info),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            if (config?.isConfigured == true) {
                                stringResource(R.string.configured)
                            } else {
                                stringResource(R.string.not_configured)
                            }
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.monitor_host),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = config?.host ?: stringResource(R.string.config_host_empty),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.monitor_key),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (config?.monitorKey?.isNotEmpty() == true) {
                    stringResource(R.string.configured)
                } else {
                    stringResource(R.string.config_key_empty)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
