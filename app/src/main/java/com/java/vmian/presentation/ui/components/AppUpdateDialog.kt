package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.java.vmian.domain.model.AppUpdateState
import com.java.vmian.presentation.ui.model.AppUpdateUiModel

@Composable
fun AppUpdateDialog(
    state: AppUpdateState,
    onConfirm: () -> Unit,
    onIgnore: () -> Unit,
    onDismiss: () -> Unit
) {
    val model = AppUpdateUiModel.from(state)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(model.title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = model.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(model.primaryActionLabel ?: "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) {
                Text(model.secondaryActionLabel ?: "稍后")
            }
        }
    )
}
