package com.abrarshakhi.lumen.feature.settings.launcher

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.component.SettingsSwitchRow
import org.koin.androidx.compose.koinViewModel

/**
 * Launcher mode.
 *
 * Deliberately explicit about what this does. Replacing the home app is the most invasive
 * thing Lumen can do to a device, and ColorOS in particular can make handing the role back
 * fiddly — so the screen says plainly how to undo it before the user opts in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherSettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LauncherSettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val chooser = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { viewModel.refresh() }

    // The role can change in system settings while Lumen is backgrounded.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.launcher_title)) },
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
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = stringResource(R.string.launcher_explainer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            SettingsSwitchRow(
                title = stringResource(R.string.launcher_offer),
                summary = stringResource(R.string.launcher_offer_summary),
                checked = state.isOffered,
                onCheckedChange = viewModel::setOffered,
            )

            if (state.isOffered) {
                Button(
                    onClick = { viewModel.chooseHomeApp()?.let(chooser::launch) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    // Different wording once Lumen holds the role: the same button then
                    // exists to hand it back, not to take it.
                    Text(
                        stringResource(
                            if (state.isCurrentHome) R.string.launcher_change else R.string.launcher_choose,
                        ),
                    )
                }
            }

            Text(
                text = stringResource(
                    if (state.isCurrentHome) R.string.launcher_is_home else R.string.launcher_not_home,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Text(
                text = stringResource(R.string.launcher_revert_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )

            if (state.shortcutsAvailable) {
                Text(
                    text = stringResource(R.string.launcher_shortcuts_on),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}
