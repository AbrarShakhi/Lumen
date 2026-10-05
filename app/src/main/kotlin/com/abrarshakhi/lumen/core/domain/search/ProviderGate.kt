package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.CapabilityChecker
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.text.TextValue

/**
 * Decides which providers run for a given query.
 *
 * This is where every "can we actually do this here?" question is answered once, so no
 * provider ever has to check a permission, a capability or its own enabled state. A
 * provider that is switched on but missing a permission is reported as a
 * [PermissionRequest] rather than silently producing nothing.
 */
interface ProviderGate {
    fun evaluate(providers: List<SearchProvider>, query: SearchQuery): GateDecision
}

data class GateDecision(
    val dispatch: List<SearchProvider>,
    val permissionRequests: List<PermissionRequest>,
)

/**
 * A provider that would have run, but needs permissions the user has not granted.
 *
 * Surfaced in the result list so the prompt appears exactly where the missing results
 * would have been, instead of in a separate settings trip.
 */
data class PermissionRequest(
    val providerId: ProviderId,
    val providerName: TextValue,
    val permissions: List<AppPermission>,
)

/** Reports which providers the user has switched off, and which prompts they dismissed. */
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

            // Providers that are expensive or surprising only run when asked for by name.
            if (metadata.requiresExplicitTrigger && query.trigger == null) continue

            // A missing required capability cannot be resolved with a permission dialog,
            // so the provider is simply absent rather than prompting for something the
            // user cannot grant from here.
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
        /** Don't prompt on the first keystroke — let the user type something real first. */
        const val PROMPT_MIN_QUERY_LENGTH = 2
    }
}
