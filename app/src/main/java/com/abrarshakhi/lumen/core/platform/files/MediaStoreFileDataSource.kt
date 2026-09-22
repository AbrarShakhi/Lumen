package com.abrarshakhi.lumen.core.platform.files

import android.content.Context
import android.provider.MediaStore
import com.abrarshakhi.lumen.core.domain.repository.DeviceFile
import com.abrarshakhi.lumen.core.domain.repository.FileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * File search backed by MediaStore.
 *
 * Filtering happens in the query rather than in Kotlin: MediaStore is a database, and
 * pulling every row across the Binder boundary to filter them here would be both slow and
 * memory-hungry on a device with thousands of files.
 *
 * Verified on the test device that `MediaStore.Files` returns documents as well as media —
 * OEM behaviour differs here, so a device that only indexes media will simply return fewer
 * results rather than failing.
 */
class MediaStoreFileDataSource(
    private val context: Context,
) : FileRepository {

    override suspend fun search(query: String, limit: Int): List<DeviceFile> {
        val term = query.trim()
        if (term.length < MIN_TERM_LENGTH) return emptyList()

        return withContext(Dispatchers.IO) {
            runCatching { queryFiles(term, limit) }.getOrDefault(emptyList())
        }
    }

    private fun queryFiles(term: String, limit: Int): List<DeviceFile> {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.RELATIVE_PATH,
        )

        // A null MIME type means a directory row; those are not openable and would be noise.
        // ESCAPE is required: SQLite treats a backslash as an ordinary character unless
        // the query declares it, so escaping without this would match the backslash itself.
        val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? ESCAPE '\\' " +
            "AND ${MediaStore.Files.FileColumns.MIME_TYPE} IS NOT NULL"
        val selectionArgs = arrayOf("%${term.escapeForLike()}%")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val results = mutableListOf<DeviceFile>()

        context.contentResolver.query(
            MediaStore.Files.getContentUri(VOLUME),
            projection,
            selection,
            selectionArgs,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.RELATIVE_PATH)

            while (cursor.moveToNext() && results.size < limit) {
                val name = cursor.getString(nameColumn)?.takeIf { it.isNotBlank() } ?: continue
                val id = cursor.getLong(idColumn)

                results += DeviceFile(
                    id = id,
                    name = name,
                    uri = MediaStore.Files.getContentUri(VOLUME, id).toString(),
                    mimeType = cursor.getString(mimeColumn),
                    sizeBytes = cursor.getLong(sizeColumn),
                    modifiedAtSeconds = cursor.getLong(modifiedColumn),
                    folder = cursor.getString(pathColumn)?.trim('/')?.takeIf { it.isNotBlank() },
                )
            }
        }

        return results
    }

    /**
     * Escapes SQL LIKE wildcards.
     *
     * Without this, searching for a literal `%` or `_` would silently match everything —
     * `_` is a single-character wildcard, which is easy to forget.
     */
    private fun String.escapeForLike(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private companion object {
        const val VOLUME = MediaStore.VOLUME_EXTERNAL
        const val MIN_TERM_LENGTH = 2
    }
}
