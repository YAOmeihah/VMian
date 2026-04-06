package com.java.vmian.presentation.ui.components

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

object AppCardDefaults {
    @Composable
    fun colors(): CardColors {
        return CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
