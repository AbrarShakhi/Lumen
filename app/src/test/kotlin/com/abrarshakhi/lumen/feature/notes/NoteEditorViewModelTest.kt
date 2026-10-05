package com.abrarshakhi.lumen.feature.notes

import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import com.abrarshakhi.lumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeNotes : NoteRepository {
        val notes = mutableMapOf<Long, Note>()
        var saveCount = 0
            private set
        private var nextId = 1L

        override fun observeAll(): Flow<List<Note>> = MutableStateFlow(notes.values.toList())

        override suspend fun byId(id: Long): Note? = notes[id]

        override suspend fun search(query: String, limit: Int): List<Note> = emptyList()

        override suspend fun save(id: Long?, title: String, body: String): Long {
            saveCount++
            val noteId = id ?: nextId++
            notes[noteId] = Note(noteId, title, body, 0, saveCount.toLong())
            return noteId
        }

        override suspend fun delete(id: Long) {
            notes.remove(id)
        }
    }

    @Test
    fun `opening an existing note does not rewrite it`() = runTest {
        // Rewriting would bump updated_at and silently reorder the notes list just because
        // the user looked at a note.
        val repository = FakeNotes()
        repository.save(null, "Shopping", "milk")
        val savesAfterSetup = repository.saveCount

        val viewModel = NoteEditorViewModel(repository, backgroundScope, noteId = 1L)
        advanceUntilIdle()

        assertEquals("Shopping", viewModel.state.value.title)
        assertEquals(savesAfterSetup, repository.saveCount, "merely opening a note must not save it")
    }

    @Test
    fun `editing an existing note saves it`() = runTest {
        val repository = FakeNotes()
        repository.save(null, "Shopping", "milk")
        val before = repository.saveCount

        val viewModel = NoteEditorViewModel(repository, backgroundScope, noteId = 1L)
        advanceUntilIdle()
        viewModel.dispatch(NoteEditorIntent.BodyChanged("milk and bread"))
        advanceUntilIdle()

        assertTrue(repository.saveCount > before, "an edit must persist")
        assertEquals("milk and bread", repository.notes.getValue(1L).body)
    }

    @Test
    fun `a note left entirely empty is not created`() = runTest {
        val repository = FakeNotes()
        val viewModel = NoteEditorViewModel(repository, backgroundScope, noteId = null)
        advanceUntilIdle()

        viewModel.dispatch(NoteEditorIntent.BodyChanged("   "))
        advanceUntilIdle()

        assertEquals(0, repository.notes.size, "whitespace alone should not become a note")
    }

    @Test
    fun `deleting removes the note and blocks any later save`() = runTest {
        val repository = FakeNotes()
        repository.save(null, "Shopping", "milk")

        val viewModel = NoteEditorViewModel(repository, backgroundScope, noteId = 1L)
        advanceUntilIdle()
        viewModel.dispatch(NoteEditorIntent.Deleted)
        advanceUntilIdle()

        assertEquals(0, repository.notes.size)

        // A pending autosave must not resurrect what was just deleted.
        viewModel.dispatch(NoteEditorIntent.BodyChanged("resurrected"))
        advanceUntilIdle()
        assertEquals(0, repository.notes.size, "a deleted note must stay deleted")
    }
}
