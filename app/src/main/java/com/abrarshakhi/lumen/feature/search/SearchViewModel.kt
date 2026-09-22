package com.abrarshakhi.lumen.feature.search

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.SearchEngine
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResults
import com.abrarshakhi.lumen.core.domain.search.TriggerResolver
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Drives the search surface.
 *
 * Depends on exactly three things and has never heard of any individual provider. Adding
 * contacts, files or AI answers does not change this class — that is the whole point of
 * routing everything through [SearchEngine].
 */
class SearchViewModel(
    private val engine: SearchEngine,
    private val invoker: ResultActionInvoker,
    private val triggers: TriggerResolver,
    private val preferences: UserPreferencesRepository,
) : MviViewModel<SearchIntent, SearchAction, SearchState, SearchEffect>(
    initialState = SearchState(),
    reducer = SearchReducer,
) {

    /** The query stream the engine observes. Fed by the UI's text field. */
    private val queries = MutableStateFlow("")

    init {
        engine.observe(queries)
            .onEach { results -> reduce(results.toAction()) }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> {
                queries.value = intent.raw
                reduce(SearchAction.QueryEchoed(intent.raw))
            }

            is SearchIntent.ResultActivated -> {
                val result = currentState.resultById(intent.id) ?: return
                runAction(result.id, result.actions.primary.id)
            }

            is SearchIntent.ActionInvoked -> runAction(intent.id, intent.actionId)

            is SearchIntent.ResultLongPressed -> {
                val result = currentState.resultById(intent.id) ?: return
                reduce(SearchAction.SheetOpened(result))
            }

            is SearchIntent.PermissionRequested -> {
                val request = currentState.permissionRequests
                    .firstOrNull { it.providerId == intent.providerId } ?: return
                emitEffect(SearchEffect.RequestPermissions(request.permissions))
            }

            is SearchIntent.PermissionPromptDismissed -> dismissPrompt(intent.providerId)

            SearchIntent.SheetDismissed -> reduce(SearchAction.SheetClosed)

            SearchIntent.Cleared -> {
                queries.value = ""
                reduce(SearchAction.Reset)
                emitEffect(SearchEffect.SetQueryText(""))
            }
        }
    }

    private suspend fun runAction(resultId: com.abrarshakhi.lumen.core.domain.search.ResultId, actionId: String) {
        val result = currentState.resultById(resultId) ?: return
        val action = result.actions.byId(actionId) ?: return
        reduce(SearchAction.SheetClosed)

        val query: SearchQuery = triggers.parse(currentState.query)
        invoker.invoke(result, action, query).forEach { emitEffect(it) }
    }

    private fun dismissPrompt(providerId: ProviderId) {
        viewModelScope.launch {
            preferences.update { current ->
                current.copy(
                    dismissedPermissionPrompts = current.dismissedPermissionPrompts + providerId.value,
                )
            }
        }
    }

    private fun SearchResults.toAction() = SearchAction.ResultsUpdated(
        query = query.raw,
        sections = sections,
        pendingProviders = pendingProviders,
        permissionRequests = permissionRequests,
    )
}
