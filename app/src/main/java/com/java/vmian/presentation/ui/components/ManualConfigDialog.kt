package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.java.vmian.R

/**
 * 手动配置对话框
 */
@Composable
fun ManualConfigDialog(
    onConfigSaved: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var host by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    val trimmedHost = host.trim()
    val trimmedKey = key.trim()
    val hostError = remember(trimmedHost) {
        trimmedHost.isNotEmpty() &&
            !trimmedHost.startsWith("http://") &&
            !trimmedHost.startsWith("https://")
    }
    val keyError = remember(trimmedKey, key) {
        key.isNotEmpty() && trimmedKey.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = AppCardDefaults.heroColors(),
            elevation = AppCardDefaults.heroElevation()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.manual_config),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = stringResource(R.string.manual_config_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.server_address)) },
                    placeholder = { Text(stringResource(R.string.server_address_example)) },
                    supportingText = {
                        Text(
                            if (hostError) {
                                stringResource(R.string.server_address_error)
                            } else {
                                stringResource(R.string.manual_config_host_hint)
                            }
                        )
                    },
                    isError = hostError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text(stringResource(R.string.monitor_key_input)) },
                    placeholder = { Text(stringResource(R.string.monitor_key_placeholder)) },
                    supportingText = {
                        Text(
                            if (keyError) {
                                stringResource(R.string.monitor_key_error)
                            } else {
                                stringResource(R.string.manual_config_key_hint)
                            }
                        )
                    },
                    isError = keyError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            if (trimmedHost.isNotBlank() && trimmedKey.isNotBlank() && !hostError) {
                                onConfigSaved(trimmedHost, trimmedKey)
                            }
                        },
                        enabled = trimmedHost.isNotBlank() && trimmedKey.isNotBlank() && !hostError,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
