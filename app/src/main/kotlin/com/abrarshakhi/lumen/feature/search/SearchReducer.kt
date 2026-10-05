package com.abrarshakhi.lumen.feature.search

import com.abrarshakhi.lumen.core.mvi.Reducer

object SearchReducer : Reducer<SearchState, SearchAction> {

    override fun reduce(state: SearchState, action: SearchAction): SearchState =
        when (action) {
            is SearchAction.QueryEchoed -> state.copy(query = action.raw)

            is SearchAction.ResultsUpdated -> state.copy(
                query = action.query,
                sections = action.sections,
                pendingProviders = action.pendingProviders,
                permissionRequests = action.permissionRequests,
            )

            is SearchAction.SheetOpened -> state.copy(sheet = ResultSheet(action.result))

            SearchAction.SheetClosed -> state.copy(sheet = null)

            SearchAction.Reset -> SearchState()
        }
}
