package com.abrarshakhi.lumen.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.ui.component.LumenSearchField
import com.abrarshakhi.lumen.core.ui.component.PermissionRequestCard
import com.abrarshakhi.lumen.core.ui.component.ResultActionSheet
import com.abrarshakhi.lumen.core.ui.component.ResultRow

enum class SearchPresentation { Fullscreen, Panel }

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
    onOpenSettings: (() -> Unit)? = null,
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

    when (presentation) {
        SearchPresentation.Fullscreen -> Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            SearchBody(
                state = state,
                textFieldState = textFieldState,
                onIntent = onIntent,
                barPosition = barPosition,
                autoFocus = autoFocus,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                fillsAvailableSpace = true,
            )
        }

        SearchPresentation.Panel -> SearchPanel(
            state = state,
            textFieldState = textFieldState,
            onIntent = onIntent,
            modifier = modifier,
            barPosition = barPosition,
            autoFocus = autoFocus,
            onDismiss = onDismiss,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
private fun SearchPanel(
    state: SearchState,
    textFieldState: TextFieldState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier,
    barPosition: SearchBarPosition,
    autoFocus: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: (() -> Unit)?,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
            .pointerInput(onDismiss) { detectTapGestures { onDismiss() } },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth()
                .pointerInput(Unit) { detectTapGestures { } },
            shape = RoundedCornerShape(PANEL_CORNER),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 16.dp,
        ) {
            SearchBody(
                state = state,
                textFieldState = textFieldState,
                onIntent = onIntent,
                barPosition = barPosition,
                autoFocus = autoFocus,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
                fillsAvailableSpace = false,
            )
        }
    }
}

@Composable
private fun SearchBody(
    state: SearchState,
    textFieldState: TextFieldState,
    onIntent: (SearchIntent) -> Unit,
    barPosition: SearchBarPosition,
    autoFocus: Boolean,
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier,
    fillsAvailableSpace: Boolean,
) {
    Column(modifier) {
        val resultsModifier = if (fillsAvailableSpace) {
            Modifier.weight(1f)
        } else {
            Modifier.heightIn(max = PANEL_RESULTS_MAX_HEIGHT)
        }

        val field = @Composable {
            LumenSearchField(
                state = textFieldState,
                onSubmit = {
                    state.topResult?.let { onIntent(SearchIntent.ResultActivated(it.id)) }
                },
                onClear = { onIntent(SearchIntent.Cleared) },
                onOpenSettings = onOpenSettings,
                autoFocus = autoFocus,
            )
        }
        val divider = @Composable {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        }

        val hasContent = state.hasResults || state.permissionRequests.isNotEmpty()

        if (barPosition == SearchBarPosition.Top) {
            field()
            if (hasContent || fillsAvailableSpace) divider()
        }

        Box(resultsModifier) {
            when {
                hasContent -> ResultList(state, onIntent)

                !fillsAvailableSpace -> Unit

                state.isQueryBlank -> EmptyMessage(
                    title = stringResource(R.string.search_idle_title),
                    subtitle = stringResource(R.string.search_idle_subtitle),
                )

                !state.isSearching -> EmptyMessage(
                    title = stringResource(R.string.search_empty_title),
                    subtitle = stringResource(R.string.search_empty_subtitle, state.query),
                )
            }
        }

        if (barPosition == SearchBarPosition.Bottom) {
            if (hasContent || fillsAvailableSpace) divider()
            field()
        }
    }
}

@Composable
private fun ResultList(state: SearchState, onIntent: (SearchIntent) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(
            items = state.permissionRequests,
            key = { request -> "permission:${request.providerId.value}" },
            contentType = { PERMISSION_CONTENT_TYPE },
        ) { request ->
            PermissionRequestCard(
                request = request,
                onGrant = { onIntent(SearchIntent.PermissionRequested(request.providerId)) },
                onDismiss = {
                    onIntent(SearchIntent.PermissionPromptDismissed(request.providerId))
                },
            )
        }

        state.sections.forEach { section ->
            item(
                key = "header:${section.category.name}",
                contentType = SECTION_HEADER_CONTENT_TYPE,
            ) {
                SectionHeader(section)
            }

            items(
                items = section.results,
                key = { result -> result.id.value },
                contentType = { result -> result.category },
            ) { result ->
                ResultRow(
                    result = result,
                    onActivate = { onIntent(SearchIntent.ResultActivated(result.id)) },
                    onLongPress = { onIntent(SearchIntent.ResultLongPressed(result.id)) },
                    onAction = { action ->
                        onIntent(SearchIntent.ActionInvoked(result.id, action.id))
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(section: ResultSection) {
    Text(
        text = stringResource(section.category.titleRes()),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyMessage(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private val PANEL_CORNER = 28.dp
private const val SCRIM_ALPHA = 0.32f
private val PANEL_RESULTS_MAX_HEIGHT = 380.dp
private const val SECTION_HEADER_CONTENT_TYPE = "section-header"
private const val PERMISSION_CONTENT_TYPE = "permission-request"

private fun ResultCategory.titleRes(): Int = when (this) {
    ResultCategory.Answer -> R.string.section_answer
    ResultCategory.App -> R.string.section_app
    ResultCategory.Shortcut -> R.string.section_shortcut
    ResultCategory.Contact -> R.string.section_contact
    ResultCategory.Note -> R.string.section_note
    ResultCategory.File -> R.string.section_file
    ResultCategory.CalendarEvent -> R.string.section_calendar
    ResultCategory.Setting -> R.string.section_setting
    ResultCategory.WebSearch -> R.string.section_web
    ResultCategory.System -> R.string.section_system
}
