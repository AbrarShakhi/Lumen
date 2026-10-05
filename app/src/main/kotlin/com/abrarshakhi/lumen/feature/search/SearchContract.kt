package com.abrarshakhi.lumen.feature.search

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.search.PermissionRequest
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState

sealed interface SearchIntent : MviIntent {
    data class QueryChanged(val raw: String) : SearchIntent

    data class ResultActivated(val id: ResultId) : SearchIntent

    data class ActionInvoked(val id: ResultId, val actionId: String) : SearchIntent
    data class ResultLongPressed(val id: ResultId) : SearchIntent
    data class PermissionRequested(val providerId: ProviderId) : SearchIntent
    data class PermissionPromptDismissed(val providerId: ProviderId) : SearchIntent
    data object SheetDismissed : SearchIntent
    data object Cleared : SearchIntent
}

sealed interface SearchAction : MviAction {
    data class ResultsUpdated(
        val query: String,
        val sections: List<ResultSection>,
        val pendingProviders: Set<ProviderId>,
        val permissionRequests: List<PermissionRequest>,
    ) : SearchAction

    data class QueryEchoed(val raw: String) : SearchAction
    data class SheetOpened(val result: SearchResult) : SearchAction
    data object SheetClosed : SearchAction
    data object Reset : SearchAction
}

@Immutable
data class SearchState(
    val query: String = "",
    val sections: List<ResultSection> = emptyList(),
    val pendingProviders: Set<ProviderId> = emptySet(),
    val permissionRequests: List<PermissionRequest> = emptyList(),
    val sheet: ResultSheet? = null,
) : MviState {

    val hasResults: Boolean get() = sections.any { it.results.isNotEmpty() }
    val isSearching: Boolean get() = pendingProviders.isNotEmpty()
    val isQueryBlank: Boolean get() = query.isBlank()

    val topResult: SearchResult? get() = sections.firstOrNull()?.results?.firstOrNull()

    fun resultById(id: ResultId): SearchResult? =
        sections.firstNotNullOfOrNull { section -> section.results.firstOrNull { it.id == id } }
}

@Immutable
data class ResultSheet(val result: SearchResult)

sealed interface SearchEffect : MviEffect {
    data class Launch(val intent: PlatformIntent) : SearchEffect
    data class RequestPermissions(val permissions: List<AppPermission>) : SearchEffect
    data class ShowMessage(val text: TextValue) : SearchEffect

    data class SetQueryText(val text: String) : SearchEffect

    data class Navigate(val route: AppRouteKey) : SearchEffect

    data object CloseSurface : SearchEffect
}
