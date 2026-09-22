package com.abrarshakhi.lumen.provider.apps

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.FuzzyMatcher
import com.abrarshakhi.lumen.core.domain.permission.PlatformCapability
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.AppIndexRepository
import com.abrarshakhi.lumen.core.domain.repository.AppShortcutRepository
import com.abrarshakhi.lumen.core.domain.repository.IndexedShortcut
import com.abrarshakhi.lumen.core.domain.repository.IndexedApp
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.SnapshotSearchProvider
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Searches installed apps.
 *
 * The reference implementation every later provider copies: it declares its metadata,
 * matches against an in-memory index, and returns results carrying their own actions. It
 * holds no `Context`, requests no permissions, and knows nothing about the search engine.
 *
 * Zero debounce on purpose — this is a purely local, in-memory match, so results should
 * appear on the same frame as the keystroke that produced them.
 */
class AppsSearchProvider(
    private val index: AppIndexRepository,
    private val shortcuts: AppShortcutRepository,
) : SnapshotSearchProvider() {

    override val id = ProviderId("apps")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_apps),
        category = ResultCategory.App,
        order = 10,
        // Shortcuts need the home role, but plain app search does not — so this is
        // optional: without it the provider still works, just without shortcut results.
        optionalCapabilities = listOf(PlatformCapability.DefaultLauncher),
        timeout = 250.milliseconds,
        debounce = kotlin.time.Duration.ZERO,
        minQueryLength = 1,
    )

    override suspend fun warmUp() {
        index.refresh()
        // Returns empty unless Lumen is the launcher; harmless either way.
        shortcuts.shortcuts()
    }

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val terms = query.normalizedTerms
        if (terms.isBlank()) return emptyList()

        val apps = index.apps.value
            .mapNotNull { app ->
                val match = FuzzyMatcher.score(terms, app.searchable) ?: return@mapNotNull null
                app.toResult(score = match.score, matches = match.ranges)
            }
            .sortedByDescending { it.score }
            .take(MAX_RESULTS)

        // Scored slightly below apps so "chrome" still surfaces the app above one of its
        // shortcuts; empty unless Lumen holds the home role.
        val shortcutResults = shortcuts.shortcuts()
            .mapNotNull { shortcut ->
                val match = FuzzyMatcher.score(terms, shortcut.searchable) ?: return@mapNotNull null
                shortcut.toResult(match.score * SHORTCUT_WEIGHT, match.ranges)
            }
            .sortedByDescending { it.score }
            .take(MAX_SHORTCUTS)

        return apps + shortcutResults
    }

    private fun IndexedShortcut.toResult(score: Float, matches: List<IntRange>) = SearchResult(
        id = ResultId("shortcut:$packageName/$id"),
        providerId = this@AppsSearchProvider.id,
        title = label,
        subtitle = appLabel,
        icon = IconSource.App(packageName),
        category = ResultCategory.Shortcut,
        score = score,
        titleMatches = matches,
        rankingKey = "shortcut:$packageName/$id",
        actions = ResultActions(
            primary = ResultAction(
                id = "open-shortcut",
                label = TextValue.Res(R.string.action_open),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Launch(PlatformIntent.AppShortcut(packageName, id)) },
            ),
        ),
    )

    private fun IndexedApp.toResult(score: Float, matches: List<IntRange>) = SearchResult(
        id = ResultId("app:$componentKey"),
        providerId = id,
        title = label,
        icon = IconSource.App(packageName, activityName),
        category = ResultCategory.App,
        score = score,
        titleMatches = matches,
        rankingKey = componentKey,
        actions = ResultActions(
            primary = ResultAction(
                id = ACTION_LAUNCH,
                label = TextValue.Res(R.string.action_open),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = {
                    ActionOutcome.Launch(PlatformIntent.Component(packageName, activityName))
                },
            ),
            secondary = listOf(
                ResultAction(
                    id = ACTION_APP_INFO,
                    label = TextValue.Res(R.string.action_app_info),
                    icon = IconSource.Vector(LumenIcon.Info),
                    kind = ActionKind.Configure,
                    invoke = { ActionOutcome.Launch(PlatformIntent.AppDetails(packageName)) },
                ),
                ResultAction(
                    id = ACTION_UNINSTALL,
                    label = TextValue.Res(R.string.action_uninstall),
                    icon = IconSource.Vector(LumenIcon.Uninstall),
                    kind = ActionKind.Delete,
                    requiresConfirmation = true,
                    invoke = { ActionOutcome.Launch(PlatformIntent.Uninstall(packageName)) },
                ),
            ),
        ),
    )

    private companion object {
        /** Trimmed again by the ranker's section cap; this just bounds the work. */
        const val MAX_RESULTS = 24
        const val MAX_SHORTCUTS = 6
        const val SHORTCUT_WEIGHT = 0.9f

        const val ACTION_LAUNCH = "launch"
        const val ACTION_APP_INFO = "app-info"
        const val ACTION_UNINSTALL = "uninstall"
    }
}
