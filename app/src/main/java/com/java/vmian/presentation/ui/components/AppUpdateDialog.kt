package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
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
    onPrimaryAction: (() -> Unit)?,
    onSecondaryAction: (() -> Unit)?,
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
                if (state is AppUpdateState.Downloading) {
                    LinearProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = model.progressLabel.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = model.speedLabel.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = model.etaLabel.orEmpty(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (model.primaryActionLabel != null && onPrimaryAction != null) {
                TextButton(onClick = onPrimaryAction) {
                    Text(model.primaryActionLabel)
                }
            }
        },
        dismissButton = {
            if (model.secondaryActionLabel != null && onSecondaryAction != null) {
                TextButton(onClick = onSecondaryAction) {
                    Text(model.secondaryActionLabel)
                }
            }
        }
    )
}
