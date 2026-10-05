package com.abrarshakhi.lumen.core.platform.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

/**
 * Tracks which folders the user has given Lumen access to.
 *
 * SAF grants are the only way to reach documents, and they must be made *persistable* —
 * a grant taken without [Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION] is lost the moment
 * the process dies, which would silently break file search after every restart.
 */
class DocumentTreeGrants(
    private val context: Context,
) {

    data class Grant(val uri: String, val displayName: String)

    fun grants(): List<Grant> = context.contentResolver.persistedUriPermissions
        .filter { it.isReadPermission }
        .map { permission ->
            Grant(
                uri = permission.uri.toString(),
                displayName = permission.uri.readableName(),
            )
        }

    fun hasAny(): Boolean = grants().isNotEmpty()

    /** Persists a grant returned by the document-tree picker. */
    fun persist(treeUri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    fun release(treeUri: Uri) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    }

    /** Turns `…%3ADownload` into `Download`, which is what the user actually picked. */
    private fun Uri.readableName(): String {
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(this) }.getOrNull()
        return documentId?.substringAfterLast(':')?.takeIf { it.isNotBlank() }
            ?: lastPathSegment.orEmpty()
    }
}
