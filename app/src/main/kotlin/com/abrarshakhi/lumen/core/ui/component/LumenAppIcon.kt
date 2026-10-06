package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.abrarshakhi.lumen.R

@Composable
fun LumenAppIcon(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pixels = with(LocalDensity.current) { size.roundToPx() }
    val bitmap: ImageBitmap? = remember(context, pixels) {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
            ?.toBitmap(pixels, pixels)
            ?.asImageBitmap()
    }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier.size(size))
    } else {
        Box(modifier.size(size))
    }
}
