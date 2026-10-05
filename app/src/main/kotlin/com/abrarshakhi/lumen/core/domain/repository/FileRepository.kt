package com.abrarshakhi.lumen.core.domain.repository

/** A file on the device that Lumen can open. */
data class DeviceFile(
    val id: Long,
    val name: String,
    val uri: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val modifiedAtSeconds: Long,
    /** Directory the file sits in, shown as context — two files often share a name. */
    val folder: String?,
) {
    val kind: FileKind get() = FileKind.of(mimeType, name)
}

enum class FileKind {
    Image, Video, Audio, Document, Archive, Other;

    companion object {
        fun of(mimeType: String?, name: String): FileKind {
            when {
                mimeType == null -> Unit
                mimeType.startsWith("image/") -> return Image
                mimeType.startsWith("video/") -> return Video
                mimeType.startsWith("audio/") -> return Audio
            }
            // Many files arrive as application/octet-stream, so the extension is often the
            // only usable signal about what something actually is.
            return when (name.substringAfterLast('.', "").lowercase()) {
                "pdf", "doc", "docx", "txt", "md", "rtf", "odt", "xls", "xlsx", "ppt", "pptx" -> Document
                "zip", "rar", "7z", "tar", "gz", "apk" -> Archive
                "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp" -> Image
                "mp4", "mkv", "avi", "mov", "webm" -> Video
                "mp3", "m4a", "wav", "ogg", "flac", "opus" -> Audio
                else -> Other
            }
        }
    }
}

/**
 * Searches files on the device.
 *
 * Queries the platform's media index on demand rather than building one of Lumen's own:
 * MediaStore is already a maintained index of the device's files, and duplicating it into
 * Room would mean a slow first scan, a background job to keep it fresh, and two sources of
 * truth that can disagree.
 */
interface FileRepository {
    suspend fun search(query: String, limit: Int = 20): List<DeviceFile>
}
