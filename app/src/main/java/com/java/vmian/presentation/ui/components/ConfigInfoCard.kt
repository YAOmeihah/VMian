package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.java.vmian.domain.model.PaymentConfig

/**
 * 配置信息卡片
 */
@Composable
fun ConfigInfoCard(config: PaymentConfig?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "配置信息",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "通知地址: ${config?.host ?: "请扫码配置"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "通讯密钥: ${if (config?.key?.isNotEmpty() == true) "已配置" else "请扫码配置"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
