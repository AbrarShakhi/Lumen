package com.abrarshakhi.lumen.core.domain.repository

import kotlinx.coroutines.flow.Flow

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
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

    suspend fun search(query: String, limit: Int = 10): List<Note>

    suspend fun save(id: Long?, title: String, body: String): Long

    suspend fun delete(id: Long)
}
