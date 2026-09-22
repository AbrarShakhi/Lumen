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

/**
 * Edits one note, saving as you type.
 *
 * There is no save button: a scratchpad should keep whatever you wrote without being asked.
 * Two mechanisms cover that, because neither is sufficient alone:
 *
 *  - a debounced autosave on [viewModelScope], which persists during normal typing and so
 *    survives a crash or the process being killed;
 *  - a final flush from [onCleared] on an **application-scoped** coroutine, catching edits
 *    made in the last few hundred milliseconds before the screen closed.
 *
 * The application scope is essential. An earlier version saved from the composable's
 * `onDispose` using `rememberCoroutineScope`, which is already cancelled by the time
 * `onDispose` runs — the launch silently never executed and every note was lost.
 */
@OptIn(FlowPreview::class)
class NoteEditorViewModel(
    private val notes: NoteRepository,
    private val applicationScope: CoroutineScope,
    private var noteId: Long?,
) : MviViewModel<NoteEditorIntent, NoteEditorAction, NoteEditorState, NoteEditorEffect>(
    initialState = NoteEditorState(),
    reducer = NoteEditorReducer,
) {

    /** Serialises saves so the autosave and the final flush cannot interleave. */
    private val saveLock = Mutex()

    @Volatile
    private var discarded = false

    /**
     * The content as last persisted.
     *
     * Compared before writing so that merely *opening* a note does not rewrite it — that
     * would bump `updated_at` and silently reorder the notes list just by looking at one.
     */
    @Volatile
    private var lastPersisted: NoteEditorState? = null

    init {
        viewModelScope.launch { load() }

        state
            // Skip the initial empty state; only real edits are worth writing.
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
                // Set before deleting so a racing autosave cannot resurrect the note.
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
        // viewModelScope is cancelled by the time this runs, so the flush has to happen on
        // a scope that outlives the screen.
        applicationScope.launch { persist() }
    }

    private suspend fun persist() = saveLock.withLock {
        if (discarded) return@withLock
        val current = currentState
        if (current == lastPersisted) return@withLock
        if (current.isBlank) {
            // Nothing was written; an empty note would just be clutter.
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
