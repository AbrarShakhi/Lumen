package com.abrarshakhi.lumen.feature.notes

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import com.abrarshakhi.lumen.core.mvi.Reducer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.milliseconds

object NoteEditorReducer : Reducer<NoteEditorState, NoteEditorAction> {
    override fun reduce(state: NoteEditorState, action: NoteEditorAction): NoteEditorState =
        when (action) {
            is NoteEditorAction.Loaded -> state.copy(title = action.title, body = action.body)
            is NoteEditorAction.TitleChanged -> state.copy(title = action.value)
            is NoteEditorAction.BodyChanged -> state.copy(body = action.value)
        }
}

@OptIn(FlowPreview::class)
class NoteEditorViewModel(
    private val notes: NoteRepository,
    private val applicationScope: CoroutineScope,
    private var noteId: Long?,
) : MviViewModel<NoteEditorIntent, NoteEditorAction, NoteEditorState, NoteEditorEffect>(
    initialState = NoteEditorState(),
    reducer = NoteEditorReducer,
) {

    private val saveLock = Mutex()

    @Volatile
    private var discarded = false

    @Volatile
    private var lastPersisted: NoteEditorState? = null

    init {
        viewModelScope.launch { load() }

        state
            .drop(1)
            .debounce(AUTOSAVE_DELAY)
            .onEach { persist() }
            .launchIn(viewModelScope)
    }

    private suspend fun load() {
        val id = noteId ?: return
        val note = notes.byId(id) ?: return
        val loaded = NoteEditorState(title = note.title, body = note.body)
        lastPersisted = loaded
        reduce(NoteEditorAction.Loaded(note.title, note.body))
    }

    override suspend fun handleIntent(intent: NoteEditorIntent) {
        when (intent) {
            is NoteEditorIntent.TitleChanged -> reduce(NoteEditorAction.TitleChanged(intent.value))
            is NoteEditorIntent.BodyChanged -> reduce(NoteEditorAction.BodyChanged(intent.value))

            NoteEditorIntent.Deleted -> {
                discarded = true
                noteId?.let { notes.delete(it) }
                noteId = null
                emitEffect(NoteEditorEffect.Close)
            }

            NoteEditorIntent.Closed -> {
                persist()
                emitEffect(NoteEditorEffect.Close)
            }
        }
    }

    override fun onCleared() {
        if (discarded) return
        applicationScope.launch { persist() }
    }

    private suspend fun persist() = saveLock.withLock {
        if (discarded) return@withLock
        val current = currentState
        if (current == lastPersisted) return@withLock
        if (current.isBlank) {
            noteId?.let { notes.delete(it) }
            noteId = null
            lastPersisted = current
            return@withLock
        }
        noteId = notes.save(noteId, current.title, current.body)
        lastPersisted = current
    }

    private companion object {
        val AUTOSAVE_DELAY = 400.milliseconds
    }
}
