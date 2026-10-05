package com.abrarshakhi.lumen.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenFileFolders: () -> Unit,
    onOpenLauncherMode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onBack = onBack,
        onOpenProviders = onOpenProviders,
        onOpenFileFolders = onOpenFileFolders,
        onOpenLauncherMode = onOpenLauncherMode,
        modifier = modifier,
    )
}
