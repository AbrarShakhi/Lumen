package com.abrarshakhi.lumen.core.domain.search

interface SearchProviderRegistry {
    val providers: List<SearchProvider>

    fun byId(id: ProviderId): SearchProvider? = providers.firstOrNull { it.id == id }
}

class DefaultSearchProviderRegistry(
    unordered: List<SearchProvider>,
) : SearchProviderRegistry {
    override val providers: List<SearchProvider> = unordered.sortedBy { it.metadata.order }
}
