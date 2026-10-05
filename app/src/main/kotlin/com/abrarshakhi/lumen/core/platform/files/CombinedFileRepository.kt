package com.abrarshakhi.lumen.core.platform.files

import com.abrarshakhi.lumen.core.data.db.FileIndexDao
import com.abrarshakhi.lumen.core.data.db.FileIndexEntity
import com.abrarshakhi.lumen.core.domain.match.TextNormalizer
import com.abrarshakhi.lumen.core.domain.repository.DeviceFile
import com.abrarshakhi.lumen.core.domain.repository.FileRepository

/**
 * File search across both places files can live.
 *
 * Two sources are unavoidable, not a design preference: scoped storage gives the app a
 * MediaStore view containing only media, while documents are reachable solely through a
 * user-granted SAF tree. Verified on the test device — a `.txt` in the public `Download/`
 * folder is visible to the shell and invisible to the app.
 *
 * Media comes first because it is the larger and more frequently wanted set, and results
 * are de-duplicated by name in case a granted tree overlaps a media folder.
 */
class CombinedFileRepository(
    private val mediaStore: MediaStoreFileDataSource,
    private val documents: FileIndexDao,
) : FileRepository {

    override suspend fun search(query: String, limit: Int): List<DeviceFile> {
        val media = mediaStore.search(query, limit)

        val remaining = limit - media.size
        if (remaining <= 0) return media

        val term = TextNormalizer.normalize(query)
        val docs = runCatching { documents.search(term, remaining) }
            .getOrDefault(emptyList())
            .map { it.toDeviceFile() }

        val seen = media.mapTo(mutableSetOf()) { it.name.lowercase() }
        return media + docs.filter { seen.add(it.name.lowercase()) }
    }

    private fun FileIndexEntity.toDeviceFile() = DeviceFile(
        // Document URIs have no numeric id; the string identity is what matters, and the
        // hash only needs to be stable within one result set.
        id = documentUri.hashCode().toLong(),
        name = name,
        uri = documentUri,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        modifiedAtSeconds = modifiedAtMillis / 1000,
        folder = folder,
    )
}
