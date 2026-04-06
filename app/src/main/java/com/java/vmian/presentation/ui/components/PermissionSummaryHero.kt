package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.java.vmian.domain.model.PermissionStatus
import com.java.vmian.presentation.ui.model.PermissionOverviewUiModel

@Composable
fun PermissionSummaryHero(
    status: PermissionStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PermissionOverviewCard(
            status = status,
            model = PermissionOverviewUiModel.from(status)
        )
    }
}
