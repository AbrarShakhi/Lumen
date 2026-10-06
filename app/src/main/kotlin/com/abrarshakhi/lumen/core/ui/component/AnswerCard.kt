package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.search.ExpandedContent
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.ui.text.resolve

@Composable
fun AnswerCard(
    result: SearchResult,
    onActivate: () -> Unit,
    onLongPress: () -> Unit,
    onAction: (ResultAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = MaterialTheme.colorScheme.primaryContainer
    val content = MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(container)
            .combinedClickable(
                role = Role.Button,
                onClickLabel = result.actions.primary.label.resolve(),
                onLongClickLabel = stringResource(R.string.action_more),
                onClick = onActivate,
                onLongClick = onLongPress,
            )
            .animateContentSize()
            .padding(20.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResultIcon(result.icon, size = 28.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = result.subtitle ?: stringResource(R.string.section_answer),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                result.trailing?.let { TrailingSlot(it) }
            }

            val hasBody = (result.expanded as? ExpandedContent.Body)?.text?.isNotBlank() == true
            if (!hasBody) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    maxLines = TITLE_MAX_LINES,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            result.expanded?.let { ExpandedBody(it) }

            val actions = result.actions.secondary
            if (actions.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    actions.forEach { action ->
                        FilledTonalButton(
                            onClick = { onAction(action) },
                            shapes = ButtonDefaults.shapes(),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        ) {
                            Icon(
                                imageVector = action.icon.toActionVector(),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(action.label.resolve())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedBody(expanded: ExpandedContent) {
    when (expanded) {
        is ExpandedContent.Body -> if (expanded.text.isNotBlank()) {
            Text(
                text = expanded.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        is ExpandedContent.KeyValues -> Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            expanded.entries.forEach { (key, value) ->
                Row {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = value, style = MaterialTheme.typography.bodyMediumEmphasized)
                }
            }
        }
    }
}

private const val TITLE_MAX_LINES = 4
