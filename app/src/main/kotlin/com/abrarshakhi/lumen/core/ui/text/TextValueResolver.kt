package com.abrarshakhi.lumen.core.ui.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.lumen.core.domain.text.TextValue

/**
 * Resolves a domain [TextValue] against Android resources at the point of rendering.
 *
 * Uses `stringResource` rather than `LocalContext.current.getString`. The latter is not
 * configuration-aware: it reads from a context captured at composition time, so a locale or
 * configuration change leaves stale text on screen until something else happens to
 * recompose that node.
 */
@Composable
fun TextValue.resolve(): String = when (this) {
    is TextValue.Raw -> value
    is TextValue.Res ->
        if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())
}
