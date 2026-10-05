package com.abrarshakhi.lumen.core.domain.repository

data class DeviceFile(
    val id: Long,
    val name: String,
    val uri: String,
    val mimeType: String?,
    val sizeBytes: Long,
    val modifiedAtSeconds: Long,
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

interface FileRepository {
    suspend fun search(query: String, limit: Int = 20): List<DeviceFile>
}
