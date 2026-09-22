package com.abrarshakhi.lumen.core.platform.permission

import android.content.Context
import android.content.pm.LauncherApps
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import com.abrarshakhi.lumen.core.domain.permission.CapabilityChecker
import com.abrarshakhi.lumen.core.domain.permission.PlatformCapability

/**
 * Reports non-permission platform affordances.
 *
 * [PlatformCapability.DefaultLauncher] is checked via `hasShortcutHostPermission`, which is
 * the authoritative signal: Android grants shortcut access only to the current home-role
 * holder, and there is no partial fallback.
 */
class AndroidCapabilityChecker(
    private val context: Context,
) : CapabilityChecker {

    override fun has(capability: PlatformCapability): Boolean = when (capability) {
        PlatformCapability.DefaultLauncher -> runCatching {
            context.getSystemService<LauncherApps>()?.hasShortcutHostPermission() == true
        }.getOrDefault(false)

        PlatformCapability.Network -> runCatching {
            val manager = context.getSystemService<ConnectivityManager>() ?: return@runCatching false
            val network = manager.activeNetwork ?: return@runCatching false
            manager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }.getOrDefault(false)

        // Populated once SAF tree grants are implemented (file search).
        PlatformCapability.DocumentTreeGrant -> false
    }
}
