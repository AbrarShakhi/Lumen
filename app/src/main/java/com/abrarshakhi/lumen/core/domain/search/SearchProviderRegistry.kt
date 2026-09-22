package com.abrarshakhi.lumen.core.domain.search

/**
 * The set of installed providers, in deterministic display order.
 *
 * An interface so that the search engine can be constructed in tests with a handful of
 * fakes instead of the real DI graph.
 */
interface SearchProviderRegistry {
    val providers: List<SearchProvider>

    fun byId(id: ProviderId): SearchProvider? = providers.firstOrNull { it.id == id }
}

/**
 * Orders providers by [ProviderMetadata.order].
 *
 * Sorting here, once, is what makes DI registration order irrelevant — a provider's
 * position in the result list is a property it declares, not an accident of wiring.
 */
class DefaultSearchProviderRegistry(
    unordered: List<SearchProvider>,
) : SearchProviderRegistry {
    override val providers: List<SearchProvider> = unordered.sortedBy { it.metadata.order }
}
