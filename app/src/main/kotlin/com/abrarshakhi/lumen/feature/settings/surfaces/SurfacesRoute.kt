package com.abrarshakhi.lumen.feature.settings.surfaces

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.ui.mvi.CollectEffects
import org.koin.androidx.compose.koinViewModel

@Composable
fun SurfacesRoute(
    onBack: () -> Unit,
    onNavigate: (AppRouteKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SurfacesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.dispatch(SurfacesIntent.Refreshed)
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is SurfacesEffect.ShowMessage ->
                snackbarHostState.showSnackbar(resources.getString(effect.message))
        }
    }

    SurfacesScreen(
        state = state,
        onIntent = viewModel::dispatch,
        onBack = onBack,
        onOpenLauncherMode = { onNavigate(AppRouteKey.LauncherMode) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}
