package com.abrarshakhi.lumen.surface.launcher

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * Manages Lumen's opt-in launcher mode.
 *
 * Two steps, because Android requires both: the home `activity-alias` has to be enabled
 * before Lumen can appear in the picker at all, and then the user has to actually choose it.
 * Neither can be done silently, which is appropriate for a change this visible.
 */
class LauncherRole(
    private val context: Context,
) {

    private val aliasComponent = ComponentName(
        context.packageName,
        "com.abrarshakhi.lumen.surface.launcher.HomeAlias",
    )

    /** Whether Lumen currently offers itself as a home app. */
    fun isEnabled(): Boolean =
        context.packageManager.getComponentEnabledSetting(aliasComponent) ==
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED

    /** Whether Lumen is the home app right now. */
    fun isCurrentHome(): Boolean {
        val resolved = context.packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return resolved?.activityInfo?.packageName == context.packageName
    }

    fun setEnabled(enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            aliasComponent,
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            // Not killing the app: disabling our own component would otherwise restart the
            // process out from under the user mid-tap.
            PackageManager.DONT_KILL_APP,
        )
    }

    /**
     * An intent that lets the user choose the home app.
     *
     * Prefers the role dialog, which is one tap. `isRoleAvailable` is checked first because
     * OEM builds — ColorOS among them — do not always expose ROLE_HOME through RoleManager,
     * and `createRequestRoleIntent` on an unavailable role throws.
     */
    fun chooseHomeAppIntent(): Intent? {
        // Requesting a role the app already holds is a no-op — the system shows nothing,
        // so the button appears broken exactly when the user wants to undo. Observed on
        // device. Once Lumen is home, the only way back is the settings screen.
        if (isCurrentHome()) return homeSettingsIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService<RoleManager>()
            if (roleManager != null &&
                runCatching { roleManager.isRoleAvailable(RoleManager.ROLE_HOME) }.getOrDefault(false)
            ) {
                return runCatching {
                    roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                }.getOrNull() ?: homeSettingsIntent()
            }
        }
        return homeSettingsIntent()
    }

    /** Falls back to the system's home-app settings screen, which always exists. */
    private fun homeSettingsIntent(): Intent? {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        return intent.takeIf { it.resolveActivity(context.packageManager) != null }
            ?: Intent(Settings.ACTION_SETTINGS).takeIf {
                it.resolveActivity(context.packageManager) != null
            }
    }
}
