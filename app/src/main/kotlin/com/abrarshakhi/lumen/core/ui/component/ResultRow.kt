package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.TrailingContent
import com.abrarshakhi.lumen.core.ui.text.resolve

private const val MAX_INLINE_ACTIONS = 2

@Composable
fun ResultRow(
    result: SearchResult,
    onActivate: () -> Unit,
    onLongPress: () -> Unit,
    onAction: (ResultAction) -> Unit,
    modifier: Modifier = Modifier,
    shapes: ListItemShapes = ListItemDefaults.shapes(),
) {
    val inlineActions = result.actions.secondary.take(MAX_INLINE_ACTIONS)
    val hasTrailing = result.trailing != null || inlineActions.isNotEmpty()

    SegmentedListItem(
        onClick = onActivate,
        onLongClick = onLongPress,
        onLongClickLabel = stringResource(R.string.action_more),
        shapes = shapes,
        modifier = modifier,
        colors = LumenListColors.tonal(),
        leadingContent = { ResultIcon(result.icon) },
        supportingContent = result.subtitle?.let { subtitle ->
            { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        },
        trailingContent = if (hasTrailing) {
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    result.trailing?.let { TrailingSlot(it) }
                    inlineActions.forEach { action ->
                        IconButton(
                            onClick = { onAction(action) },
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector = action.icon.toActionVector(),
                                contentDescription = action.label.resolve(),
                            )
                        }
                    }
                }
            }
        } else {
            null
        },
    ) {
        Text(
            text = result.highlightedTitle(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TrailingSlot(trailing: TrailingContent) {
    when (trailing) {
        is TrailingContent.Text -> Text(
            text = trailing.value,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.primary,
        )

        is TrailingContent.Badge -> Text(
            text = trailing.label.resolve(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is TrailingContent.Toggle -> LumenSwitch(checked = trailing.checked)

        TrailingContent.Progress -> LoadingIndicator(Modifier.size(24.dp))
    }
}

@Composable
private fun SearchResult.highlightedTitle(): AnnotatedString {
    val emphasis = MaterialTheme.colorScheme.primary
    return remember(title, titleMatches, emphasis) {
        if (titleMatches.isEmpty()) {
            AnnotatedString(title)
        } else {
            buildAnnotatedString {
                append(title)
                titleMatches.forEach { range ->
                    val start = range.first.coerceIn(0, title.length)
                    val end = (range.last + 1).coerceIn(start, title.length)
                    if (start < end) {
                        addStyle(
                            SpanStyle(color = emphasis, fontWeight = FontWeight.SemiBold),
                            start,
                            end,
                        )
                    }
                }
            }
        }
    }
}
