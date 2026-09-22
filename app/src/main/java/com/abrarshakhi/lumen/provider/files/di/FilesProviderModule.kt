package com.abrarshakhi.lumen.provider.files.di

import com.abrarshakhi.lumen.core.data.db.FileIndexDao
import com.abrarshakhi.lumen.core.data.db.LumenDatabase
import com.abrarshakhi.lumen.core.domain.repository.FileRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.platform.files.CombinedFileRepository
import com.abrarshakhi.lumen.core.platform.files.DocumentTreeGrants
import com.abrarshakhi.lumen.core.platform.files.MediaStoreFileDataSource
import com.abrarshakhi.lumen.core.platform.files.SafDocumentIndexer
import com.abrarshakhi.lumen.provider.files.FilesSearchProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val filesProviderModule = module {

    single<FileIndexDao> { get<LumenDatabase>().fileIndexDao() }

    single { MediaStoreFileDataSource(androidContext()) }
    single { DocumentTreeGrants(androidContext()) }
    single { SafDocumentIndexer(context = androidContext(), dao = get()) }

    single<FileRepository> {
        CombinedFileRepository(mediaStore = get(), documents = get())
    }

    single { FilesSearchProvider(files = get()) } bind SearchProvider::class
}
