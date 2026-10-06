package com.abrarshakhi.lumen.core.ui.theme

import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.ktx.animateColorScheme
import com.materialkolor.rememberDynamicColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LumenTheme(
    preferences: UserPreferences = UserPreferences.Default,
    content: @Composable () -> Unit,
) {
    val darkTheme = preferences.themeMode.resolveDark()
    val context = LocalContext.current
    val seed = remember(preferences.dynamicColor, preferences.accent, context) {
        context.wallpaperSeed().takeIf { preferences.dynamicColor } ?: preferences.accent.seed
    }

    val colorScheme = rememberDynamicColorScheme(
        seedColor = seed,
        isDark = darkTheme,
        isAmoled = darkTheme && preferences.pureBlack,
        style = preferences.colorStyle.toPaletteStyle(),
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )

    SystemBarsStyle(darkTheme = darkTheme)

    MaterialExpressiveTheme(
        colorScheme = animateColorScheme(colorScheme),
        motionScheme = MotionScheme.expressive(),
        typography = remember(preferences.fontScale) { lumenTypography(preferences.fontScale) },
        content = content,
    )
}

@Composable
private fun SystemBarsStyle(darkTheme: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    DisposableEffect(activity, darkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = AndroidColor.TRANSPARENT,
                darkScrim = AndroidColor.TRANSPARENT,
                detectDarkMode = { darkTheme },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = LIGHT_NAVIGATION_SCRIM,
                darkScrim = DARK_NAVIGATION_SCRIM,
                detectDarkMode = { darkTheme },
            ),
        )
        onDispose {}
    }
}

private val LIGHT_NAVIGATION_SCRIM = AndroidColor.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DARK_NAVIGATION_SCRIM = AndroidColor.argb(0x80, 0x1B, 0x1B, 0x1B)

@Composable
private fun ThemeMode.resolveDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

fun Context.supportsWallpaperColors(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private fun Context.wallpaperSeed(): Color? =
    if (supportsWallpaperColors()) Color(getColor(android.R.color.system_accent1_500)) else null
