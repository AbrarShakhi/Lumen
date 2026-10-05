package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.TrailingContent
import com.abrarshakhi.lumen.core.ui.text.resolve

/** How many secondary actions appear inline before the rest move to the long-press sheet. */
private const val MAX_INLINE_ACTIONS = 2

/**
 * One result.
 *
 * Renders any result from any provider: everything provider-specific arrives inside
 * [SearchResult] itself, so this composable never grows a branch per provider.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultRow(
    result: SearchResult,
    onActivate: () -> Unit,
    onLongPress: () -> Unit,
    onAction: (ResultAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onActivate, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResultIcon(result.icon)

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = result.highlightedTitle(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            result.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        result.trailing?.let { trailing ->
            Spacer(Modifier.width(8.dp))
            TrailingSlot(trailing)
        }

        result.actions.secondary.take(MAX_INLINE_ACTIONS).forEach { action ->
            IconButton(onClick = { onAction(action) }) {
                Icon(
                    imageVector = action.icon.toActionVector(),
                    contentDescription = action.label.resolve(),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrailingSlot(trailing: TrailingContent) {
    when (trailing) {
        is TrailingContent.Text -> Text(
            text = trailing.value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        is TrailingContent.Badge -> Text(
            text = trailing.label.resolve(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        is TrailingContent.Toggle -> Text(
            text = if (trailing.checked) "On" else "Off",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TrailingContent.Progress -> CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
        )
    }
}

/**
 * Bolds the characters the query matched.
 *
 * The ranges come from the matcher, so highlighting always reflects why the result was
 * returned rather than a second, independent substring search.
 */
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
