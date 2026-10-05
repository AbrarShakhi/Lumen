package com.abrarshakhi.lumen.feature.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.snapshotFlow
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.ui.mvi.CollectEffects
import com.abrarshakhi.lumen.core.ui.permission.rememberPermissionRequester
import com.abrarshakhi.lumen.core.ui.text.resolve
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Wires the stateless [SearchScreen] to its ViewModel, DI and navigation.
 *
 * Keeping this split means the screen itself stays testable and previewable with a
 * fabricated state, while everything environment-dependent lives here.
 */
@Composable
fun SearchRoute(
    onNavigate: (AppRouteKey) -> Unit,
    onCloseSurface: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
    autoFocus: Boolean = true,
    barPosition: SearchBarPosition = SearchBarPosition.Bottom,
    presentation: SearchPresentation = SearchPresentation.Fullscreen,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val intentLauncher = koinInject<IntentLauncher>()

    // The text buffer lives here, in the UI, not in SearchState.
    val textFieldState = remember { TextFieldState(initialText = initialQuery.orEmpty()) }

    // The gate re-evaluates from the checker on the next keystroke, so nothing to do with
    // the result here beyond the recording the requester performs.
    val requestPermissions = rememberPermissionRequester()

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { viewModel.dispatch(SearchIntent.QueryChanged(it)) }
    }

    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is SearchEffect.Launch -> intentLauncher.launch(effect.intent)
            is SearchEffect.Navigate -> onNavigate(effect.route)
            is SearchEffect.SetQueryText -> textFieldState.setTextAndPlaceCursorAtEnd(effect.text)
            is SearchEffect.RequestPermissions -> requestPermissions(effect.permissions)
            is SearchEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text.asString())
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
        // Tapping away from a summoned panel dismisses it, exactly as back does.
        onDismiss = onCloseSurface,
        onOpenSettings = { onNavigate(AppRouteKey.Settings) },
    )
}

/**
 * Resolves a [com.abrarshakhi.lumen.core.domain.text.TextValue] outside composition.
 *
 * Effects are handled in a coroutine rather than during composition, so the `@Composable`
 * resolver cannot be used; only [TextValue.Raw] reaches here in practice.
 */
private fun com.abrarshakhi.lumen.core.domain.text.TextValue.asString(): String = when (this) {
    is com.abrarshakhi.lumen.core.domain.text.TextValue.Raw -> value
    is com.abrarshakhi.lumen.core.domain.text.TextValue.Res -> ""
}
