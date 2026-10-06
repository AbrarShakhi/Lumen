package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

enum class LumenShape { Circle, Cookie, Clover, Sunny, Gem, Flower, Puffy, Arch, SoftBurst, Pill }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LumenShape.asShape(): Shape = when (this) {
    LumenShape.Circle -> CircleShape
    LumenShape.Cookie -> MaterialShapes.Cookie9Sided.toShape()
    LumenShape.Clover -> MaterialShapes.Clover4Leaf.toShape()
    LumenShape.Sunny -> MaterialShapes.Sunny.toShape()
    LumenShape.Gem -> MaterialShapes.Gem.toShape()
    LumenShape.Flower -> MaterialShapes.Flower.toShape()
    LumenShape.Puffy -> MaterialShapes.Puffy.toShape()
    LumenShape.Arch -> MaterialShapes.Arch.toShape()
    LumenShape.SoftBurst -> MaterialShapes.SoftBurst.toShape()
    LumenShape.Pill -> MaterialShapes.Pill.toShape()
}
