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

/** What the user did on the search surface. */
sealed interface SearchIntent : MviIntent {
    data class QueryChanged(val raw: String) : SearchIntent

    /** Tap on a row, or the IME action key on the top-ranked result. */
    data class ResultActivated(val id: ResultId) : SearchIntent

    data class ActionInvoked(val id: ResultId, val actionId: String) : SearchIntent
    data class ResultLongPressed(val id: ResultId) : SearchIntent
    data class PermissionRequested(val providerId: ProviderId) : SearchIntent
    data class PermissionPromptDismissed(val providerId: ProviderId) : SearchIntent
    data object SheetDismissed : SearchIntent
    data object Cleared : SearchIntent
}

/** What happened, in the reducer's vocabulary. */
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

/**
 * The search screen's complete rendering input.
 *
 * Note what is absent: any branch per provider. Provider-specific richness rides inside
 * each [SearchResult] (its trailing and expanded content), so adding the tenth provider
 * does not add a field here. That is what keeps this screen's ViewModel from becoming a
 * god object as the app grows.
 *
 * The query text buffer is *not* here either — see [SearchScreen].
 */
@Immutable
data class SearchState(
    /** An echo of the current query, used only for empty-state messaging. */
    val query: String = "",
    val sections: List<ResultSection> = emptyList(),
    val pendingProviders: Set<ProviderId> = emptySet(),
    val permissionRequests: List<PermissionRequest> = emptyList(),
    val sheet: ResultSheet? = null,
) : MviState {

    val hasResults: Boolean get() = sections.any { it.results.isNotEmpty() }
    val isSearching: Boolean get() = pendingProviders.isNotEmpty()
    val isQueryBlank: Boolean get() = query.isBlank()

    /** The result the IME action key should activate. */
    val topResult: SearchResult? get() = sections.firstOrNull()?.results?.firstOrNull()

    fun resultById(id: ResultId): SearchResult? =
        sections.firstNotNullOfOrNull { section -> section.results.firstOrNull { it.id == id } }
}

/** The long-press action sheet for one result. */
@Immutable
data class ResultSheet(val result: SearchResult)

/** One-shot events. Never part of state. */
sealed interface SearchEffect : MviEffect {
    data class Launch(val intent: PlatformIntent) : SearchEffect
    data class RequestPermissions(val permissions: List<AppPermission>) : SearchEffect
    data class ShowMessage(val text: TextValue) : SearchEffect

    /** Programmatic query change — applied to the text field the UI owns. */
    data class SetQueryText(val text: String) : SearchEffect

    data class Navigate(val route: AppRouteKey) : SearchEffect

    /** Dismiss Lumen, e.g. after launching an app. */
    data object CloseSurface : SearchEffect
}
