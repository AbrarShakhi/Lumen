package com.abrarshakhi.lumen.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences

/**
 * Lumen's Material 3 theme, driven entirely by user preferences.
 *
 * Takes [UserPreferences] as a parameter rather than reading a repository itself, so it
 * stays previewable and testable without DI.
 */
@Composable
fun LumenTheme(
    preferences: UserPreferences = UserPreferences.Default,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (preferences.themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val context = LocalContext.current
    val colorScheme: ColorScheme = remember(darkTheme, preferences.dynamicColor, context) {
        val canUseDynamic = preferences.dynamicColor &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        when {
            canUseDynamic && darkTheme -> dynamicDarkColorScheme(context)
            canUseDynamic -> dynamicLightColorScheme(context)
            darkTheme -> LumenDarkColors.toColorScheme(dark = true)
            else -> LumenLightColors.toColorScheme(dark = false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = remember(preferences.fontScale) { lumenTypography(preferences.fontScale) },
        content = content,
    )
}

private fun LumenPalette.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        error = error,
        onError = onError,
    )
}
