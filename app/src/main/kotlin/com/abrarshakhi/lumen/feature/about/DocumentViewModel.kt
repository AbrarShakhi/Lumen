package com.abrarshakhi.lumen.feature.about

import com.abrarshakhi.lumen.core.domain.document.DocumentRepository
import com.abrarshakhi.lumen.core.domain.document.MarkdownBlock
import com.abrarshakhi.lumen.core.domain.document.MarkdownParser
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import kotlinx.coroutines.CancellationException

class DocumentViewModel(
    private val document: ProjectDocument,
    private val repository: DocumentRepository,
) : MviViewModel<DocumentIntent, DocumentAction, DocumentState, DocumentEffect>(
    initialState = DocumentState(document = document),
    reducer = DocumentReducer,
) {

    init {
        dispatch(DocumentIntent.Load)
    }

    override suspend fun handleIntent(intent: DocumentIntent) {
        when (intent) {
            DocumentIntent.Load -> load()
        }
    }

    private suspend fun load() {
        reduce(DocumentAction.Loading)
        try {
            val source = repository.load(document)
            reduce(DocumentAction.Loaded(MarkdownParser.parse(source, document.format).withoutTitle()))
        } catch (failure: Exception) {
            if (failure is CancellationException) throw failure
            reduce(DocumentAction.Failed)
        }
    }

    private fun List<MarkdownBlock>.withoutTitle(): List<MarkdownBlock> {
        val first = firstOrNull()
        return if (first is MarkdownBlock.Heading && first.level == 1) drop(1) else this
    }
}
