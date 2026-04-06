package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.java.vmian.R

@Composable
fun QuickActionsCard(
    onTestListener: () -> Unit,
    onOpenPermissions: () -> Unit,
    onEditConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = AppCardDefaults.infoColors(),
        elevation = AppCardDefaults.sectionElevation()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.quick_actions_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditConfig,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_config),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onTestListener,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                ) {
                    Text(
                        text = stringResource(R.string.test_listener),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
            ) {
                Text(
                    text = stringResource(R.string.permission_settings),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
