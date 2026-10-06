package com.abrarshakhi.lumen.feature.search

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.ui.mvi.CollectEffects
import com.abrarshakhi.lumen.core.ui.permission.rememberPermissionRequester
import com.abrarshakhi.lumen.core.ui.text.resolve
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun SearchRoute(
    onNavigate: (AppRouteKey) -> Unit,
    onCloseSurface: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    launchQuery: String? = null,
    launchSerial: Int = 0,
    autoFocus: Boolean = true,
    barPosition: SearchBarPosition = SearchBarPosition.Bottom,
    presentation: SearchPresentation = SearchPresentation.Fullscreen,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val intentLauncher = koinInject<IntentLauncher>()
    val resources = LocalResources.current

    val textFieldState = rememberTextFieldState(initialText = launchQuery.orEmpty())
    var appliedSerial by rememberSaveable { mutableIntStateOf(launchSerial) }

    val requestPermissions = rememberPermissionRequester()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { viewModel.dispatch(SearchIntent.QueryChanged(it)) }
    }

    LaunchedEffect(launchSerial) {
        if (launchSerial != appliedSerial) {
            appliedSerial = launchSerial
            launchQuery?.let(textFieldState::setTextAndPlaceCursorAtEnd)
        }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is SearchEffect.Launch -> intentLauncher.launch(effect.intent)
            is SearchEffect.Navigate -> onNavigate(effect.route)
            is SearchEffect.SetQueryText -> textFieldState.setTextAndPlaceCursorAtEnd(effect.text)
            is SearchEffect.RequestPermissions -> requestPermissions(effect.permissions)
            is SearchEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.resolve(resources))
            SearchEffect.CloseSurface -> onCloseSurface()
        }
    }

    SearchScreen(
        state = state,
        textFieldState = textFieldState,
        onIntent = viewModel::dispatch,
        modifier = modifier,
        presentation = presentation,
        barPosition = barPosition,
        autoFocus = autoFocus,
        onDismiss = onCloseSurface,
        onNavigate = onNavigate,
    )
}
