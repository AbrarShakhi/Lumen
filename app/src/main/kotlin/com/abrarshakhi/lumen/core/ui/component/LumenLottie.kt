package com.abrarshakhi.lumen.core.ui.component

import androidx.annotation.RawRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.abrarshakhi.lumen.R
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

enum class LumenAnimation(@param:RawRes val resource: Int) {
    Glow(R.raw.lumen_glow),
    Dots(R.raw.lumen_dots),
}

@Composable
fun LumenLottie(
    animation: LumenAnimation,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animation.resource))
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(LottieProperty.COLOR, color.toArgb(), "**"),
    )
    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = LottieConstants.IterateForever,
        dynamicProperties = dynamicProperties,
    )
}
