package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.java.vmian.domain.model.AppUpdateState
import com.java.vmian.presentation.ui.model.AppUpdateUiModel

@Composable
fun AppUpdateStatusCard(
    state: AppUpdateState,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val model = AppUpdateUiModel.from(state)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.infoColors(),
        elevation = AppCardDefaults.sectionElevation()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = model.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = model.body,
                style = MaterialTheme.typography.bodyMedium
            )
            if (state is AppUpdateState.Downloading) {
                LinearProgressIndicator(
                    progress = { state.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(model.progressLabel.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    Text(model.speedLabel.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    Text(model.etaLabel.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (model.primaryActionLabel != null) {
                    Button(
                        onClick = onPrimaryAction,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                    ) {
                        Text(model.primaryActionLabel)
                    }
                }
                if (model.secondaryActionLabel != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                    ) {
                        Text(model.secondaryActionLabel)
                    }
                }
            }
        }
    }
}
