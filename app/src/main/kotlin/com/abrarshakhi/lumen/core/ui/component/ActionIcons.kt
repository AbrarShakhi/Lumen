package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import com.abrarshakhi.lumen.core.domain.search.IconSource

internal fun IconSource.toActionVector(): ImageVector = when (this) {
    is IconSource.Vector -> icon.toImageVector()
    else -> Icons.AutoMirrored.Filled.ArrowForward
}
