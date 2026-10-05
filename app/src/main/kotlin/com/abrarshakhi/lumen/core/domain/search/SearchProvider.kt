package com.abrarshakhi.lumen.core.domain.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface SearchProvider {

    val id: ProviderId

    val metadata: ProviderMetadata

    fun search(query: SearchQuery): Flow<ProviderResults>

    suspend fun warmUp() {}
}

abstract class SnapshotSearchProvider : SearchProvider {

    protected abstract suspend fun runSearch(query: SearchQuery): List<SearchResult>

    final override fun search(query: SearchQuery): Flow<ProviderResults> = flow {
        emit(ProviderResults(providerId = id, results = runSearch(query)))
    }
}

data class ProviderResults(
    val providerId: ProviderId,
    val results: List<SearchResult>,
    val isPartial: Boolean = false,
    val failure: Throwable? = null,
) {
    companion object {
        fun empty(providerId: ProviderId) = ProviderResults(providerId, emptyList())

        fun failed(providerId: ProviderId, cause: Throwable) =
            ProviderResults(providerId, emptyList(), failure = cause)
    }
}
