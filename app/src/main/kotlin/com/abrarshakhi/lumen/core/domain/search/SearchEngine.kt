package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.rank.ResultRanker
import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.rank.UsageSnapshot
import com.abrarshakhi.lumen.core.domain.util.takeUntilTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlin.time.Duration

/**
 * Merges every eligible provider's results for the current query.
 *
 * The two properties that make search feel instant rather than form-like:
 *
 *  - **`flatMapLatest`** means a new keystroke structurally cancels every in-flight
 *    provider. Providers get cancellation for free and never need to check for staleness.
 *  - **`scan`** means results render as they arrive. Apps land in a few milliseconds and
 *    paint immediately; a network-backed answer joins the same list a second later. The
 *    user never waits on the slowest provider to see the fastest one's results.
 *
 * This class must not need modification when a provider is added — if it ever does, the
 * [SearchProvider] abstraction is wrong.
 */
class SearchEngine(
    private val registry: SearchProviderRegistry,
    private val ranker: ResultRanker,
    private val triggers: TriggerResolver,
    private val gate: ProviderGate,
    private val usage: StateFlow<UsageSnapshot>,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(rawQueries: Flow<String>): Flow<SearchResults> =
        rawQueries
            .map(String::trim)
            .distinctUntilChanged()
            .flatMapLatest { raw ->
                val query = triggers.parse(raw)
                if (query.isBlank) return@flatMapLatest flowOf(SearchResults.idle(query))

                val decision = gate.evaluate(registry.providers, query)
                if (decision.dispatch.isEmpty()) {
                    return@flatMapLatest flowOf(
                        SearchResults.idle(query).copy(permissionRequests = decision.permissionRequests),
                    )
                }

                val streams = decision.dispatch.map { provider -> provider.pipeline(query) }
                merge(*streams.toTypedArray())
                    .scan(Accumulator.pending(query, decision)) { accumulator, event ->
                        accumulator.with(event)
                    }
                    .map { it.render(ranker, usage.value) }
            }
            .distinctUntilChanged()

    /**
     * Wraps one provider with the policy it declared: debounce, timeout, failure isolation.
     *
     * Debounce is a plain `delay` inside the child coroutine rather than `Flow.debounce`.
     * A global debounce before `flatMapLatest` would make local providers feel laggy just
     * to protect a network one, and `Flow.debounce` applied afterwards does nothing here
     * because a single-query flow completes immediately, flushing the pending value. A
     * `delay` in the child is cancelled by the next keystroke, which is exactly the
     * per-provider behaviour wanted.
     */
    private fun SearchProvider.pipeline(query: SearchQuery): Flow<ProviderEvent> = flow {
        if (metadata.debounce > Duration.ZERO) delay(metadata.debounce)
        emitAll(
            search(query)
                .takeUntilTimeout(metadata.timeout)
                // One failing provider degrades its own section; it never fails the search.
                .catch { cause ->
                    if (cause is CancellationException) throw cause
                    emit(ProviderResults.failed(id, cause))
                }
                .map<ProviderResults, ProviderEvent> { ProviderEvent.Emitted(it) },
        )
    }
        // Completion is tracked explicitly rather than inferred from the last emission.
        // A provider killed by its timeout completes without ever emitting, and inferring
        // "done" from emissions alone would leave it pending forever — a spinner that
        // never stops.
        .onCompletion { cause -> if (cause == null) emit(ProviderEvent.Finished(id)) }

    /** Internal envelope so the accumulator can distinguish results from stream completion. */
    private sealed interface ProviderEvent {
        data class Emitted(val results: ProviderResults) : ProviderEvent
        data class Finished(val providerId: ProviderId) : ProviderEvent
    }

    /**
     * Accumulates emissions per provider for one query.
     *
     * Keyed by provider so a streaming provider's later emission replaces its earlier one
     * rather than appending duplicates.
     */
    private data class Accumulator(
        val query: SearchQuery,
        val byProvider: Map<ProviderId, ProviderResults>,
        val pending: Set<ProviderId>,
        val permissionRequests: List<PermissionRequest>,
    ) {
        fun with(event: ProviderEvent): Accumulator = when (event) {
            is ProviderEvent.Emitted -> copy(
                // Keyed by provider so a streaming provider's later emission replaces its
                // earlier one rather than appending duplicates.
                byProvider = byProvider + (event.results.providerId to event.results),
            )

            is ProviderEvent.Finished -> copy(pending = pending - event.providerId)
        }

        fun render(ranker: ResultRanker, usage: UsageSnapshot): SearchResults {
            val all = byProvider.values.flatMap { it.results }
            return SearchResults(
                query = query,
                sections = ranker.rank(query, all, usage),
                pendingProviders = pending,
                permissionRequests = permissionRequests,
                failures = byProvider.values
                    .mapNotNull { results -> results.failure?.let { results.providerId to it } }
                    .toMap(),
            )
        }

        companion object {
            fun pending(query: SearchQuery, decision: GateDecision) = Accumulator(
                query = query,
                byProvider = emptyMap(),
                pending = decision.dispatch.map { it.id }.toSet(),
                permissionRequests = decision.permissionRequests,
            )
        }
    }
}

/**
 * A rendered snapshot of the search, possibly still filling in.
 *
 * [pendingProviders] is derived by the engine — providers never report their own state —
 * and is what the UI uses to decide whether to show progress.
 */
data class SearchResults(
    val query: SearchQuery,
    val sections: List<ResultSection>,
    val pendingProviders: Set<ProviderId> = emptySet(),
    val permissionRequests: List<PermissionRequest> = emptyList(),
    val failures: Map<ProviderId, Throwable> = emptyMap(),
) {
    val isEmpty: Boolean get() = sections.isEmpty() && permissionRequests.isEmpty()
    val isSettled: Boolean get() = pendingProviders.isEmpty()

    /** Flattened in display order — used for "activate the first result" on the IME key. */
    val flatResults: List<SearchResult> get() = sections.flatMap { it.results }

    companion object {
        fun idle(query: SearchQuery) = SearchResults(query = query, sections = emptyList())
        val Empty = idle(SearchQuery.Empty)
    }
}
