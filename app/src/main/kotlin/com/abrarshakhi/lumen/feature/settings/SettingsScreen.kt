package com.abrarshakhi.lumen.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.BuildConfig
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.ui.component.ConnectedChoiceGroup
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenBrandMark
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.SettingsControl
import com.abrarshakhi.lumen.core.ui.component.SettingsGroup
import com.abrarshakhi.lumen.core.ui.component.SettingsItem
import com.abrarshakhi.lumen.core.ui.theme.supportsWallpaperColors
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onBack: () -> Unit,
    onNavigate: (AppRouteKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val preferences = state.preferences
    val context = LocalContext.current
    val wallpaperSupported = remember(context) { context.supportsWallpaperColors() }

    LumenScaffold(
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "hero") { SettingsHero() }

            item(key = "space") {
                SettingsGroup(
                    title = stringResource(R.string.settings_group_space),
                    items = spaceItems(onNavigate),
                )
            }

            item(key = "search") {
                SettingsGroup(
                    title = stringResource(R.string.settings_search_sources),
                    items = searchItems(onNavigate),
                )
            }

            item(key = "appearance") {
                SettingsGroup(
                    title = stringResource(R.string.settings_appearance),
                    items = appearanceItems(preferences, wallpaperSupported, onIntent),
                )
            }

            item(key = "layout") {
                SettingsGroup(
                    title = stringResource(R.string.settings_layout),
                    items = layoutItems(preferences, onIntent),
                )
            }
        }
    }
}

