package com.abrarshakhi.lumen.core.ui.text

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.lumen.core.domain.text.TextValue

@Composable
fun TextValue.resolve(): String = when (this) {
    is TextValue.Raw -> value
    is TextValue.Res ->
        if (args.isEmpty()) stringResource(id) else stringResource(id, *args.toTypedArray())
}

fun TextValue.resolve(resources: Resources): String = when (this) {
    is TextValue.Raw -> value
    is TextValue.Res ->
        if (args.isEmpty()) resources.getString(id) else resources.getString(id, *args.toTypedArray())
}
