package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.theme.SearchFieldTextStyle

/**
 * The query field.
 *
 * Takes a [TextFieldState] from the caller rather than a `value`/`onValueChange` pair: the
 * text buffer belongs to the UI, not to screen state. Round-tripping every keystroke out to
 * a ViewModel and back is the classic cause of cursor jumps and broken IME composition, and
 * it puts a state-flow hop on the hottest path in the app.
 */
@Composable
fun LumenSearchField(
    state: TextFieldState,
    onSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null,
    autoFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )

        Spacer(Modifier.width(12.dp))

        // Judged on trimmed text: whitespace alone is not a query, so the field should
        // still read as empty rather than showing a clear button over a blank-looking field.
        val hasQuery = state.text.isNotBlank()

        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (!hasQuery) {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = SearchFieldTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    // The field itself is single-line; a wrapping hint would make the
                    // panel taller than the field it is standing in for.
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                state = state,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                textStyle = SearchFieldTextStyle.merge(
                    LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
                ),
                lineLimits = TextFieldLineLimits.SingleLine,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Go,
                ),
                onKeyboardAction = KeyboardActionHandler { onSubmit() },
            )
        }

        // One trailing slot, two jobs: clearing is only meaningful with a query, and
        // settings is only reachable before one is typed. Showing both would crowd the
        // field for no gain.
        when {
            hasQuery -> IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.search_clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            onOpenSettings != null -> IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = stringResource(R.string.settings_open),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
