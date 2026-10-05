package com.abrarshakhi.lumen.surface.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.abrarshakhi.lumen.surface.overlay.QuickSearchActivity

/**
 * Summons Lumen from the Quick Settings panel.
 *
 * A thin launcher into [QuickSearchActivity] — every surface funnels into the same panel,
 * which is why adding this one is a few dozen lines rather than a second search UI.
 */
class LumenTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    // The PendingIntent overload only exists from API 34, and minSdk is 30 — so on the
    // test device the deprecated Intent form is the only one that exists. Lint's check does
    // not account for the version guard below, hence the suppression rather than a fix.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()

        val intent = QuickSearchActivity.intent(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // From API 34 the Intent overload is deprecated and a PendingIntent is required.
            startActivityAndCollapse(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
