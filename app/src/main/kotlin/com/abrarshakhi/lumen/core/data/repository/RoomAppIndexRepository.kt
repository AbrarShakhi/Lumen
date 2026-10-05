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

class RoomAppIndexRepository(
    private val dao: AppIndexDao,
    private val dataSource: LauncherAppsDataSource,
    private val scope: CoroutineScope,
    private val now: () -> Long = System::currentTimeMillis,
) : AppIndexRepository {

    private val refreshLock = Mutex()

    init {
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
