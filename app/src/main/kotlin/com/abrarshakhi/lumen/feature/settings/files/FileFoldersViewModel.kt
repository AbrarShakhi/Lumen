package com.abrarshakhi.lumen.feature.settings.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.data.db.FileIndexDao
import com.abrarshakhi.lumen.core.platform.files.DocumentTreeGrants
import com.abrarshakhi.lumen.core.platform.files.SafDocumentIndexer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FileFoldersState(
    val folders: List<DocumentTreeGrants.Grant> = emptyList(),
    val indexedCount: Int = 0,
    val isIndexing: Boolean = false,
)

/**
 * Manages the folders Lumen may search for documents.
 *
 * Indexing runs here rather than in a background worker: it is started by an explicit user
 * action, the user is looking at the screen while it happens, and ColorOS is aggressive
 * about killing background jobs. The indexer upserts incrementally, so an interrupted walk
 * keeps whatever it already found rather than starting over.
 */
class FileFoldersViewModel(
    private val grants: DocumentTreeGrants,
    private val indexer: SafDocumentIndexer,
    private val dao: FileIndexDao,
) : ViewModel() {

    private val _state = MutableStateFlow(FileFoldersState())
    val state: StateFlow<FileFoldersState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onFolderPicked(treeUri: Uri) {
        grants.persist(treeUri)
        reindex()
    }

    fun removeFolder(uri: String) {
        viewModelScope.launch {
            grants.release(Uri.parse(uri))
            dao.deleteTree(uri)
            refreshNow()
        }
    }

    fun reindex() {
        viewModelScope.launch {
            _state.update { it.copy(isIndexing = true, folders = grants.grants()) }
            runCatching { indexer.indexAll() }
            refreshNow()
            _state.update { it.copy(isIndexing = false) }
        }
    }

    private fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    private suspend fun refreshNow() {
        _state.update {
            it.copy(folders = grants.grants(), indexedCount = runCatching { dao.count() }.getOrDefault(0))
        }
    }
}
