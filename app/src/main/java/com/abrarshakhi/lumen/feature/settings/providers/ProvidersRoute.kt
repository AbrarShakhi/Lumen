package com.abrarshakhi.lumen.feature.settings.providers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.ui.mvi.CollectEffects
import com.abrarshakhi.lumen.core.ui.permission.rememberPermissionRequester
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ProvidersRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProvidersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val intentLauncher = koinInject<IntentLauncher>()

    val requestPermissions = rememberPermissionRequester {
        viewModel.dispatch(ProvidersIntent.Refreshed)
    }

    // Permissions can be changed in system settings while Lumen is backgrounded, and there
    // is no flow to observe, so state is re-read every time the screen comes back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.dispatch(ProvidersIntent.Refreshed)
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is ProvidersEffect.RequestPermissions -> requestPermissions(effect.permissions)

            ProvidersEffect.OpenAppSettings ->
                intentLauncher.launch(PlatformIntent.AppDetails(context.packageName))
        }
    }

    ProvidersScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onBack = onBack,
        modifier = modifier,
    )
}
