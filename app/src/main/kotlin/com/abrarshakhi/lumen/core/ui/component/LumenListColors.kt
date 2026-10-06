package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

object LumenListColors {

    @Composable
    fun raised(): ListItemColors =
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceBright)

    @Composable
    fun tonal(): ListItemColors =
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)

    @Composable
    fun elevated(): ListItemColors =
        ListItemDefaults.segmentedColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
}
