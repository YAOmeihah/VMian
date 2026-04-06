package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.java.vmian.R
import com.java.vmian.presentation.ui.model.MainScreenStage
import com.java.vmian.presentation.ui.model.MainScreenUiModel

@Composable
fun MainStatusCard(
    model: MainScreenUiModel,
    isLoading: Boolean,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.heroColors(),
        elevation = AppCardDefaults.heroElevation()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.main_primary_section),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.current_status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(
                    text = when (model.stage) {
                        MainScreenStage.Setup -> stringResource(R.string.status_pending_setup)
                        MainScreenStage.PermissionsRequired -> stringResource(R.string.status_pending_permission)
                        MainScreenStage.Ready -> stringResource(R.string.status_ready)
                    }
                )
            }

            Text(
                text = model.headline,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = model.supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onPrimaryAction,
                enabled = model.stage != MainScreenStage.Ready || !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (model.stage == MainScreenStage.Ready && isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(model.primaryActionLabel)
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String) {
    val containerColor = when (text) {
        stringResource(R.string.status_pending_setup) -> MaterialTheme.colorScheme.primaryContainer
        stringResource(R.string.status_pending_permission) -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when (text) {
        stringResource(R.string.status_pending_setup) -> MaterialTheme.colorScheme.onPrimaryContainer
        stringResource(R.string.status_pending_permission) -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
