package com.abrarshakhi.lumen.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

internal fun lumenTypography(scale: Float): Typography {
    val base = Typography()
    fun TextStyle.scaled() = copy(
        fontSize = fontSize * scale,
        lineHeight = if (lineHeight.isSpecified) lineHeight * scale else lineHeight,
    )
    return base.copy(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        displaySmall = base.displaySmall.scaled(),
        headlineLarge = base.headlineLarge.scaled(),
        headlineMedium = base.headlineMedium.scaled(),
        headlineSmall = base.headlineSmall.scaled(),
        titleLarge = base.titleLarge.scaled(),
        titleMedium = base.titleMedium.scaled(),
        titleSmall = base.titleSmall.scaled(),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled(),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled(),
        displayLargeEmphasized = base.displayLargeEmphasized.scaled(),
        displayMediumEmphasized = base.displayMediumEmphasized.scaled(),
        displaySmallEmphasized = base.displaySmallEmphasized.scaled(),
        headlineLargeEmphasized = base.headlineLargeEmphasized.scaled(),
        headlineMediumEmphasized = base.headlineMediumEmphasized.scaled(),
        headlineSmallEmphasized = base.headlineSmallEmphasized.scaled(),
        titleLargeEmphasized = base.titleLargeEmphasized.scaled(),
        titleMediumEmphasized = base.titleMediumEmphasized.scaled(),
        titleSmallEmphasized = base.titleSmallEmphasized.scaled(),
        bodyLargeEmphasized = base.bodyLargeEmphasized.scaled(),
        bodyMediumEmphasized = base.bodyMediumEmphasized.scaled(),
        bodySmallEmphasized = base.bodySmallEmphasized.scaled(),
        labelLargeEmphasized = base.labelLargeEmphasized.scaled(),
        labelMediumEmphasized = base.labelMediumEmphasized.scaled(),
        labelSmallEmphasized = base.labelSmallEmphasized.scaled(),
    )
}

internal val SearchFieldTextStyle = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp,
)
