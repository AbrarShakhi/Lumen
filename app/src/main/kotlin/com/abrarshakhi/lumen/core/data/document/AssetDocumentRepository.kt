package com.abrarshakhi.lumen.core.data.document

import android.content.res.AssetManager
import com.abrarshakhi.lumen.core.domain.document.DocumentRepository
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetDocumentRepository(
    private val assets: AssetManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DocumentRepository {

    override suspend fun load(document: ProjectDocument): String = withContext(dispatcher) {
        assets.open("$DIRECTORY/${document.fileName}").bufferedReader().use { it.readText() }
    }

    private companion object {
        const val DIRECTORY = "docs"
    }
}
