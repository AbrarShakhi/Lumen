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

    val textFieldState = remember { TextFieldState(initialText = initialQuery.orEmpty()) }

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
        onDismiss = onCloseSurface,
        onOpenSettings = { onNavigate(AppRouteKey.Settings) },
    )
}

private fun com.abrarshakhi.lumen.core.domain.text.TextValue.asString(): String = when (this) {
    is com.abrarshakhi.lumen.core.domain.text.TextValue.Raw -> value
    is com.abrarshakhi.lumen.core.domain.text.TextValue.Res -> ""
}
