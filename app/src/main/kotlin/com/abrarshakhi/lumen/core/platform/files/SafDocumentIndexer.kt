package com.abrarshakhi.lumen.core.platform.files

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.abrarshakhi.lumen.core.data.db.FileIndexDao
import com.abrarshakhi.lumen.core.data.db.FileIndexEntity
import com.abrarshakhi.lumen.core.domain.match.TextNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Walks a user-granted folder and records the documents in it.
 *
 * Exists because scoped storage hides documents from MediaStore: on the test device a
 * plain `.txt` in `Download/` is visible to the shell and invisible to the app. SAF is the
 * only route to them, and SAF has no search — hence an index.
 *
 * Uses raw `ContentResolver` queries against `buildChildDocumentsUriUsingTree` rather than
 * `DocumentFile`, which performs one IPC per attribute per file and is roughly an order of
 * magnitude slower over a large tree.
 */
class SafDocumentIndexer(
    private val context: Context,
    private val dao: FileIndexDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    /** Walks every granted tree. Safe to call repeatedly; entries are upserted. */
    suspend fun indexAll(): Int = withContext(Dispatchers.IO) {
        var total = 0
        for (permission in context.contentResolver.persistedUriPermissions) {
            if (!permission.isReadPermission) continue
            total += runCatching { indexTree(permission.uri) }.getOrDefault(0)
        }
        total
    }

    private suspend fun indexTree(treeUri: Uri): Int {
        val startedAt = now()
        val rootId = DocumentsContract.getTreeDocumentId(treeUri)
        var count = 0

        // Explicit stack rather than recursion: a deep tree would otherwise risk a
        // StackOverflowError on a directory structure Lumen does not control.
        val pending = ArrayDeque<Pair<String, String>>()
        pending.add(rootId to "")

        val batch = mutableListOf<FileIndexEntity>()

        while (pending.isNotEmpty()) {
            // Cancellation-cooperative: indexing a large tree must stop promptly when the
            // scope that started it goes away.
            coroutineContext.ensureActive()

            val (documentId, path) = pending.removeFirst()
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)

            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE,
                    DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                ),
                null,
                null,
                null,
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                val modifiedColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                while (cursor.moveToNext()) {
                    val childId = cursor.getString(idColumn) ?: continue
                    val name = cursor.getString(nameColumn)?.takeIf { it.isNotBlank() } ?: continue
                    val mime = cursor.getString(mimeColumn)

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        if (!name.startsWith('.') && pending.size < MAX_PENDING_DIRS) {
                            pending.add(childId to if (path.isEmpty()) name else "$path/$name")
                        }
                        continue
                    }

                    batch += FileIndexEntity(
                        documentUri = DocumentsContract
                            .buildDocumentUriUsingTree(treeUri, childId).toString(),
                        treeUri = treeUri.toString(),
                        name = name,
                        normalizedName = TextNormalizer.normalize(name),
                        mimeType = mime,
                        sizeBytes = cursor.getLong(sizeColumn),
                        modifiedAtMillis = cursor.getLong(modifiedColumn),
                        folder = path.takeIf { it.isNotEmpty() },
                        indexedAtMillis = startedAt,
                    )
                    count++

                    // Written in batches so a long walk makes results available as it goes,
                    // and so an interrupted index keeps what it already found.
                    if (batch.size >= BATCH_SIZE) {
                        dao.upsertAll(batch.toList())
                        batch.clear()
                    }
                }
            }
        }

        if (batch.isNotEmpty()) dao.upsertAll(batch)
        // Anything not seen in this walk has been moved or deleted.
        dao.deleteStale(treeUri.toString(), startedAt)
        return count
    }

    private companion object {
        const val BATCH_SIZE = 200

        /** Guards against a pathological tree exhausting memory. */
        const val MAX_PENDING_DIRS = 5_000
    }
}
