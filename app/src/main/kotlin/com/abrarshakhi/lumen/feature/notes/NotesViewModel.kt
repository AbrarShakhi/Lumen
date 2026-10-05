package com.abrarshakhi.lumen.feature.notes

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import com.abrarshakhi.lumen.core.mvi.Reducer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

object NotesReducer : Reducer<NotesState, NotesAction> {
    override fun reduce(state: NotesState, action: NotesAction): NotesState = when (action) {
        is NotesAction.NotesLoaded -> state.copy(notes = action.notes)
    }
}

class NotesViewModel(
    private val notes: NoteRepository,
) : MviViewModel<NotesIntent, NotesAction, NotesState, NotesEffect>(
    initialState = NotesState(),
    reducer = NotesReducer,
) {

    init {
        notes.observeAll()
            .onEach { reduce(NotesAction.NotesLoaded(it)) }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.Deleted -> notes.delete(intent.id)
        }
    }
}
