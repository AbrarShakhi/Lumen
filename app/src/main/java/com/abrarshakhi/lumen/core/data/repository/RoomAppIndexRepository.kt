package com.abrarshakhi.lumen.core.data.repository

import com.abrarshakhi.lumen.core.data.db.AppIndexDao
import com.abrarshakhi.lumen.core.data.db.AppIndexEntity
import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.AppIndexRepository
import com.abrarshakhi.lumen.core.domain.repository.IndexedApp
import com.abrarshakhi.lumen.core.platform.app.LauncherAppsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The app index, persisted in Room and projected into memory.
 *
 * Room is what makes a cold start fast: the previous scan is available immediately while a
 * fresh one runs in the background, so the first keystroke after launch already has
 * something to match against.
 */
class RoomAppIndexRepository(
    private val dao: AppIndexDao,
    private val dataSource: LauncherAppsDataSource,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
) : AppIndexRepository {

    // Serialises refreshes so a package-change broadcast arriving mid-scan cannot
    // interleave two scans and delete the rows the other just wrote.
    private val refreshLock = Mutex()

    init {
        // Keeps the index honest while the app is running: newly installed apps become
        // searchable immediately, and uninstalled ones stop being offered.
        dataSource.packageChanges()
            .onEach { refresh() }
            .launchIn(scope)
    }

    override val apps: StateFlow<List<IndexedApp>> = dao.observeAll()
        .catch { emit(emptyList()) }
        .map { rows -> rows.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun refresh() {
        refreshLock.withLock {
            val scanStartedAt = now()
            val installed = dataSource.loadInstalledApps()
            if (installed.isEmpty()) return

            dao.upsertAll(
                installed.map { app ->
                    val searchable = SearchableText.of(app.label)
                    AppIndexEntity(
                        packageName = app.packageName,
                        activityName = app.activityName,
                        label = app.label,
                        normalizedLabel = searchable.normalized,
                        acronym = searchable.acronym,
                        isSystem = app.isSystem,
                        indexedAtMillis = scanStartedAt,
                    )
                },
            )
            // Anything not touched by this scan is no longer installed.
            dao.deleteStale(scanStartedAt)
        }
    }

    private fun AppIndexEntity.toDomain() = IndexedApp(
        packageName = packageName,
        activityName = activityName,
        label = label,
        searchable = SearchableText.restored(
            original = label,
            normalized = normalizedLabel,
            acronym = acronym,
        ),
        isSystem = isSystem,
    )
}
