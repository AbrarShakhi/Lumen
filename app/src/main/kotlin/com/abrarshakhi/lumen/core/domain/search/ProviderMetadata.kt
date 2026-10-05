package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PlatformCapability
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Everything the search engine needs to know about a provider without running it.
 *
 * Providers *declare* their constraints; [SearchEngine] *enforces* them. Timeouts in
 * particular are not left to provider authors — if each one had to remember its own
 * `withTimeout`, one eventually would not, and a single hung provider would stall
 * the whole search.
 */
data class ProviderMetadata(
    val displayName: TextValue,
    val category: ResultCategory,
    /**
     * Deterministic section ordering. Never rely on DI registration order, which is
     * incidental — this is the single source of truth for where a section appears.
     */
    val order: Int,
    /** Permissions without which this provider cannot produce results at all. */
    val requiredPermissions: List<AppPermission> = emptyList(),
    /** Capabilities without which this provider cannot run. */
    val requiredCapabilities: List<PlatformCapability> = emptyList(),
    /** Capabilities that enrich results but whose absence only degrades them. */
    val optionalCapabilities: List<PlatformCapability> = emptyList(),
    /** Total budget from dispatch to completion. Emissions already made are kept. */
    val timeout: Duration = 800.milliseconds,
    /**
     * Delay before dispatch, per provider.
     *
     * Zero for local providers so app results paint on the same frame as the keystroke;
     * non-zero for network-backed ones so typing does not spray requests. Applying this
     * globally instead would make the whole app feel laggy to protect one slow provider.
     */
    val debounce: Duration = Duration.ZERO,
    val minQueryLength: Int = 1,
    /** When true, only runs if the user typed this provider's trigger explicitly. */
    val requiresExplicitTrigger: Boolean = false,
    val defaultEnabled: Boolean = true,
)
