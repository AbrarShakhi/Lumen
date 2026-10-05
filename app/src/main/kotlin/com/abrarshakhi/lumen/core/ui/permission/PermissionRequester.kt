package com.abrarshakhi.lumen.core.ui.permission

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.activity.compose.LocalActivity
import androidx.core.app.ActivityCompat
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PermissionRequestRecorder
import org.koin.compose.koinInject

/**
 * Requests runtime permissions and records the outcome.
 *
 * The recording is the point. Android never reports "permanently denied" — it only offers
 * `shouldShowRequestPermissionRationale`, which needs an Activity and means nothing before
 * the first ask. Checking it immediately *after* a denial is what distinguishes "not asked
 * yet" from "denied, don't ask again", and that distinction decides whether the UI offers a
 * prompt or sends the user to system settings.
 *
 * Shared by every screen that asks, so the bookkeeping cannot be forgotten at one call site.
 */
@Composable
fun rememberPermissionRequester(
    onResult: (Map<AppPermission, Boolean>) -> Unit = {},
): (List<AppPermission>) -> Unit {
    val activity = LocalActivity.current
    val recorder = koinInject<PermissionRequestRecorder>()
    val currentOnResult by rememberUpdatedState(onResult)

    // Remembered so the result callback can map manifest names back to what was asked for.
    val requested = remember { mutableListOf<AppPermission>() }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val outcome = requested.associateWith { permission ->
            grants[permission.manifestName] ?: false
        }

        outcome.forEach { (permission, granted) ->
            val canAskAgain = if (granted) {
                true
            } else {
                // False here means the system will not show a dialog for it again.
                (activity as? Activity)?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, permission.manifestName)
                } ?: true
            }
            recorder.recordAsked(permission, canAskAgain)
        }

        currentOnResult(outcome)
    }

    return { permissions ->
        requested.clear()
        requested.addAll(permissions)
        launcher.launch(permissions.map { it.manifestName }.toTypedArray())
    }
}
