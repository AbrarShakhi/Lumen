package com.abrarshakhi.lumen.feature.about

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.core.domain.document.MarkdownBlock
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState
import com.abrarshakhi.lumen.core.mvi.Reducer

sealed interface DocumentIntent : MviIntent {
    data object Load : DocumentIntent
}

sealed interface DocumentAction : MviAction {
    data object Loading : DocumentAction
    data class Loaded(val blocks: List<MarkdownBlock>) : DocumentAction
    data object Failed : DocumentAction
}

sealed interface DocumentContent {
    data object Loading : DocumentContent
    data class Ready(val blocks: List<MarkdownBlock>) : DocumentContent
    data object Unavailable : DocumentContent
}

@Immutable
data class DocumentState(
    val document: ProjectDocument,
    val content: DocumentContent = DocumentContent.Loading,
) : MviState

sealed interface DocumentEffect : MviEffect

object DocumentReducer : Reducer<DocumentState, DocumentAction> {
    override fun reduce(state: DocumentState, action: DocumentAction): DocumentState = when (action) {
        DocumentAction.Loading -> state.copy(content = DocumentContent.Loading)
        is DocumentAction.Loaded -> state.copy(content = DocumentContent.Ready(action.blocks))
        DocumentAction.Failed -> state.copy(content = DocumentContent.Unavailable)
    }
}
