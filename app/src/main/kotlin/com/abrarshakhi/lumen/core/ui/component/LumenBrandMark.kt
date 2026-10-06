package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R

@Composable
fun LumenBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 168.dp,
    animated: Boolean = true,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (animated) {
            LumenLottie(animation = LumenAnimation.Glow, modifier = Modifier.fillMaxSize())
        }
        val breath by rememberInfiniteTransition(label = "brand-breath").animateFloat(
            initialValue = BREATH_MIN,
            targetValue = BREATH_MAX,
            animationSpec = infiniteRepeatable(
                animation = tween(BREATH_MILLIS, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "brand-scale",
        )
        Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = if (animated) breath else 1f
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}

private const val BREATH_MIN = 0.97f
private const val BREATH_MAX = 1.05f
private const val BREATH_MILLIS = 1600
