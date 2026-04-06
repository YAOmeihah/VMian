package com.java.vmian.presentation.ui.components

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object AppCardDefaults {
    @Composable
    fun colors(): CardColors {
        return infoColors()
    }

    @Composable
    fun heroColors(): CardColors {
        return CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    @Composable
    fun infoColors(): CardColors {
        return CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    @Composable
    fun workspaceColors(): CardColors {
        return CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    @Composable
    fun heroElevation() = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
        pressedElevation = 1.dp
    )

    @Composable
    fun sectionElevation() = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp
    )
}
