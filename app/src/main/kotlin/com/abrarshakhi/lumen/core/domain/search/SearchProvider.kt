package com.abrarshakhi.lumen.core.domain.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A source of search results.
 *
 * This is the extension point of the whole application. Every feature — contacts, files,
 * the calculator, AI answers — is a new implementation of this interface plus one line in
 * the DI composition root. [SearchEngine] must never need to change to accommodate one.
 *
 * A provider must not touch `Context`, request permissions, or know that any other
 * provider exists. It declares what it needs via [metadata] and the engine arranges it.
 */
interface SearchProvider {

    val id: ProviderId

    val metadata: ProviderMetadata

    /**
     * Results for [query], as a cold flow.
     *
     * A `Flow` rather than a `suspend fun` because two kinds of provider exist: snapshot
     * providers that compute one answer (see [SnapshotSearchProvider]), and streaming ones
     * that emit a placeholder and refine it — an AI answer arriving token by token, or a
     * currency conversion that shows a cached rate before the network confirms it.
     * Forcing the second kind through a single return value would mean a side channel.
     *
     * Implementations must be cancellation-cooperative: the engine cancels this flow on
     * the next keystroke.
     */
    fun search(query: SearchQuery): Flow<ProviderResults>

    /**
     * Optional index priming, called off the main thread when the app comes to the
     * foreground. Constructors must stay trivial — expensive setup belongs here.
     */
    suspend fun warmUp() {}
}

/**
 * Convenience base for providers that compute a single batch of results.
 *
 * Covers the common case without making every implementation hand-write a `flow { }`.
 */
abstract class SnapshotSearchProvider : SearchProvider {

    protected abstract suspend fun runSearch(query: SearchQuery): List<SearchResult>

    final override fun search(query: SearchQuery): Flow<ProviderResults> = flow {
        emit(ProviderResults(providerId = id, results = runSearch(query)))
    }
}

/**
 * One emission from a provider.
 *
 * Deliberately has no "loading" state. Whether a provider is still pending is derived by
 * the engine from what it has not yet emitted for the current query — providers report
 * results, never their own lifecycle.
 */
data class ProviderResults(
    val providerId: ProviderId,
    val results: List<SearchResult>,
    /** True while more emissions are expected for this query, e.g. a streaming answer. */
    val isPartial: Boolean = false,
    /** Set when the provider failed. The engine degrades rather than failing the search. */
    val failure: Throwable? = null,
) {
    companion object {
        fun empty(providerId: ProviderId) = ProviderResults(providerId, emptyList())

        fun failed(providerId: ProviderId, cause: Throwable) =
            ProviderResults(providerId, emptyList(), failure = cause)
    }
}
