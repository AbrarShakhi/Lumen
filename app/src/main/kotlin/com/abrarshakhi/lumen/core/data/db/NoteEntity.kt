package com.abrarshakhi.lumen.core.data.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    @ColumnInfo(name = "created_at") val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at") val updatedAtMillis: Long,
)

@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    val title: String,
    val body: String,
)

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun byId(id: Long): NoteEntity?

    @Query(
        """
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.rowid = notes_fts.rowid
        WHERE notes_fts MATCH :match
        ORDER BY notes.updated_at DESC
        LIMIT :limit
        """,
    )
    suspend fun search(match: String, limit: Int): List<NoteEntity>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
