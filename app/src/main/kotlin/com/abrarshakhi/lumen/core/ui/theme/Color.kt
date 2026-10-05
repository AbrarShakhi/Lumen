package com.abrarshakhi.lumen.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Fallback palette, used when dynamic colour is unavailable or switched off.
 *
 * Neutral and low-chroma on purpose: Lumen is a surface you look *through* on the way to
 * something else, so the palette should stay out of the way of app icons and contact photos.
 */
internal val LumenDarkColors = LumenPalette(
    primary = Color(0xFFB9C6FF),
    onPrimary = Color(0xFF16275C),
    primaryContainer = Color(0xFF2E3E74),
    onPrimaryContainer = Color(0xFFDCE1FF),
    secondary = Color(0xFFC3C5DD),
    onSecondary = Color(0xFF2C2F42),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44464F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8F9099),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

internal val LumenLightColors = LumenPalette(
    primary = Color(0xFF45568D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE1FF),
    onPrimaryContainer = Color(0xFF001551),
    secondary = Color(0xFF5B5D72),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1A1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE2E1EC),
    onSurfaceVariant = Color(0xFF44464F),
    outline = Color(0xFF767680),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
)

/** The subset of Material colour roles Lumen actually overrides. */
internal data class LumenPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val error: Color,
    val onError: Color,
)
