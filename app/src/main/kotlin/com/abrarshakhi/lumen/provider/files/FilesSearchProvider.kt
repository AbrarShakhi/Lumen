package com.abrarshakhi.lumen.provider.files

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.DeviceFile
import com.abrarshakhi.lumen.core.domain.repository.FileKind
import com.abrarshakhi.lumen.core.domain.repository.FileRepository
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.SnapshotSearchProvider
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Finds files on the device.
 *
 * Declares every storage permission it might need across SDK levels; the gate resolves
 * which ones actually apply to *this* device — `READ_EXTERNAL_STORAGE` below API 33, the
 * granular media permissions from 33. Hard-coding either set would break one of them.
 */
class FilesSearchProvider(
    private val files: FileRepository,
) : SnapshotSearchProvider() {

    override val id = ProviderId("files")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_files),
        category = ResultCategory.File,
        order = 40,
        requiredPermissions = listOf(
            AppPermission.ReadExternalStorage,
            AppPermission.ReadMediaImages,
            AppPermission.ReadMediaVideo,
            AppPermission.ReadMediaAudio,
        ),
        timeout = 2.seconds,
        // Every query is a ContentResolver round trip, so it is worth not firing per keystroke.
        debounce = 150.milliseconds,
        minQueryLength = 2,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> =
        files.search(query.terms, limit = MAX_RESULTS).map { it.toResult() }

    private fun DeviceFile.toResult() = SearchResult(
        id = ResultId("file:$id"),
        providerId = this@FilesSearchProvider.id,
        title = name,
        subtitle = listOfNotNull(folder, humanSize()).joinToString(" · ").takeIf { it.isNotBlank() },
        icon = IconSource.Vector(kind.icon()),
        category = ResultCategory.File,
        // MediaStore ranks by recency rather than relevance, so results share one score and
        // the query's own ordering decides.
        score = 0.75f,
        rankingKey = "file:$id",
        actions = ResultActions(
            primary = ResultAction(
                id = "open-file",
                label = TextValue.Res(R.string.action_open),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                // A content URI needs a read grant travelling with the intent, or the
                // receiving app cannot open it.
                invoke = { ActionOutcome.Launch(PlatformIntent.ViewDocument(uri, mimeType)) },
            ),
            secondary = listOf(
                ResultAction(
                    id = "share-file",
                    label = TextValue.Res(R.string.action_share),
                    icon = IconSource.Vector(LumenIcon.Share),
                    kind = ActionKind.Share,
                    invoke = { ActionOutcome.Launch(PlatformIntent.Share(uri)) },
                ),
            ),
        ),
    )

    private fun DeviceFile.humanSize(): String? {
        if (sizeBytes <= 0) return null
        val units = listOf("B", "KB", "MB", "GB")
        var value = sizeBytes.toDouble()
        var unit = 0
        while (value >= 1024 && unit < units.lastIndex) {
            value /= 1024
            unit++
        }
        return if (unit == 0) "${value.toInt()} ${units[unit]}" else "%.1f %s".format(value, units[unit])
    }

    private fun FileKind.icon(): LumenIcon = when (this) {
        FileKind.Image -> LumenIcon.Image
        FileKind.Video -> LumenIcon.Video
        FileKind.Audio -> LumenIcon.Music
        FileKind.Document -> LumenIcon.File
        FileKind.Archive -> LumenIcon.Folder
        FileKind.Other -> LumenIcon.File
    }

    private companion object {
        const val MAX_RESULTS = 20
    }
}
