package com.abrarshakhi.lumen.core.domain.rank

import kotlin.math.exp
import kotlin.math.ln

data class UsageStat(
    val launchCount: Int,
    val lastLaunchedAtMillis: Long,
)

class UsageSnapshot(
    private val stats: Map<String, UsageStat>,
    private val nowMillis: Long,
) {

    fun scoreFor(rankingKey: String): Float {
        val stat = stats[rankingKey] ?: return 0f
        if (stat.launchCount <= 0) return 0f

        val frequency = (ln(stat.launchCount.toDouble() + 1.0) / ln(FREQUENCY_SATURATION))
            .coerceIn(0.0, 1.0)

        val ageMillis = (nowMillis - stat.lastLaunchedAtMillis).coerceAtLeast(0L)
        val recency = exp(-LN2 * ageMillis.toDouble() / HALF_LIFE_MILLIS)

        return (frequency * FREQUENCY_SHARE + recency * RECENCY_SHARE).toFloat().coerceIn(0f, 1f)
    }

    fun isEmpty(): Boolean = stats.isEmpty()

    companion object {
        val Empty = UsageSnapshot(emptyMap(), 0L)

        private const val LN2 = 0.6931471805599453
        private const val HALF_LIFE_MILLIS = 14.0 * 24 * 60 * 60 * 1000
        private const val FREQUENCY_SATURATION = 26.0
        private const val FREQUENCY_SHARE = 0.6
        private const val RECENCY_SHARE = 0.4
    }
}
