package com.abrarshakhi.lumen.core.data.repository

import com.abrarshakhi.lumen.core.data.db.UsageStatDao
import com.abrarshakhi.lumen.core.domain.rank.UsageSnapshot
import com.abrarshakhi.lumen.core.domain.rank.UsageStat
import com.abrarshakhi.lumen.core.domain.repository.UsageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Usage statistics backed by Room, projected into an in-memory snapshot.
 *
 * The projection is the point: the table is read once and kept hot as a [StateFlow], so
 * ranking stays a pure synchronous function with no I/O on the keystroke path.
 */
class RoomUsageRepository(
    private val dao: UsageStatDao,
    scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
) : UsageRepository {

    override val snapshot: StateFlow<UsageSnapshot> = dao.observeAll()
        .catch { emit(emptyList()) }
        .map { rows ->
            UsageSnapshot(
                stats = rows.associate { row ->
                    row.rankingKey to UsageStat(
                        launchCount = row.launchCount,
                        lastLaunchedAtMillis = row.lastLaunchedAtMillis,
                    )
                },
                nowMillis = now(),
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, UsageSnapshot.Empty)

    override suspend fun record(rankingKey: String) {
        dao.recordLaunch(rankingKey, now())
    }
}