@Composable
private fun SettingsHero() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LumenBrandMark(size = 88.dp, animated = false)
            Column(Modifier.padding(start = 8.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                )
                Text(
                    text = stringResource(R.string.settings_tagline),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun spaceItems(onNavigate: (AppRouteKey) -> Unit): List<SettingsItem> = listOf(
    SettingsItem.Link(
        key = "notes",
        title = stringResource(R.string.notes_title),
        summary = stringResource(R.string.settings_notes_summary),
        icon = Icons.AutoMirrored.Filled.StickyNote2,
        shape = LumenShape.Cookie,
        tone = IconTone.Primary,
        onClick = { onNavigate(AppRouteKey.Notes) },
    ),
    SettingsItem.Link(
        key = "surfaces",
        title = stringResource(R.string.surfaces_title),
        summary = stringResource(R.string.settings_surfaces_summary),
        icon = Icons.Filled.Widgets,
        shape = LumenShape.Clover,
        tone = IconTone.Tertiary,
        onClick = { onNavigate(AppRouteKey.Surfaces) },
    ),
    SettingsItem.Link(
        key = "launcher",
        title = stringResource(R.string.launcher_title),
        summary = stringResource(R.string.launcher_offer_summary),
        icon = Icons.Filled.Home,
        shape = LumenShape.Arch,
        tone = IconTone.Secondary,
        onClick = { onNavigate(AppRouteKey.LauncherMode) },
    ),
)

@Composable
private fun searchItems(onNavigate: (AppRouteKey) -> Unit): List<SettingsItem> = listOf(
    SettingsItem.Link(
        key = "providers",
        title = stringResource(R.string.providers_title),
        summary = stringResource(R.string.settings_search_sources_summary),
        icon = Icons.AutoMirrored.Filled.ManageSearch,
        shape = LumenShape.Gem,
        tone = IconTone.Primary,
        onClick = { onNavigate(AppRouteKey.Providers) },
    ),
    SettingsItem.Link(
        key = "ai",
        title = stringResource(R.string.ai_title),
        summary = stringResource(R.string.settings_ai_summary),
        icon = Icons.Filled.AutoAwesome,
        shape = LumenShape.Sunny,
        tone = IconTone.Secondary,
        onClick = { onNavigate(AppRouteKey.AiSettings) },
    ),
    SettingsItem.Link(
        key = "folders",
        title = stringResource(R.string.files_folders_title),
        summary = stringResource(R.string.settings_folders_summary),
        icon = Icons.Filled.FolderOpen,
        shape = LumenShape.Puffy,
        tone = IconTone.Tertiary,
        onClick = { onNavigate(AppRouteKey.FileFolders) },
    ),
)

@Composable
private fun appearanceItems(
    preferences: UserPreferences,
    wallpaperSupported: Boolean,
    onIntent: (SettingsIntent) -> Unit,
): List<SettingsItem> {
    val usingWallpaper = wallpaperSupported && preferences.dynamicColor
    return listOf(
        SettingsItem.Custom(key = "theme") {
            SettingsControl(
                title = stringResource(R.string.settings_theme),
                icon = Icons.Filled.Contrast,
                shape = LumenShape.Cookie,
            ) {
                ConnectedChoiceGroup(
                    options = ThemeMode.entries,
                    selected = preferences.themeMode,
                    onSelect = { onIntent(SettingsIntent.ThemeModeSelected(it)) },
                    label = { mode -> stringResource(mode.labelRes()) },
                )
            }
        },
        SettingsItem.Toggle(
            key = "dynamic",
            title = stringResource(R.string.settings_dynamic_color),
            summary = stringResource(
                if (wallpaperSupported) {
                    R.string.settings_dynamic_color_summary
                } else {
                    R.string.settings_dynamic_color_unsupported
                },
            ),
            icon = Icons.Filled.Wallpaper,
            shape = LumenShape.Flower,
            tone = IconTone.Tertiary,
            checked = usingWallpaper,
            enabled = wallpaperSupported,
            onCheckedChange = { onIntent(SettingsIntent.DynamicColorSet(it)) },
        ),
        SettingsItem.Custom(key = "accent") {
            SettingsControl(
                title = stringResource(R.string.settings_accent),
                summary = if (usingWallpaper) stringResource(R.string.settings_accent_wallpaper) else null,
                icon = Icons.Filled.Palette,
                shape = LumenShape.Sunny,
                tone = IconTone.Secondary,
            ) {
                AccentPicker(
                    selected = preferences.accent,
                    enabled = !usingWallpaper,
                    onSelect = { onIntent(SettingsIntent.AccentSelected(it)) },
                )
            }
        },
        SettingsItem.Custom(key = "style") {
            SettingsControl(
                title = stringResource(R.string.settings_color_style),
                summary = stringResource(R.string.settings_color_style_summary),
                icon = Icons.Filled.Style,
                shape = LumenShape.Gem,
            ) {
                ColorStylePicker(
                    selected = preferences.colorStyle,
                    onSelect = { onIntent(SettingsIntent.ColorStyleSelected(it)) },
                )
            }
        },
        SettingsItem.Toggle(
            key = "black",
            title = stringResource(R.string.settings_pure_black),
            summary = stringResource(R.string.settings_pure_black_summary),
            icon = Icons.Filled.DarkMode,
            shape = LumenShape.Circle,
            tone = IconTone.Neutral,
            checked = preferences.pureBlack,
            enabled = preferences.themeMode != ThemeMode.Light,
            onCheckedChange = { onIntent(SettingsIntent.PureBlackSet(it)) },
        ),
    )
}

@Composable
private fun layoutItems(
    preferences: UserPreferences,
    onIntent: (SettingsIntent) -> Unit,
): List<SettingsItem> = listOf(
    SettingsItem.Custom(key = "bar") {
        SettingsControl(
            title = stringResource(R.string.settings_bar_position),
            icon = Icons.Filled.ViewAgenda,
            shape = LumenShape.Pill,
            tone = IconTone.Tertiary,
        ) {
            ConnectedChoiceGroup(
                options = SearchBarPosition.entries,
                selected = preferences.searchBarPosition,
                onSelect = { onIntent(SettingsIntent.SearchBarPositionSelected(it)) },
                label = { position -> stringResource(position.labelRes()) },
                icon = { position -> position.icon() },
            )
        }
    },
    SettingsItem.Custom(key = "font") {
        SettingsControl(
            title = stringResource(R.string.settings_font_size),
            summary = stringResource(
                R.string.settings_font_size_value,
                (preferences.fontScale * PERCENT).roundToInt(),
            ),
            icon = Icons.Filled.FormatSize,
            shape = LumenShape.Puffy,
            tone = IconTone.Secondary,
        ) {
            FontScaleSlider(
                value = preferences.fontScale,
                onValueChange = { onIntent(SettingsIntent.FontScaleSet(it)) },
            )
        }
    },
)

@Composable
private fun FontScaleSlider(value: Float, onValueChange: (Float) -> Unit) {
    val sliderState = rememberSliderState(
        value = value,
        steps = FONT_SCALE_STEPS,
        trackRange = UserPreferences.MIN_FONT_SCALE..UserPreferences.MAX_FONT_SCALE,
    )
    LaunchedEffect(value) {
        if (sliderState.value != value) sliderState.value = value
    }
    Slider(
        state = sliderState,
        onValueChange = { scale ->
            sliderState.value = scale
            onValueChange(scale)
        },
    )
}

private const val FONT_SCALE_STEPS = 8
private const val PERCENT = 100

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.System -> R.string.settings_theme_system
    ThemeMode.Light -> R.string.settings_theme_light
    ThemeMode.Dark -> R.string.settings_theme_dark
}

private fun SearchBarPosition.labelRes(): Int = when (this) {
    SearchBarPosition.Top -> R.string.settings_bar_position_top
    SearchBarPosition.Bottom -> R.string.settings_bar_position_bottom
}

private fun SearchBarPosition.icon() = when (this) {
    SearchBarPosition.Top -> Icons.Filled.VerticalAlignTop
    SearchBarPosition.Bottom -> Icons.Filled.VerticalAlignBottom
}
