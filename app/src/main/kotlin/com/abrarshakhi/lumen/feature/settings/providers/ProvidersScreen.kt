package com.abrarshakhi.lumen.feature.settings.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.component.EmptyState
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenListColors
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.LumenSwitch
import com.abrarshakhi.lumen.core.ui.component.ShapedIcon
import com.abrarshakhi.lumen.core.ui.component.toImageVector
import com.abrarshakhi.lumen.core.ui.text.resolve

@Composable
fun ProvidersScreen(
    state: ProvidersState,
    onIntent: (ProvidersIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LumenScaffold(
        title = stringResource(R.string.providers_title),
        subtitle = stringResource(R.string.providers_subtitle),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        if (state.providers.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.providers_empty),
                subtitle = stringResource(R.string.settings_search_sources_summary),
                modifier = Modifier.padding(innerPadding),
            )
            return@LumenScaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            itemsIndexed(state.providers, key = { _, provider -> provider.id.value }) { index, provider ->
                ProviderRow(
                    provider = provider,
                    index = index,
                    count = state.providers.size,
                    onToggle = { enabled ->
                        onIntent(ProvidersIntent.EnabledSet(provider.id, enabled))
                    },
                    onResolvePermission = {
                        onIntent(ProvidersIntent.PermissionRequested(provider.id))
                    },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    provider: ProviderSetting,
    index: Int,
    count: Int,
    onToggle: (Boolean) -> Unit,
    onResolvePermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val permission = provider.permission
    val needsAttention = permission is ProviderPermissionState.Missing ||
        permission is ProviderPermissionState.Blocked
    val shapes = ListItemDefaults.segmentedShapes(index, count)
    val leading = @Composable {
        ShapedIcon(
            icon = provider.icon.toImageVector(),
            shape = SHAPE_CYCLE[index % SHAPE_CYCLE.size],
            tone = if (needsAttention) IconTone.Error else TONE_CYCLE[index % TONE_CYCLE.size],
        )
    }
    val supporting: (@Composable () -> Unit)? = permission.label()?.let { label ->
        {
            Text(
                text = stringResource(label),
                color = if (needsAttention) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }

    if (needsAttention) {
        SegmentedListItem(
            onClick = onResolvePermission,
            shapes = shapes,
            modifier = modifier,
            colors = LumenListColors.raised(),
            leadingContent = leading,
            supportingContent = supporting,
            trailingContent = {
                FilledTonalButton(onClick = onResolvePermission, shapes = ButtonDefaults.shapes()) {
                    Text(
                        stringResource(
                            if (permission is ProviderPermissionState.Blocked) {
                                R.string.providers_open_settings
                            } else {
                                R.string.providers_allow
                            },
                        ),
                    )
                }
            },
        ) {
            Text(provider.name.resolve())
        }
    } else {
        SegmentedListItem(
            checked = provider.enabled,
            onCheckedChange = onToggle,
            shapes = shapes,
            modifier = modifier,
            colors = LumenListColors.raised(),
            leadingContent = leading,
            supportingContent = supporting,
            trailingContent = { LumenSwitch(checked = provider.enabled) },
        ) {
            Text(provider.name.resolve())
        }
    }
}

private val SHAPE_CYCLE = listOf(
    LumenShape.Cookie,
    LumenShape.Clover,
    LumenShape.Sunny,
    LumenShape.Gem,
    LumenShape.Puffy,
    LumenShape.Flower,
)

private val TONE_CYCLE = listOf(IconTone.Primary, IconTone.Secondary, IconTone.Tertiary)

private fun ProviderPermissionState.label(): Int? = when (this) {
    ProviderPermissionState.NotRequired -> null
    ProviderPermissionState.Granted -> R.string.providers_permission_granted
    is ProviderPermissionState.Missing -> R.string.providers_permission_needed
    is ProviderPermissionState.Blocked -> R.string.providers_permission_blocked
}
