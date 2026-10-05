package com.abrarshakhi.lumen.core.platform.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

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

    private fun Uri.readableName(): String {
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(this) }.getOrNull()
        return documentId?.substringAfterLast(':')?.takeIf { it.isNotBlank() }
            ?: lastPathSegment.orEmpty()
    }
}
