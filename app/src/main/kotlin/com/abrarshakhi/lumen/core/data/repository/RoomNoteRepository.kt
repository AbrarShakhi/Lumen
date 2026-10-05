package com.abrarshakhi.lumen.core.data.repository

import com.abrarshakhi.lumen.core.data.db.NoteDao
import com.abrarshakhi.lumen.core.data.db.NoteEntity
import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteRepository(
    private val dao: NoteDao,
    private val now: () -> Long = System::currentTimeMillis,
) : NoteRepository {

    override fun observeAll(): Flow<List<Note>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun byId(id: Long): Note? = dao.byId(id)?.toDomain()

    override suspend fun search(query: String, limit: Int): List<Note> {
        val match = FtsQuery.sanitize(query) ?: return emptyList()
        // A malformed MATCH expression throws at the SQLite level; declining to search is
        // the right answer for a half-typed query, not a crash.
        return runCatching { dao.search(match, limit) }
            .getOrDefault(emptyList())
            .map { it.toDomain() }
    }

    override suspend fun save(id: Long?, title: String, body: String): Long {
        val timestamp = now()
        val existing = id?.let { dao.byId(it) }

        return if (existing == null) {
            dao.insert(
                NoteEntity(
                    title = title.trim(),
                    body = body,
                    createdAtMillis = timestamp,
                    updatedAtMillis = timestamp,
                ),
            )
        } else {
            dao.update(
                existing.copy(
                    title = title.trim(),
                    body = body,
                    updatedAtMillis = timestamp,
                ),
            )
            existing.id
        }
    }

    override suspend fun delete(id: Long) = dao.deleteById(id)

    private fun NoteEntity.toDomain() = Note(
        id = id,
        title = title,
        body = body,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
}

/**
 * Turns a typed query into a safe FTS4 `MATCH` expression.
 *
 * FTS4 has its own query syntax, so raw input cannot be passed through: quotes, `*`, `-`,
 * `:` and `^` are all operators there, and an unbalanced one is a syntax error rather than
 * a search that finds nothing. Stripping them and appending `*` per token gives
 * prefix matching, which is what someone typing mid-word expects.
 */
internal object FtsQuery {

    private val UNSAFE = Regex("""["*\-:^()]""")

    fun sanitize(raw: String): String? {
        val tokens = raw
            .replace(UNSAFE, " ")
            .split(' ', '\t', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (tokens.isEmpty()) return null

        // Space-separated terms are ANDed by FTS4, which matches the expectation that
        // adding a word narrows the search.
        return tokens.joinToString(" ") { "$it*" }
    }
}
