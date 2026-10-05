package com.abrarshakhi.lumen.feature.search

import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.domain.platform.ClipboardWriter
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.UsageRepository
import com.abrarshakhi.lumen.core.domain.search.ActionContext
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.InternalDestination
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.text.TextValue

/**
 * Runs a result's action and turns what it returns into a screen effect.
 *
 * The single `when` below is over [ActionOutcome] — a closed set of *outcomes* — not over
 * providers. That is the distinction that keeps this class from growing as providers are
 * added: a new provider invents new behaviour, but never a new kind of outcome.
 *
 * Usage is recorded here, in one place, so no provider has to remember to do it.
 */
class ResultActionInvoker(
    private val intentLauncher: IntentLauncher,
    private val clipboard: ClipboardWriter,
    private val usage: UsageRepository,
) {

    suspend fun invoke(
        result: SearchResult,
        action: ResultAction,
        query: SearchQuery,
    ): List<SearchEffect> {
        val outcome = runCatching { action.invoke(context(query)) }
            .getOrElse { ActionOutcome.Failed(it) }

        // Only successful, intentional activations count toward ranking — a failed launch
        // or a permission prompt should not teach the ranker that this is a favourite.
        if (outcome !is ActionOutcome.Failed && outcome !is ActionOutcome.RequestPermissions) {
            runCatching { usage.record(result.rankingKey) }
        }

        return outcome.toEffects()
    }

    private fun ActionOutcome.toEffects(): List<SearchEffect> = when (this) {
        ActionOutcome.Handled -> listOf(SearchEffect.CloseSurface)

        is ActionOutcome.Launch -> listOf(
            SearchEffect.Launch(intent),
            SearchEffect.CloseSurface,
        )

        is ActionOutcome.ReplaceQuery -> listOf(SearchEffect.SetQueryText(query))

        is ActionOutcome.Message -> listOf(SearchEffect.ShowMessage(text))

        is ActionOutcome.RequestPermissions -> listOf(SearchEffect.RequestPermissions(permissions))

        is ActionOutcome.Navigate -> listOf(SearchEffect.Navigate(destination.toRoute()))

        is ActionOutcome.Failed -> listOf(
            SearchEffect.ShowMessage(TextValue.Raw(cause.message ?: "Couldn't complete that")),
        )
    }

    private fun context(currentQuery: SearchQuery) = object : ActionContext {
        override val query: SearchQuery = currentQuery

        override suspend fun copyToClipboard(label: String, text: String) =
            clipboard.copy(label, text)

        override suspend fun launch(intent: PlatformIntent): Boolean =
            intentLauncher.launch(intent)
    }
}

/** Maps a domain-level destination onto a concrete route. */
private fun InternalDestination.toRoute(): AppRouteKey = when (this) {
    InternalDestination.Settings -> AppRouteKey.Settings
    InternalDestination.Providers -> AppRouteKey.Providers
    InternalDestination.Triggers -> AppRouteKey.Triggers
    InternalDestination.SearchEngines -> AppRouteKey.SearchEngines
    InternalDestination.AiSettings -> AppRouteKey.AiSettings
    InternalDestination.Notes -> AppRouteKey.Notes
    InternalDestination.FileFolders -> AppRouteKey.FileFolders
    is InternalDestination.NoteEditor -> AppRouteKey.NoteEditor(noteId)
}
