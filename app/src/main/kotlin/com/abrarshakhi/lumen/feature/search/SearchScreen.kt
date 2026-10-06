package com.abrarshakhi.lumen.feature.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.ui.component.EmptyState
import com.abrarshakhi.lumen.core.ui.component.LumenSearchField
import com.abrarshakhi.lumen.core.ui.component.ResultActionSheet

enum class SearchPresentation { Fullscreen, Panel }

private enum class BodyMode { Home, Results, Pending, NoResults }

private val SearchState.hasContent: Boolean
    get() = hasResults || permissionRequests.isNotEmpty()

private val SearchState.bodyMode: BodyMode
    get() = when {
        hasContent -> BodyMode.Results
        isQueryBlank -> BodyMode.Home
        isSearching -> BodyMode.Pending
        else -> BodyMode.NoResults
    }

@Composable
fun SearchScreen(
    state: SearchState,
    textFieldState: TextFieldState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    presentation: SearchPresentation = SearchPresentation.Fullscreen,
    barPosition: SearchBarPosition = SearchBarPosition.Bottom,
    autoFocus: Boolean = true,
    onDismiss: () -> Unit = {},
    onNavigate: (AppRouteKey) -> Unit = {},
) {
    state.sheet?.let { sheet ->
        ResultActionSheet(
            result = sheet.result,
            onAction = { action ->
                onIntent(SearchIntent.ActionInvoked(sheet.result.id, action.id))
            },
            onDismiss = { onIntent(SearchIntent.SheetDismissed) },
        )
    }

    val onQuickAction: (QuickAction) -> Unit = { action ->
        when (action) {
            is QuickAction.Navigate -> onNavigate(action.route)
            is QuickAction.Prefill -> textFieldState.setTextAndPlaceCursorAtEnd(action.query)
        }
    }

    val field = @Composable {
        LumenSearchField(
            state = textFieldState,
            onSubmit = { state.topResult?.let { onIntent(SearchIntent.ResultActivated(it.id)) } },
            onClear = { onIntent(SearchIntent.Cleared) },
            isSearching = state.isSearching,
            onOpenSettings = { onNavigate(AppRouteKey.Settings) },
            autoFocus = autoFocus,
        )
    }

    when (presentation) {
        SearchPresentation.Fullscreen -> FullscreenSearch(
            state = state,
            field = field,
            onIntent = onIntent,
            onQuickAction = onQuickAction,
            barPosition = barPosition,
            modifier = modifier,
        )

        SearchPresentation.Panel -> PanelSearch(
            state = state,
            field = field,
            onIntent = onIntent,
            barPosition = barPosition,
            onDismiss = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun FullscreenSearch(
    state: SearchState,
    field: @Composable () -> Unit,
    onIntent: (SearchIntent) -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    barPosition: SearchBarPosition,
    modifier: Modifier,
) {
    val motion = MaterialTheme.motionScheme
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (barPosition == SearchBarPosition.Top) field()

            AnimatedContent(
                targetState = state.bodyMode,
                transitionSpec = {
                    (fadeIn(motion.defaultEffectsSpec()) +
                        scaleIn(motion.defaultSpatialSpec(), initialScale = 0.96f))
                        .togetherWith(fadeOut(motion.fastEffectsSpec()))
                },
                contentAlignment = Alignment.Center,
                label = "search-body",
                modifier = Modifier.weight(1f),
            ) { mode ->
                when (mode) {
                    BodyMode.Home -> SearchHome(onQuickAction = onQuickAction)
                    BodyMode.Results -> SearchResultsList(state, onIntent, Modifier.fillMaxSize())
                    BodyMode.Pending -> Box(Modifier.fillMaxSize())
                    BodyMode.NoResults -> EmptyState(
                        title = stringResource(R.string.search_empty_title),
                        subtitle = stringResource(R.string.search_empty_subtitle, state.query),
                    )
                }
            }

            if (barPosition == SearchBarPosition.Bottom) field()
        }
    }
}

@Composable
private fun PanelSearch(
    state: SearchState,
    field: @Composable () -> Unit,
    onIntent: (SearchIntent) -> Unit,
    barPosition: SearchBarPosition,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    val visibility = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
            .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
        contentAlignment = if (barPosition == SearchBarPosition.Top) {
            Alignment.TopCenter
        } else {
            Alignment.BottomCenter
        },
    ) {
        AnimatedVisibility(
            visibleState = visibility,
            enter = fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()) +
                slideInVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) { height ->
                    if (barPosition == SearchBarPosition.Top) -height / 3 else height / 3
                },
            modifier = Modifier.safeDrawingPadding(),
        ) {
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .pointerInput(Unit) { detectTapGestures { } },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                shadowElevation = 16.dp,
            ) {
                Column(Modifier.animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())) {
                    if (barPosition == SearchBarPosition.Top) field()
                    if (state.hasContent) {
                        SearchResultsList(
                            state = state,
                            onIntent = onIntent,
                            modifier = Modifier.heightIn(max = PANEL_RESULTS_MAX_HEIGHT),
                        )
                    }
                    if (barPosition == SearchBarPosition.Bottom) field()
                }
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.32f
private val PANEL_RESULTS_MAX_HEIGHT = 420.dp
