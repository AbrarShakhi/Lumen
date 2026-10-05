package com.abrarshakhi.lumen.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.vector.ImageVector
import com.abrarshakhi.lumen.core.domain.search.LumenIcon

/**
 * Maps the domain's icon vocabulary onto concrete vectors.
 *
 * The indirection is what lets providers name an icon without the domain layer importing
 * Compose. This `when` is over a closed enum, so it is exhaustive by construction.
 */
internal fun LumenIcon.toImageVector(): ImageVector = when (this) {
    LumenIcon.Search -> Icons.Filled.Search
    LumenIcon.App -> Icons.Filled.Apps
    LumenIcon.Contact -> Icons.Filled.Person
    LumenIcon.Phone -> Icons.Filled.Call
    LumenIcon.Message -> Icons.AutoMirrored.Filled.Message
    // Forum's stacked bubbles read differently from Message's single bubble at
    // icon size; the two were indistinguishable side by side in a result row.
    LumenIcon.Chat -> Icons.Filled.Forum
    LumenIcon.File -> Icons.Filled.Description
    LumenIcon.Folder -> Icons.Filled.Folder
    LumenIcon.Image -> Icons.Filled.Image
    LumenIcon.Video -> Icons.Filled.Videocam
    LumenIcon.Music -> Icons.Filled.MusicNote
    LumenIcon.Calendar -> Icons.Filled.CalendarMonth
    LumenIcon.Note -> Icons.AutoMirrored.Filled.Note
    LumenIcon.Settings -> Icons.Filled.Settings
    LumenIcon.Web -> Icons.Filled.Language
    LumenIcon.Calculator -> Icons.Filled.Calculate
    LumenIcon.Convert -> Icons.Filled.SwapHoriz
    LumenIcon.Clock -> Icons.Filled.Schedule
    LumenIcon.Currency -> Icons.Filled.CurrencyExchange
    LumenIcon.Ai -> Icons.Filled.AutoAwesome
    LumenIcon.Copy -> Icons.Filled.ContentCopy
    LumenIcon.Share -> Icons.Filled.Share
    LumenIcon.Open -> Icons.AutoMirrored.Filled.ArrowForward
    LumenIcon.Delete -> Icons.Filled.Delete
    LumenIcon.Edit -> Icons.Filled.Edit
    LumenIcon.Info -> Icons.Filled.Info
    LumenIcon.Uninstall -> Icons.Filled.Delete
    LumenIcon.Pin -> Icons.Filled.PushPin
    LumenIcon.Hide -> Icons.Filled.VisibilityOff
    LumenIcon.Trigger -> Icons.Filled.Shortcut
    LumenIcon.Permission -> Icons.Filled.Lock
    LumenIcon.Warning -> Icons.Filled.Warning
    LumenIcon.History -> Icons.Filled.History
}
