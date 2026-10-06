package com.abrarshakhi.lumen.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.preferences.ColorStyle
import com.abrarshakhi.lumen.core.domain.preferences.ThemeAccent
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.asShape
import com.abrarshakhi.lumen.core.ui.theme.seed

@Composable
internal fun AccentPicker(
    selected: ThemeAccent,
    enabled: Boolean,
    onSelect: (ThemeAccent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .horizontalScroll(rememberScrollState())
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(SWATCH_SPACING),
    ) {
        ThemeAccent.entries.forEach { accent ->
            AccentSwatch(
                accent = accent,
                selected = accent == selected,
                enabled = enabled,
                onClick = { onSelect(accent) },
            )
        }
    }
}

@Composable
private fun AccentSwatch(
    accent: ThemeAccent,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else UNSELECTED_SCALE,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "swatch-scale",
    )
    val name = stringResource(accent.labelRes())
    val shape = if (selected) LumenShape.Cookie.asShape() else LumenShape.Circle.asShape()

    Box(
        modifier = Modifier
            .size(SWATCH_SIZE)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(accent.seed)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = selected, enter = scaleIn(), exit = scaleOut()) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}

@Composable
internal fun ColorStylePicker(
    selected: ColorStyle,
    onSelect: (ColorStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ColorStyle.entries.forEach { style ->
            val isSelected = style == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(style) },
                label = { Text(stringResource(style.labelRes())) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

private fun ThemeAccent.labelRes(): Int = when (this) {
    ThemeAccent.Ember -> R.string.accent_ember
    ThemeAccent.Amber -> R.string.accent_amber
    ThemeAccent.Moss -> R.string.accent_moss
    ThemeAccent.Ocean -> R.string.accent_ocean
    ThemeAccent.Iris -> R.string.accent_iris
    ThemeAccent.Blossom -> R.string.accent_blossom
}

private fun ColorStyle.labelRes(): Int = when (this) {
    ColorStyle.TonalSpot -> R.string.style_tonal_spot
    ColorStyle.Vibrant -> R.string.style_vibrant
    ColorStyle.Expressive -> R.string.style_expressive
    ColorStyle.Neutral -> R.string.style_neutral
    ColorStyle.Fidelity -> R.string.style_fidelity
    ColorStyle.Monochrome -> R.string.style_monochrome
}

private const val DISABLED_ALPHA = 0.38f
private const val UNSELECTED_SCALE = 0.84f
private val SWATCH_SIZE = 40.dp
private val SWATCH_SPACING = 8.dp
