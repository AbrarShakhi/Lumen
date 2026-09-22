package com.abrarshakhi.lumen.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * How often and how recently one result has been activated.
 *
 * Keyed by the result's ranking key rather than its id, so a provider whose ids vary per
 * query still accumulates usage against a stable identity.
 */
@Entity(tableName = "usage_stats")
data class UsageStatEntity(
    @PrimaryKey
    @ColumnInfo(name = "ranking_key") val rankingKey: String,
    @ColumnInfo(name = "launch_count") val launchCount: Int,
    @ColumnInfo(name = "last_launched_at") val lastLaunchedAtMillis: Long,
)

@Dao
interface UsageStatDao {

    @Query("SELECT * FROM usage_stats")
    fun observeAll(): Flow<List<UsageStatEntity>>

    /**
     * Increments in a single statement.
     *
     * An upsert rather than read-modify-write: launches can arrive in quick succession and
     * a read-then-write would lose counts under concurrency.
     */
    @Query(
        """
        INSERT INTO usage_stats (ranking_key, launch_count, last_launched_at)
        VALUES (:rankingKey, 1, :timestamp)
        ON CONFLICT(ranking_key) DO UPDATE SET
            launch_count = launch_count + 1,
            last_launched_at = :timestamp
        """,
    )
    suspend fun recordLaunch(rankingKey: String, timestamp: Long)

    @Query("DELETE FROM usage_stats WHERE ranking_key = :rankingKey")
    suspend fun clear(rankingKey: String)

    @Query("DELETE FROM usage_stats")
    suspend fun clearAll()
}
