package com.abrarshakhi.lumen.feature.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.ui.component.AnswerCard
import com.abrarshakhi.lumen.core.ui.component.PermissionRequestCard
import com.abrarshakhi.lumen.core.ui.component.ResultRow

@Composable
internal fun SearchResultsList(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
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
                modifier = Modifier.animateItem().padding(bottom = 12.dp),
            )
        }

        state.sections.forEach { section ->
            if (section.category == ResultCategory.Answer) {
                answers(section, onIntent)
            } else {
                rows(section, onIntent)
            }
        }
    }
}

private fun LazyListScope.answers(
    section: ResultSection,
    onIntent: (SearchIntent) -> Unit,
) {
    items(
        items = section.results,
        key = { result -> result.id.value },
        contentType = { ANSWER_CONTENT_TYPE },
    ) { result ->
        AnswerCard(
            result = result,
            onActivate = { onIntent(SearchIntent.ResultActivated(result.id)) },
            onLongPress = { onIntent(SearchIntent.ResultLongPressed(result.id)) },
            onAction = { action -> onIntent(SearchIntent.ActionInvoked(result.id, action.id)) },
            modifier = Modifier.animateItem().padding(bottom = 12.dp),
        )
    }
}

private fun LazyListScope.rows(
    section: ResultSection,
    onIntent: (SearchIntent) -> Unit,
) {
    item(
        key = "header:${section.category.name}",
        contentType = SECTION_HEADER_CONTENT_TYPE,
    ) {
        SectionHeader(section, Modifier.animateItem())
    }

    itemsIndexed(
        items = section.results,
        key = { _, result -> result.id.value },
        contentType = { _, result -> result.category },
    ) { index, result ->
        val isLast = index == section.results.lastIndex
        ResultRow(
            result = result,
            onActivate = { onIntent(SearchIntent.ResultActivated(result.id)) },
            onLongPress = { onIntent(SearchIntent.ResultLongPressed(result.id)) },
            onAction = { action -> onIntent(SearchIntent.ActionInvoked(result.id, action.id)) },
            shapes = ListItemDefaults.segmentedShapes(index, section.results.size),
            modifier = Modifier
                .animateItem()
                .padding(bottom = if (isLast) 0.dp else ListItemDefaults.SegmentedGap),
        )
    }
}

@Composable
private fun SectionHeader(section: ResultSection, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(section.category.titleRes()),
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
    )
}

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

private const val SECTION_HEADER_CONTENT_TYPE = "section-header"
private const val PERMISSION_CONTENT_TYPE = "permission-request"
private const val ANSWER_CONTENT_TYPE = "answer"
