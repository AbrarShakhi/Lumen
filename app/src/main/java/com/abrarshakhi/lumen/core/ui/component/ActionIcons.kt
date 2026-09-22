package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.graphics.vector.ImageVector
import com.abrarshakhi.lumen.core.domain.search.IconSource

/**
 * Actions always render as a glyph, so non-vector icon sources fall back to a generic one
 * rather than trying to rasterise an app icon into an icon button.
 */
internal fun IconSource.toActionVector(): ImageVector = when (this) {
    is IconSource.Vector -> icon.toImageVector()
    else -> Icons.AutoMirrored.Filled.ArrowForward
}
