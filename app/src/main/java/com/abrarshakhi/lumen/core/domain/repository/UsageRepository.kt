package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.rank.UsageSnapshot
import kotlinx.coroutines.flow.StateFlow

/**
 * Records what the user actually opens, so ranking can learn.
 *
 * Exposed as an always-available [StateFlow] snapshot rather than a suspending query:
 * ranking runs on every keystroke and must never touch disk.
 */
interface UsageRepository {
    val snapshot: StateFlow<UsageSnapshot>

    /** Counts one activation of [rankingKey]. */
    suspend fun record(rankingKey: String)
}
