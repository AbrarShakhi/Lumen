package com.abrarshakhi.lumen.feature.settings.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AiSettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val launcher = koinInject<IntentLauncher>()
    val scope = rememberCoroutineScope()

    AiSettingsScreen(
        state = state,
        onSave = viewModel::save,
        onRemove = viewModel::remove,
        onOpenKeyUrl = {
            scope.launch { launcher.launch(PlatformIntent.ViewUri(viewModel.keyUrl)) }
        },
        onBack = onBack,
        modifier = modifier,
    )
}
