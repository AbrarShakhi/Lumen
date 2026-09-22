package com.abrarshakhi.lumen.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * A document discovered by walking a folder the user granted through SAF.
 *
 * Indexed into Room rather than queried live, because SAF has no search: finding a file
 * means walking the tree, which is far too slow to do per keystroke.
 *
 * Only documents live here. Media already lives in MediaStore, and duplicating it would
 * mean two sources of truth that drift apart.
 */
@Entity(tableName = "file_index")
data class FileIndexEntity(
    @PrimaryKey
    @ColumnInfo(name = "document_uri") val documentUri: String,
    @ColumnInfo(name = "tree_uri") val treeUri: String,
    val name: String,
    @ColumnInfo(name = "normalized_name") val normalizedName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String?,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "modified_at") val modifiedAtMillis: Long,
    val folder: String?,
    @ColumnInfo(name = "indexed_at") val indexedAtMillis: Long,
)

@Dao
interface FileIndexDao {

    @Query(
        """
        SELECT * FROM file_index
        WHERE normalized_name LIKE '%' || :term || '%'
        ORDER BY modified_at DESC
        LIMIT :limit
        """,
    )
    suspend fun search(term: String, limit: Int): List<FileIndexEntity>

    @Upsert
    suspend fun upsertAll(entries: List<FileIndexEntity>)

    /** Removes entries from a tree that the latest walk did not see. */
    @Query("DELETE FROM file_index WHERE tree_uri = :treeUri AND indexed_at < :staleBefore")
    suspend fun deleteStale(treeUri: String, staleBefore: Long)

    /** Used when the user revokes a folder. */
    @Query("DELETE FROM file_index WHERE tree_uri = :treeUri")
    suspend fun deleteTree(treeUri: String)

    @Query("SELECT COUNT(*) FROM file_index")
    suspend fun count(): Int
}
