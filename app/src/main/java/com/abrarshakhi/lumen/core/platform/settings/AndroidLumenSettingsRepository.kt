package com.abrarshakhi.lumen.core.platform.settings

import android.content.Context
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.LumenSettingEntry
import com.abrarshakhi.lumen.core.domain.repository.LumenSettingsRepository
import com.abrarshakhi.lumen.core.domain.search.InternalDestination

/**
 * Lumen's own settings, with titles resolved against the current locale.
 *
 * Keywords are matched in addition to the title so that "dark mode" finds the theme
 * setting even though the screen is called something else.
 */
class AndroidLumenSettingsRepository(
    private val context: Context,
) : LumenSettingsRepository {

    private val cached: List<LumenSettingEntry> by lazy {
        listOf(
            entry("settings", R.string.lumen_setting_all, InternalDestination.Settings, "preferences", "options"),
            entry("theme", R.string.lumen_setting_theme, InternalDestination.Settings, "dark mode", "appearance", "font size", "colours"),
            entry("providers", R.string.lumen_setting_sources, InternalDestination.Providers, "search sources", "providers"),
            entry("notes", R.string.notes_title, InternalDestination.Notes, "my notes"),
        )
    }

    override fun entries(): List<LumenSettingEntry> = cached

    private fun entry(
        id: String,
        titleRes: Int,
        destination: InternalDestination,
        vararg keywords: String,
    ): LumenSettingEntry {
        val title = context.getString(titleRes)
        return LumenSettingEntry(
            id = id,
            title = title,
            destination = destination,
            searchable = SearchableText.of(title),
            keywords = keywords.map(SearchableText::of),
        )
    }
}
