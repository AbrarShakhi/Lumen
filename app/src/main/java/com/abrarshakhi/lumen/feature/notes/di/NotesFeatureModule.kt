package com.abrarshakhi.lumen.feature.notes.di

import com.abrarshakhi.lumen.core.data.di.ApplicationScopeQualifier
import com.abrarshakhi.lumen.feature.notes.NoteEditorViewModel
import com.abrarshakhi.lumen.feature.notes.NotesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val notesFeatureModule = module {
    viewModelOf(::NotesViewModel)
    // The editor is parameterised by which note it is editing; null means a new one.
    viewModel { (noteId: Long?) ->
        NoteEditorViewModel(
            notes = get(),
            applicationScope = get(ApplicationScopeQualifier),
            noteId = noteId,
        )
    }
}
