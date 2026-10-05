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

class SafDocumentIndexer(
    private val context: Context,
    private val dao: FileIndexDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

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

        val pending = ArrayDeque<Pair<String, String>>()
        pending.add(rootId to "")

        val batch = mutableListOf<FileIndexEntity>()

        while (pending.isNotEmpty()) {
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

                    if (batch.size >= BATCH_SIZE) {
                        dao.upsertAll(batch.toList())
                        batch.clear()
                    }
                }
            }
        }

        if (batch.isNotEmpty()) dao.upsertAll(batch)
        dao.deleteStale(treeUri.toString(), startedAt)
        return count
    }

    private companion object {
        const val BATCH_SIZE = 200

        const val MAX_PENDING_DIRS = 5_000
    }
}
