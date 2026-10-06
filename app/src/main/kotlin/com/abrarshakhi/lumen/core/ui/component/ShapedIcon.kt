package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconTone { Primary, Secondary, Tertiary, Error, Neutral }

@Composable
fun IconTone.containerColor(): Color = when (this) {
    IconTone.Primary -> MaterialTheme.colorScheme.primaryContainer
    IconTone.Secondary -> MaterialTheme.colorScheme.secondaryContainer
    IconTone.Tertiary -> MaterialTheme.colorScheme.tertiaryContainer
    IconTone.Error -> MaterialTheme.colorScheme.errorContainer
    IconTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHighest
}

@Composable
fun IconTone.contentColor(): Color = when (this) {
    IconTone.Primary -> MaterialTheme.colorScheme.onPrimaryContainer
    IconTone.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
    IconTone.Tertiary -> MaterialTheme.colorScheme.onTertiaryContainer
    IconTone.Error -> MaterialTheme.colorScheme.onErrorContainer
    IconTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun ShapedIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    shape: LumenShape = LumenShape.Circle,
    tone: IconTone = IconTone.Primary,
    size: Dp = 40.dp,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape.asShape())
            .background(tone.containerColor()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tone.contentColor(),
            modifier = Modifier.size(size * ICON_FRACTION),
        )
    }
}

private const val ICON_FRACTION = 0.5f
