package com.abrarshakhi.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    /**
     * Title, or the first non-blank line of the body where the note has none.
     *
     * "First non-blank" rather than simply "first": a note starting with a blank line would
     * otherwise have no visible title at all, which is why this used to need an "Untitled"
     * placeholder. A note with neither title nor body is never saved, so for any persisted
     * note this is non-empty.
     */
    val displayTitle: String
        get() = title.ifBlank {
            body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        }

    val preview: String
        get() = body.replace('\n', ' ').trim()
}

interface NoteRepository {
    fun observeAll(): Flow<List<Note>>

    suspend fun byId(id: Long): Note?

    /** Full-text search. Returns empty rather than throwing on an unparseable query. */
    suspend fun search(query: String, limit: Int = 10): List<Note>

    /** Creates or updates, returning the note's id. */
    suspend fun save(id: Long?, title: String, body: String): Long

    suspend fun delete(id: Long)
}
