package com.abrarshakhi.lumen.core.ui.component

import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.abrarshakhi.lumen.core.domain.search.IconSource

/** Standard size for a result row's leading icon. */
val ResultIconSize: Dp = 42.dp

/**
 * Renders any [IconSource].
 *
 * A single renderer for a closed set of icon shapes, so adding a provider never means
 * touching icon rendering — the provider just names a shape.
 */
@Composable
fun ResultIcon(
    source: IconSource,
    modifier: Modifier = Modifier,
    size: Dp = ResultIconSize,
) {
    when (source) {
        is IconSource.App -> AppIcon(source, modifier, size)

        is IconSource.Vector -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = source.icon.toImageVector(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size * 0.55f),
            )
        }

        is IconSource.Letter -> MonogramIcon(
            text = source.text,
            seedColorArgb = source.seedColorArgb,
            modifier = modifier,
            size = size,
        )

        is IconSource.ContactPhoto -> MonogramIcon(
            text = source.fallbackInitial.toString(),
            seedColorArgb = null,
            modifier = modifier,
            size = size,
        )

        is IconSource.Emoji -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(source.character, style = MaterialTheme.typography.titleLarge)
        }

        is IconSource.Remote -> MonogramIcon(
            text = "?",
            seedColorArgb = null,
            modifier = modifier,
            size = size,
        )
    }
}

@Composable
private fun AppIcon(source: IconSource.App, modifier: Modifier, size: Dp) {
    val context = LocalContext.current
    val key = "${source.packageName}/${source.activityName.orEmpty()}"
    var bitmap by remember(key) { mutableStateOf(AppIconCache.cached(key)) }

    LaunchedEffect(key) {
        if (bitmap == null) {
            bitmap = withContext(Dispatchers.IO) {
                AppIconCache.load(context.packageManager, key, source)
            }
        }
    }

    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = modifier.size(size).clip(RoundedCornerShape(size * 0.22f)),
        )
    } else {
        // Reserve the slot so rows never reflow when the icon resolves.
        Box(modifier.size(size))
    }
}

@Composable
private fun MonogramIcon(text: String, seedColorArgb: Int?, modifier: Modifier, size: Dp) {
    val fallback = MaterialTheme.colorScheme.secondaryContainer
    val background = remember(seedColorArgb, fallback) {
        seedColorArgb?.let { Color(it) } ?: fallback
    }
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

/**
 * An in-memory cache of rasterised app icons.
 *
 * Deliberately hand-rolled rather than pulling in an image-loading library: the source is
 * a local `Drawable`, not a network image, so there is no fetching, no disk cache and no
 * placeholder pipeline to justify the dependency.
 */
private object AppIconCache {

    private const val ICON_PIXELS = 144
    private val cache = LruCache<String, ImageBitmap>(96)

    fun cached(key: String): ImageBitmap? = cache[key]

    fun load(
        packageManager: PackageManager,
        key: String,
        source: IconSource.App,
    ): ImageBitmap? {
        cache[key]?.let { return it }

        val drawable = runCatching {
            if (source.activityName != null) {
                packageManager.getActivityIcon(
                    ComponentName(source.packageName, source.activityName),
                )
            } else {
                packageManager.getApplicationIcon(source.packageName)
            }
        }.getOrNull() ?: return null

        val image = drawable.toImageBitmap() ?: return null
        cache.put(key, image)
        return image
    }

    private fun Drawable.toImageBitmap(): ImageBitmap? = runCatching {
        if (this is BitmapDrawable && bitmap != null) {
            return@runCatching bitmap.asImageBitmap()
        }
        val width = intrinsicWidth.takeIf { it > 0 } ?: ICON_PIXELS
        val height = intrinsicHeight.takeIf { it > 0 } ?: ICON_PIXELS
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        output.asImageBitmap()
    }.getOrNull()
}
