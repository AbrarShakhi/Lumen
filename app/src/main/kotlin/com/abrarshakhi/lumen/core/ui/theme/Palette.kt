package com.abrarshakhi.lumen.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.abrarshakhi.lumen.core.domain.preferences.ColorStyle
import com.abrarshakhi.lumen.core.domain.preferences.ThemeAccent
import com.materialkolor.PaletteStyle

val ThemeAccent.seed: Color
    get() = when (this) {
        ThemeAccent.Ember -> Color(0xFFC8472E)
        ThemeAccent.Amber -> Color(0xFFE0A100)
        ThemeAccent.Moss -> Color(0xFF4F7A3A)
        ThemeAccent.Ocean -> Color(0xFF1F6FB2)
        ThemeAccent.Iris -> Color(0xFF6750A4)
        ThemeAccent.Blossom -> Color(0xFFC2457A)
    }

internal fun ColorStyle.toPaletteStyle(): PaletteStyle = when (this) {
    ColorStyle.TonalSpot -> PaletteStyle.TonalSpot
    ColorStyle.Vibrant -> PaletteStyle.Vibrant
    ColorStyle.Expressive -> PaletteStyle.Expressive
    ColorStyle.Neutral -> PaletteStyle.Neutral
    ColorStyle.Fidelity -> PaletteStyle.Fidelity
    ColorStyle.Monochrome -> PaletteStyle.Monochrome
}
