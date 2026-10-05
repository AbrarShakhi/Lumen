package com.abrarshakhi.lumen.feature.settings.providers

import com.abrarshakhi.lumen.core.mvi.Reducer

object ProvidersReducer : Reducer<ProvidersState, ProvidersAction> {
    override fun reduce(state: ProvidersState, action: ProvidersAction): ProvidersState =
        when (action) {
            is ProvidersAction.ProvidersLoaded -> state.copy(providers = action.providers)
        }
}
