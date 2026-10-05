package com.abrarshakhi.lumen.surface.launcher

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.getSystemService

class LauncherRole(
    private val context: Context,
) {

    private val aliasComponent = ComponentName(
        context.packageName,
        "com.abrarshakhi.lumen.surface.launcher.HomeAlias",
    )

    fun isEnabled(): Boolean =
        context.packageManager.getComponentEnabledSetting(aliasComponent) ==
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED

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
            PackageManager.DONT_KILL_APP,
        )
    }

    fun chooseHomeAppIntent(): Intent? {
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

    private fun homeSettingsIntent(): Intent? {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        return intent.takeIf { it.resolveActivity(context.packageManager) != null }
            ?: Intent(Settings.ACTION_SETTINGS).takeIf {
                it.resolveActivity(context.packageManager) != null
            }
    }
}
