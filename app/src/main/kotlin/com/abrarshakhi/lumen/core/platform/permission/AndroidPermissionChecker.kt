package com.abrarshakhi.lumen.core.platform.permission

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.permission.PermissionRequestRecorder

/**
 * Reads runtime permission state.
 *
 * "Permanently denied" is not directly observable on Android — the system only exposes
 * `shouldShowRequestPermissionRationale`, which requires an Activity and is ambiguous
 * before the first ask. Lumen therefore records that it has asked, and treats
 * "asked before, still not granted, no rationale" as permanent denial.
 */
class AndroidPermissionChecker(
    private val context: Context,
    private val askHistory: SharedPreferences,
) : PermissionChecker, PermissionRequestRecorder {

    override val sdkInt: Int = Build.VERSION.SDK_INT

    override fun isGranted(permission: AppPermission): Boolean {
        if (!permission.appliesTo(sdkInt)) return true
        return ContextCompat.checkSelfPermission(context, permission.manifestName) ==
            PackageManager.PERMISSION_GRANTED
    }

    override fun isPermanentlyDenied(permission: AppPermission): Boolean =
        !isGranted(permission) && wasAsked(permission) && !shouldShowRationale(permission)

    override fun recordAsked(permission: AppPermission, canAskAgain: Boolean) {
        askHistory.edit {
            putBoolean(askedKey(permission), true)
            putBoolean(rationaleKey(permission), canAskAgain)
        }
    }

    private fun wasAsked(permission: AppPermission): Boolean =
        askHistory.getBoolean(askedKey(permission), false)

    private fun shouldShowRationale(permission: AppPermission): Boolean =
        askHistory.getBoolean(rationaleKey(permission), false)

    private fun askedKey(permission: AppPermission) = "asked:${permission.name}"
    private fun rationaleKey(permission: AppPermission) = "rationale:${permission.name}"

    companion object {
        const val PREFS_NAME = "permission_history"
    }
}
