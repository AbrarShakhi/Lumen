package com.abrarshakhi.lumen.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.ui.component.SettingsChoiceRow
import com.abrarshakhi.lumen.core.ui.component.SettingsNavigationRow
import com.abrarshakhi.lumen.core.ui.component.SettingsSection
import com.abrarshakhi.lumen.core.ui.component.SettingsSliderRow
import com.abrarshakhi.lumen.core.ui.component.SettingsSwitchRow
import kotlin.math.roundToInt

/**
 * Stateless settings screen.
 *
 * Owns its own Scaffold: Lumen has no shared chrome layer, because the full-bleed search
 * surface and an ordinary settings list have nothing to share.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    onOpenProviders: () -> Unit,
    onOpenFileFolders: () -> Unit,
    onOpenLauncherMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val preferences = state.preferences

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSection(stringResource(R.string.settings_appearance)) {
                SettingsChoiceRow(
                    title = stringResource(R.string.settings_theme),
                    options = ThemeMode.entries,
                    selected = preferences.themeMode,
                    optionLabel = { mode -> stringResource(mode.labelRes()) },
                    onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                )

                SettingsSwitchRow(
                    title = stringResource(R.string.settings_dynamic_color),
                    summary = stringResource(R.string.settings_dynamic_color_summary),
                    checked = preferences.dynamicColor,
                    onCheckedChange = { onIntent(SettingsIntent.DynamicColorSet(it)) },
                )

                SettingsSliderRow(
                    title = stringResource(R.string.settings_font_size),
                    value = preferences.fontScale,
                    valueLabel = stringResource(
                        R.string.settings_font_size_value,
                        (preferences.fontScale * 100).roundToInt(),
                    ),
                    range = UserPreferences.MIN_FONT_SCALE..UserPreferences.MAX_FONT_SCALE,
                    // Discrete stops keep the scale to sensible 5% increments and make the
                    // control reachable with a screen reader.
                    steps = FONT_SCALE_STEPS,
                    onValueChange = { onIntent(SettingsIntent.FontScaleSet(it)) },
                )
            }

            SettingsSection(stringResource(R.string.settings_layout)) {
                SettingsChoiceRow(
                    title = stringResource(R.string.settings_bar_position),
                    options = SearchBarPosition.entries,
                    selected = preferences.searchBarPosition,
                    optionLabel = { position -> stringResource(position.labelRes()) },
                    onSelect = { onIntent(SettingsIntent.SearchBarPositionSelected(it)) },
                )
            }

            SettingsSection(stringResource(R.string.settings_search_sources)) {
                SettingsNavigationRow(
                    title = stringResource(R.string.providers_title),
                    summary = stringResource(R.string.settings_search_sources_summary),
                    onClick = onOpenProviders,
                )

                SettingsNavigationRow(
                    title = stringResource(R.string.files_folders_title),
                    summary = stringResource(R.string.files_add_folder),
                    onClick = onOpenFileFolders,
                )

                SettingsNavigationRow(
                    title = stringResource(R.string.launcher_title),
                    summary = stringResource(R.string.launcher_offer_summary),
                    onClick = onOpenLauncherMode,
                )
            }
        }
    }
}

/**
 * Number of intermediate stops on the font-size slider.
 *
 * The range spans 0.85..1.30 and 5% increments give ten positions, so nine sit between the
 * two ends.
 */
private const val FONT_SCALE_STEPS = 8

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.System -> R.string.settings_theme_system
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
}

private fun SearchBarPosition.labelRes(): Int = when (this) {
    SearchBarPosition.Top -> R.string.settings_bar_position_top
    SearchBarPosition.Bottom -> R.string.settings_bar_position_bottom
}
