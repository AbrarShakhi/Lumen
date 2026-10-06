package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.core.domain.document.InlineRun
import com.abrarshakhi.lumen.core.domain.document.MarkdownBlock

@Composable
fun MarkdownBlockView(
    block: MarkdownBlock,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (block) {
        is MarkdownBlock.Heading -> InlineText(
            runs = block.content,
            style = block.level.headingStyle(),
            onOpenUrl = onOpenUrl,
            modifier = modifier.padding(top = if (block.level <= 2) 20.dp else 12.dp, bottom = 4.dp),
            color = if (block.level <= 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )

        is MarkdownBlock.Paragraph -> InlineText(
            runs = block.content,
            style = MaterialTheme.typography.bodyLarge,
            onOpenUrl = onOpenUrl,
            modifier = modifier.padding(vertical = 6.dp),
        )

        is MarkdownBlock.ListBlock -> Column(
            modifier = modifier.padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            block.items.forEachIndexed { index, item ->
                Row {
                    Text(
                        text = if (block.ordered) "${index + 1}." else BULLET,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(MARKER_WIDTH),
                    )
                    InlineText(
                        runs = item,
                        style = MaterialTheme.typography.bodyLarge,
                        onOpenUrl = onOpenUrl,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        is MarkdownBlock.Table -> MarkdownTable(block, onOpenUrl, modifier.padding(vertical = 8.dp))

        is MarkdownBlock.Code -> Surface(
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(14.dp),
            )
        }

        MarkdownBlock.Rule -> HorizontalDivider(modifier.padding(vertical = 12.dp))
    }
}

@Composable
private fun MarkdownTable(
    table: MarkdownBlock.Table,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column {
            TableRow(table.header, header = true, onOpenUrl = onOpenUrl)
            table.rows.forEach { row ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                TableRow(row, header = false, onOpenUrl = onOpenUrl)
            }
        }
    }
}

@Composable
private fun TableRow(
    cells: List<List<InlineRun>>,
    header: Boolean,
    onOpenUrl: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        cells.forEach { cell ->
            InlineText(
                runs = cell,
                style = if (header) {
                    MaterialTheme.typography.labelLargeEmphasized
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                onOpenUrl = onOpenUrl,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun InlineText(
    runs: List<InlineRun>,
    style: TextStyle,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val codeBackground = MaterialTheme.colorScheme.surfaceContainerHighest
    val currentOnOpenUrl by rememberUpdatedState(onOpenUrl)
    val text = remember(runs, linkColor, codeBackground) {
        runs.toAnnotatedString(linkColor, codeBackground) { url -> currentOnOpenUrl(url) }
    }
    Text(text = text, style = style, color = color, modifier = modifier)
}

private fun List<InlineRun>.toAnnotatedString(
    linkColor: Color,
    codeBackground: Color,
    onOpenUrl: (String) -> Unit,
): AnnotatedString = buildAnnotatedString {
    forEach { run ->
        val style = SpanStyle(
            fontWeight = if (run.bold) FontWeight.SemiBold else null,
            fontStyle = if (run.italic) FontStyle.Italic else null,
            fontFamily = if (run.code) FontFamily.Monospace else null,
            background = if (run.code) codeBackground else Color.Unspecified,
        )
        val url = run.url
        if (url == null) {
            withStyle(style) { append(run.text) }
        } else {
            withLink(
                LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    ),
                    linkInteractionListener = { onOpenUrl(url) },
                ),
            ) {
                withStyle(style) { append(run.text) }
            }
        }
    }
}

@Composable
private fun Int.headingStyle(): TextStyle = when (this) {
    1 -> MaterialTheme.typography.headlineSmallEmphasized
    2 -> MaterialTheme.typography.titleLargeEmphasized
    else -> MaterialTheme.typography.titleMediumEmphasized
}

private const val BULLET = "•"
private val MARKER_WIDTH = 24.dp
