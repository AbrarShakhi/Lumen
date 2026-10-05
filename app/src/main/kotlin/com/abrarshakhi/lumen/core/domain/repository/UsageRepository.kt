package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.rank.UsageSnapshot
import kotlinx.coroutines.flow.StateFlow

interface UsageRepository {
    val snapshot: StateFlow<UsageSnapshot>

    suspend fun record(rankingKey: String)
}
