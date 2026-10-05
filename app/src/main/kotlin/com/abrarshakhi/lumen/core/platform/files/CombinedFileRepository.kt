package com.abrarshakhi.lumen.core.platform.files

import com.abrarshakhi.lumen.core.data.db.FileIndexDao
import com.abrarshakhi.lumen.core.data.db.FileIndexEntity
import com.abrarshakhi.lumen.core.domain.match.TextNormalizer
import com.abrarshakhi.lumen.core.domain.repository.DeviceFile
import com.abrarshakhi.lumen.core.domain.repository.FileRepository

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
        id = documentUri.hashCode().toLong(),
        name = name,
        uri = documentUri,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        modifiedAtSeconds = modifiedAtMillis / 1000,
        folder = folder,
    )
}
