package com.abrarshakhi.lumen.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * One launchable activity, with its match forms precomputed.
 *
 * `normalized_label` and `acronym` are derived at index time rather than per keystroke:
 * normalising a few hundred labels on every character typed is exactly the kind of work
 * that makes a search field feel sluggish.
 */
@Entity(tableName = "app_index", primaryKeys = ["package_name", "activity_name"])
data class AppIndexEntity(
    @ColumnInfo(name = "package_name") val packageName: String,
    @ColumnInfo(name = "activity_name") val activityName: String,
    val label: String,
    @ColumnInfo(name = "normalized_label") val normalizedLabel: String,
    val acronym: String,
    @ColumnInfo(name = "is_system") val isSystem: Boolean,
    @ColumnInfo(name = "indexed_at") val indexedAtMillis: Long,
)

@Dao
interface AppIndexDao {

    @Query("SELECT * FROM app_index ORDER BY label COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<AppIndexEntity>>

    @Query("SELECT * FROM app_index")
    suspend fun getAll(): List<AppIndexEntity>

    @Upsert
    suspend fun upsertAll(entries: List<AppIndexEntity>)

    @Query("DELETE FROM app_index WHERE package_name = :packageName")
    suspend fun deletePackage(packageName: String)

    /** Removes anything not seen in the latest full scan. */
    @Query("DELETE FROM app_index WHERE indexed_at < :staleBefore")
    suspend fun deleteStale(staleBefore: Long)

    @Query("SELECT COUNT(*) FROM app_index")
    suspend fun count(): Int
}
