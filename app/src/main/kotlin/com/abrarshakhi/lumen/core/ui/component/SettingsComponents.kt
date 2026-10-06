package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ListItemShapes

@Immutable
sealed interface SettingsItem {
    val key: String

    data class Link(
        override val key: String,
        val title: String,
        val icon: ImageVector,
        val onClick: () -> Unit,
        val summary: String? = null,
        val tone: IconTone = IconTone.Primary,
        val shape: LumenShape = LumenShape.Circle,
    ) : SettingsItem

    data class Toggle(
        override val key: String,
        val title: String,
        val icon: ImageVector,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val summary: String? = null,
        val enabled: Boolean = true,
        val tone: IconTone = IconTone.Primary,
        val shape: LumenShape = LumenShape.Circle,
    ) : SettingsItem

    data class Custom(
        override val key: String,
        val content: @Composable () -> Unit,
    ) : SettingsItem
}

@Composable
fun SettingsGroup(
    items: List<SettingsItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        title?.let { SettingsGroupTitle(it) }
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            items.forEachIndexed { index, item ->
                key(item.key) {
                    SettingsItemRow(item, ListItemDefaults.segmentedShapes(index, items.size))
                }
            }
        }
    }
}

@Composable
fun SettingsGroupTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsItemRow(item: SettingsItem, shapes: ListItemShapes) {
    when (item) {
        is SettingsItem.Link -> SegmentedListItem(
            onClick = item.onClick,
            shapes = shapes,
            colors = LumenListColors.raised(),
            leadingContent = { ShapedIcon(item.icon, shape = item.shape, tone = item.tone) },
            supportingContent = item.summary?.let { summary -> { Text(summary) } },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
        ) {
            Text(item.title)
        }

        is SettingsItem.Toggle -> SegmentedListItem(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            shapes = shapes,
            enabled = item.enabled,
            colors = LumenListColors.raised(),
            leadingContent = { ShapedIcon(item.icon, shape = item.shape, tone = item.tone) },
            supportingContent = item.summary?.let { summary -> { Text(summary) } },
            trailingContent = { LumenSwitch(checked = item.checked, enabled = item.enabled) },
        ) {
            Text(item.title)
        }

        is SettingsItem.Custom -> Surface(
            shape = shapes.shape,
            color = LumenListColors.raised().containerColor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            item.content()
        }
    }
}

@Composable
fun LumenSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = null,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        },
    )
}

@Composable
fun SettingsControl(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    summary: String? = null,
    tone: IconTone = IconTone.Primary,
    shape: LumenShape = LumenShape.Circle,
    control: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShapedIcon(icon, shape = shape, tone = tone)
            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Column(Modifier.padding(top = 14.dp)) { control() }
    }
}
