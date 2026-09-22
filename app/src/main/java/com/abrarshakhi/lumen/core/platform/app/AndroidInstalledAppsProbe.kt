package com.abrarshakhi.lumen.core.platform.app

import android.content.Context
import android.content.pm.PackageManager
import com.abrarshakhi.lumen.core.domain.repository.InstalledAppsProbe

/**
 * Checks package presence, caching the answer.
 *
 * Results are cached because this is consulted while building every contact result, and
 * `getPackageInfo` is an IPC call — doing it per row per keystroke would be wasteful. The
 * cache is dropped when packages change, via [invalidate].
 */
class AndroidInstalledAppsProbe(
    private val context: Context,
) : InstalledAppsProbe {

    private val cache = mutableMapOf<String, Boolean>()

    override fun isInstalled(packageName: String): Boolean = synchronized(cache) {
        cache.getOrPut(packageName) {
            runCatching {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            }.getOrDefault(false)
        }
    }

    fun invalidate() = synchronized(cache) { cache.clear() }
}
