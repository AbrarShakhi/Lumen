package com.abrarshakhi.lumen.surface

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.platform.HomeScreenSurfaces
import com.abrarshakhi.lumen.core.domain.platform.HomeWidget
import com.abrarshakhi.lumen.core.domain.platform.TileRequestResult
import com.abrarshakhi.lumen.surface.tile.LumenTileService
import com.abrarshakhi.lumen.surface.widget.LumenNotesWidgetReceiver
import com.abrarshakhi.lumen.surface.widget.LumenSearchWidgetReceiver
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidHomeScreenSurfaces(
    private val context: Context,
) : HomeScreenSurfaces {

    private val widgetManager: AppWidgetManager? = context.getSystemService()

    override val canPinWidgets: Boolean
        get() = widgetManager?.isRequestPinAppWidgetSupported == true

    override val canRequestTile: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun pinWidget(widget: HomeWidget): Boolean {
        val manager = widgetManager ?: return false
        if (!manager.isRequestPinAppWidgetSupported) return false
        val provider = when (widget) {
            HomeWidget.Search -> ComponentName(context, LumenSearchWidgetReceiver::class.java)
            HomeWidget.Notes -> ComponentName(context, LumenNotesWidgetReceiver::class.java)
        }
        return runCatching { manager.requestPinAppWidget(provider, null, null) }.getOrDefault(false)
    }

    override suspend fun requestTile(): TileRequestResult =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestTileApi33()
        } else {
            TileRequestResult.Unsupported
        }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun requestTileApi33(): TileRequestResult {
        val statusBar = context.getSystemService<StatusBarManager>()
            ?: return TileRequestResult.Unsupported
        return suspendCancellableCoroutine { continuation ->
            runCatching {
                statusBar.requestAddTileService(
                    ComponentName(context, LumenTileService::class.java),
                    context.getString(R.string.tile_label),
                    Icon.createWithResource(context, R.drawable.ic_widget_search),
                    context.mainExecutor,
                ) { code ->
                    if (continuation.isActive) continuation.resume(code.toTileResult())
                }
            }.onFailure {
                if (continuation.isActive) continuation.resume(TileRequestResult.Unsupported)
            }
        }
    }

    private fun Int.toTileResult(): TileRequestResult = when (this) {
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> TileRequestResult.Added
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> TileRequestResult.AlreadyAdded
        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> TileRequestResult.NotAdded
        else -> TileRequestResult.Unsupported
    }
}
