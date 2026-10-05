package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.CapabilityChecker
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.text.TextValue

interface ProviderGate {
    fun evaluate(providers: List<SearchProvider>, query: SearchQuery): GateDecision
}

data class GateDecision(
    val dispatch: List<SearchProvider>,
    val permissionRequests: List<PermissionRequest>,
)

data class PermissionRequest(
    val providerId: ProviderId,
    val providerName: TextValue,
    val permissions: List<AppPermission>,
)

interface ProviderPreferences {
    fun isEnabled(providerId: ProviderId, defaultEnabled: Boolean): Boolean
    fun isPermissionPromptDismissed(providerId: ProviderId): Boolean
}

class DefaultProviderGate(
    private val permissions: PermissionChecker,
    private val capabilities: CapabilityChecker,
    private val preferences: ProviderPreferences,
) : ProviderGate {

    override fun evaluate(providers: List<SearchProvider>, query: SearchQuery): GateDecision {
        if (query.isBlank) return GateDecision(emptyList(), emptyList())

        val dispatch = mutableListOf<SearchProvider>()
        val requests = mutableListOf<PermissionRequest>()

        for (provider in providers) {
            val metadata = provider.metadata
            if (!preferences.isEnabled(provider.id, metadata.defaultEnabled)) continue
            if (query.length < metadata.minQueryLength) continue

            if (metadata.requiresExplicitTrigger && query.trigger == null) continue

            if (metadata.requiredCapabilities.any { !capabilities.has(it) }) continue

            val missing = AppPermission
                .applicable(metadata.requiredPermissions, permissions.sdkInt)
                .filterNot { permissions.isGranted(it) }

            if (missing.isEmpty()) {
                dispatch += provider
            } else if (
                !preferences.isPermissionPromptDismissed(provider.id) &&
                query.length >= PROMPT_MIN_QUERY_LENGTH &&
                missing.none { permissions.isPermanentlyDenied(it) }
            ) {
                requests += PermissionRequest(provider.id, metadata.displayName, missing)
            }
        }

        return GateDecision(dispatch, requests)
    }

    private companion object {
        const val PROMPT_MIN_QUERY_LENGTH = 2
    }
}
