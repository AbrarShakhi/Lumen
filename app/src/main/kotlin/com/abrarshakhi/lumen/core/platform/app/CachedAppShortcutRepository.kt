package com.abrarshakhi.lumen.core.platform.app

import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.AppShortcutRepository
import com.abrarshakhi.lumen.core.domain.repository.IndexedShortcut
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedAppShortcutRepository(
    private val dataSource: LauncherAppsDataSource,
) : AppShortcutRepository {

    private val lock = Mutex()

    @Volatile
    private var cache: List<IndexedShortcut>? = null

    override suspend fun shortcuts(): List<IndexedShortcut> {
        cache?.let { return it }
        return lock.withLock {
            cache ?: run {
                val loaded = dataSource.loadShortcuts().map { shortcut ->
                    IndexedShortcut(
                        id = shortcut.id,
                        packageName = shortcut.packageName,
                        label = shortcut.label,
                        appLabel = shortcut.appLabel,
                        searchable = SearchableText.of(shortcut.label),
                    )
                }
                if (loaded.isNotEmpty()) cache = loaded
                loaded
            }
        }
    }

    override fun invalidate() {
        cache = null
    }
}
