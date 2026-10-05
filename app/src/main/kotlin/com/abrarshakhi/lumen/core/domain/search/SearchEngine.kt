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

    private fun SearchProvider.pipeline(query: SearchQuery): Flow<ProviderEvent> = flow {
        if (metadata.debounce > Duration.ZERO) delay(metadata.debounce)
        emitAll(
            search(query)
                .takeUntilTimeout(metadata.timeout)
                .catch { cause ->
                    if (cause is CancellationException) throw cause
                    emit(ProviderResults.failed(id, cause))
                }
                .map<ProviderResults, ProviderEvent> { ProviderEvent.Emitted(it) },
        )
    }
        .onCompletion { cause -> if (cause == null) emit(ProviderEvent.Finished(id)) }

    private sealed interface ProviderEvent {
        data class Emitted(val results: ProviderResults) : ProviderEvent
        data class Finished(val providerId: ProviderId) : ProviderEvent
    }

    private data class Accumulator(
        val query: SearchQuery,
        val byProvider: Map<ProviderId, ProviderResults>,
        val pending: Set<ProviderId>,
        val permissionRequests: List<PermissionRequest>,
    ) {
        fun with(event: ProviderEvent): Accumulator = when (event) {
            is ProviderEvent.Emitted -> copy(
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

data class SearchResults(
    val query: SearchQuery,
    val sections: List<ResultSection>,
    val pendingProviders: Set<ProviderId> = emptySet(),
    val permissionRequests: List<PermissionRequest> = emptyList(),
    val failures: Map<ProviderId, Throwable> = emptyMap(),
) {
    val isEmpty: Boolean get() = sections.isEmpty() && permissionRequests.isEmpty()
    val isSettled: Boolean get() = pendingProviders.isEmpty()

    val flatResults: List<SearchResult> get() = sections.flatMap { it.results }

    companion object {
        fun idle(query: SearchQuery) = SearchResults(query = query, sections = emptyList())
        val Empty = idle(SearchQuery.Empty)
    }
}
