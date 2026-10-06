package com.abrarshakhi.lumen.feature.settings.launcher

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppShortcut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.SettingsControl
import com.abrarshakhi.lumen.core.ui.component.SettingsGroup
import com.abrarshakhi.lumen.core.ui.component.SettingsItem
import com.abrarshakhi.lumen.core.ui.component.ShapedIcon
import org.koin.androidx.compose.koinViewModel

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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    LauncherSettingsScreen(
        state = state,
        onOfferedChange = viewModel::setOffered,
        onChooseHome = { viewModel.chooseHomeApp()?.let(chooser::launch) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun LauncherSettingsScreen(
    state: LauncherSettingsState,
    onOfferedChange: (Boolean) -> Unit,
    onChooseHome: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LumenScaffold(
        title = stringResource(R.string.launcher_title),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HomeStatusCard(isCurrentHome = state.isCurrentHome)

            Text(
                text = stringResource(R.string.launcher_explainer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            )

            SettingsGroup(
                items = buildList {
                    add(
                        SettingsItem.Toggle(
                            key = "offer",
                            title = stringResource(R.string.launcher_offer),
                            summary = stringResource(R.string.launcher_offer_summary),
                            icon = Icons.Filled.Home,
                            shape = LumenShape.Arch,
                            checked = state.isOffered,
                            onCheckedChange = onOfferedChange,
                        ),
                    )
                    if (state.isOffered) {
                        add(
                            SettingsItem.Custom(key = "choose") {
                                SettingsControl(
                                    title = stringResource(
                                        if (state.isCurrentHome) R.string.launcher_change else R.string.launcher_choose,
                                    ),
                                    icon = Icons.Filled.SwapHoriz,
                                    shape = LumenShape.Clover,
                                    tone = IconTone.Secondary,
                                ) {
                                    Button(
                                        onClick = onChooseHome,
                                        shapes = ButtonDefaults.shapes(),
                                        modifier = Modifier.align(Alignment.End),
                                    ) {
                                        Text(
                                            stringResource(
                                                if (state.isCurrentHome) {
                                                    R.string.launcher_change
                                                } else {
                                                    R.string.launcher_choose
                                                },
                                            ),
                                        )
                                    }
                                }
                            },
                        )
                    }
                    if (state.shortcutsAvailable) {
                        add(
                            SettingsItem.Custom(key = "shortcuts") {
                                SettingsControl(
                                    title = stringResource(R.string.launcher_shortcuts_on),
                                    icon = Icons.Filled.AppShortcut,
                                    shape = LumenShape.Sunny,
                                    tone = IconTone.Tertiary,
                                ) {}
                            },
                        )
                    }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShapedIcon(
                    icon = Icons.Filled.SettingsBackupRestore,
                    tone = IconTone.Neutral,
                    size = 32.dp,
                )
                Text(
                    text = stringResource(R.string.launcher_revert_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun HomeStatusCard(isCurrentHome: Boolean) {
    val container by animateColorAsState(
        targetValue = if (isCurrentHome) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "home-status",
    )
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapedIcon(
                icon = Icons.Filled.Home,
                shape = LumenShape.Arch,
                tone = if (isCurrentHome) IconTone.Primary else IconTone.Neutral,
                size = 56.dp,
            )
            Text(
                text = stringResource(
                    if (isCurrentHome) R.string.launcher_is_home else R.string.launcher_not_home,
                ),
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
