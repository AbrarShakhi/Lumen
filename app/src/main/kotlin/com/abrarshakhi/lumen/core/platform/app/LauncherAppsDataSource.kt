package com.abrarshakhi.lumen.core.platform.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/** A shortcut published by an installed app, e.g. "New message". */
data class AppShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val appLabel: String,
)

/** A launchable activity as reported by the system. */
data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isSystem: Boolean,
)

/**
 * Enumerates launchable apps.
 *
 * Uses `LauncherApps.getActivityList` rather than `PackageManager.queryIntentActivities`:
 * it returns label and component in one pass, and it is user-aware, so work-profile apps
 * are handled correctly rather than silently missing.
 */
class LauncherAppsDataSource(
    private val context: Context,
) {

    private val launcherApps: LauncherApps?
        get() = context.getSystemService<LauncherApps>()

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val apps = launcherApps ?: return@withContext emptyList()

        runCatching {
            apps.getActivityList(null, Process.myUserHandle()).mapNotNull { activity ->
                val label = activity.label?.toString()?.trim().orEmpty()
                if (label.isEmpty()) return@mapNotNull null

                InstalledApp(
                    packageName = activity.componentName.packageName,
                    activityName = activity.componentName.className,
                    label = label,
                    isSystem = activity.applicationInfo.isSystemApp(),
                )
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Emits whenever the set of installed apps changes.
     *
     * Without this the index is only built at startup, so an app installed while Lumen is
     * running stays unsearchable — and one that was uninstalled keeps appearing and fails
     * when launched. `LauncherApps.Callback` works without the home role, unlike shortcut
     * access.
     */
    fun packageChanges(): Flow<Unit> = callbackFlow {
        val apps = launcherApps
        if (apps == null) {
            close()
            return@callbackFlow
        }

        val callback = object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) = Unit

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean,
            ) {
                trySend(Unit)
            }
        }

        // LauncherApps requires a Handler; the main looper is fine because the callback
        // only signals, and the actual rescan happens on a background dispatcher.
        apps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { runCatching { apps.unregisterCallback(callback) } }
    }

    /**
     * Whether Lumen may read other apps' shortcuts.
     *
     * True only while Lumen holds the home role — Android exposes shortcuts exclusively to
     * the default launcher, with no partial fallback for other apps.
     */
    fun canReadShortcuts(): Boolean =
        runCatching { launcherApps?.hasShortcutHostPermission() == true }.getOrDefault(false)

    /**
     * Shortcuts published by installed apps.
     *
     * Returns empty unless Lumen holds the home role — Android shares shortcuts only with
     * the current launcher, and there is no partial fallback. Failures are swallowed rather
     * than propagated so the apps provider degrades to plain app results.
     */
    suspend fun loadShortcuts(): List<AppShortcut> = withContext(Dispatchers.IO) {
        val apps = launcherApps ?: return@withContext emptyList()
        if (!canReadShortcuts()) return@withContext emptyList()

        runCatching {
            val query = LauncherApps.ShortcutQuery().setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
            )

            apps.getShortcuts(query, Process.myUserHandle())
                .orEmpty()
                .mapNotNull { shortcut -> shortcut.toAppShortcut() }
        }.getOrDefault(emptyList())
    }

    /** Launches a shortcut. Only possible while Lumen is the launcher. */
    fun startShortcut(packageName: String, shortcutId: String): Boolean = runCatching {
        launcherApps?.startShortcut(
            packageName,
            shortcutId,
            null,
            null,
            Process.myUserHandle(),
        )
        true
    }.getOrDefault(false)

    private fun ShortcutInfo.toAppShortcut(): AppShortcut? {
        val label = (longLabel ?: shortLabel)?.toString()?.trim().orEmpty()
        if (label.isEmpty()) return null
        val appLabel = runCatching {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(`package`, 0),
            ).toString()
        }.getOrDefault(`package`)

        return AppShortcut(id = id, packageName = `package`, label = label, appLabel = appLabel)
    }

    private fun ApplicationInfo.isSystemApp(): Boolean =
        (flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
}
