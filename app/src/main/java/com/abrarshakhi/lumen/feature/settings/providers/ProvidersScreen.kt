package com.abrarshakhi.lumen.feature.settings.providers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.text.resolve

/**
 * One row per registered search source: switch it off, or resolve whatever permission is
 * standing in its way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    state: ProvidersState,
    onIntent: (ProvidersIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.providers_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.providers.isEmpty()) {
            Text(
                text = stringResource(R.string.providers_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(innerPadding).padding(24.dp),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
        ) {
            items(state.providers, key = { it.id.value }) { provider ->
                ProviderRow(
                    provider = provider,
                    onToggle = { enabled ->
                        onIntent(ProvidersIntent.EnabledSet(provider.id, enabled))
                    },
                    onResolvePermission = {
                        onIntent(ProvidersIntent.PermissionRequested(provider.id))
                    },
                )
            }
        }
    }
}

@Composable
private fun ProviderRow(
    provider: ProviderSetting,
    onToggle: (Boolean) -> Unit,
    onResolvePermission: () -> Unit,
) {
    val permissionLabel = provider.permission.label()
    // When something is blocking the source, the row's job is to resolve that rather than
    // to toggle a switch that would have no visible effect.
    val needsAttention = provider.permission is ProviderPermissionState.Missing ||
        provider.permission is ProviderPermissionState.Blocked

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = if (needsAttention) Role.Button else Role.Switch) {
                if (needsAttention) onResolvePermission() else onToggle(!provider.enabled)
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = provider.name.resolve(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            permissionLabel?.let { label ->
                Text(
                    text = stringResource(label),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (needsAttention) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = provider.enabled,
            onCheckedChange = null,
            // A source that cannot run is not meaningfully "on", so the switch stays
            // inert until the permission is resolved.
            enabled = !needsAttention,
        )
    }
}

private fun ProviderPermissionState.label(): Int? = when (this) {
    ProviderPermissionState.NotRequired -> null
    ProviderPermissionState.Granted -> R.string.providers_permission_granted
    is ProviderPermissionState.Missing -> R.string.providers_permission_needed
    is ProviderPermissionState.Blocked -> R.string.providers_permission_blocked
}
