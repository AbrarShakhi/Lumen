package com.abrarshakhi.lumen.feature.settings.surfaces

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.core.domain.platform.HomeWidget
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState
import com.abrarshakhi.lumen.core.mvi.Reducer

sealed interface SurfacesIntent : MviIntent {
    data class PinWidget(val widget: HomeWidget) : SurfacesIntent
    data object AddTile : SurfacesIntent
    data object OpenAssistantSettings : SurfacesIntent
    data object Refreshed : SurfacesIntent
}

sealed interface SurfacesAction : MviAction {
    data class CapabilitiesLoaded(
        val canPinWidgets: Boolean,
        val canRequestTile: Boolean,
    ) : SurfacesAction
}

@Immutable
data class SurfacesState(
    val canPinWidgets: Boolean = false,
    val canRequestTile: Boolean = false,
) : MviState

sealed interface SurfacesEffect : MviEffect {
    data class ShowMessage(@param:StringRes val message: Int) : SurfacesEffect
}

object SurfacesReducer : Reducer<SurfacesState, SurfacesAction> {
    override fun reduce(state: SurfacesState, action: SurfacesAction): SurfacesState =
        when (action) {
            is SurfacesAction.CapabilitiesLoaded -> state.copy(
                canPinWidgets = action.canPinWidgets,
                canRequestTile = action.canRequestTile,
            )
        }
}
