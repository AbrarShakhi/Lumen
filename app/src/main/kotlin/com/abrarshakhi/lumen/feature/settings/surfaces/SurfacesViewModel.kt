package com.abrarshakhi.lumen.feature.settings.surfaces

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.platform.HomeScreenSurfaces
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.platform.TileRequestResult
import com.abrarshakhi.lumen.core.mvi.MviViewModel

class SurfacesViewModel(
    private val surfaces: HomeScreenSurfaces,
    private val intentLauncher: IntentLauncher,
) : MviViewModel<SurfacesIntent, SurfacesAction, SurfacesState, SurfacesEffect>(
    initialState = SurfacesState(),
    reducer = SurfacesReducer,
) {

    init {
        dispatch(SurfacesIntent.Refreshed)
    }

    override suspend fun handleIntent(intent: SurfacesIntent) {
        when (intent) {
            SurfacesIntent.Refreshed -> reduce(
                SurfacesAction.CapabilitiesLoaded(
                    canPinWidgets = surfaces.canPinWidgets,
                    canRequestTile = surfaces.canRequestTile,
                ),
            )

            is SurfacesIntent.PinWidget -> {
                if (!surfaces.pinWidget(intent.widget)) {
                    emitEffect(SurfacesEffect.ShowMessage(R.string.surfaces_pin_unsupported))
                }
            }

            SurfacesIntent.AddTile -> emitEffect(
                SurfacesEffect.ShowMessage(surfaces.requestTile().messageRes()),
            )

            SurfacesIntent.OpenAssistantSettings -> {
                val opened = intentLauncher.launch(PlatformIntent.SystemSetting(VOICE_INPUT_SETTINGS))
                if (!opened) emitEffect(SurfacesEffect.ShowMessage(R.string.surfaces_assistant_unavailable))
            }
        }
    }

    private fun TileRequestResult.messageRes(): Int = when (this) {
        TileRequestResult.Added -> R.string.surfaces_tile_added
        TileRequestResult.AlreadyAdded -> R.string.surfaces_tile_already_added
        TileRequestResult.NotAdded -> R.string.surfaces_tile_not_added
        TileRequestResult.Unsupported -> R.string.surfaces_tile_manual
    }

    private companion object {
        const val VOICE_INPUT_SETTINGS = "android.settings.VOICE_INPUT_SETTINGS"
    }
}
