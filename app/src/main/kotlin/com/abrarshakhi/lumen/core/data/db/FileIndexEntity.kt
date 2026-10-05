package com.abrarshakhi.lumen.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

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

    @Query("DELETE FROM file_index WHERE tree_uri = :treeUri AND indexed_at < :staleBefore")
    suspend fun deleteStale(treeUri: String, staleBefore: Long)

    @Query("DELETE FROM file_index WHERE tree_uri = :treeUri")
    suspend fun deleteTree(treeUri: String)

    @Query("SELECT COUNT(*) FROM file_index")
    suspend fun count(): Int
}
