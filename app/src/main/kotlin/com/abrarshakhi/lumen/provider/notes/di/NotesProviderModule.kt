package com.abrarshakhi.lumen.provider.notes.di

import com.abrarshakhi.lumen.core.data.db.LumenDatabase
import com.abrarshakhi.lumen.core.data.db.NoteDao
import com.abrarshakhi.lumen.core.data.repository.RoomNoteRepository
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.notes.NotesSearchProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val notesProviderModule = module {
    single<NoteDao> { get<LumenDatabase>().noteDao() }
    single<NoteRepository> { RoomNoteRepository(get()) }
    single { NotesSearchProvider(notes = get()) } bind SearchProvider::class
}
