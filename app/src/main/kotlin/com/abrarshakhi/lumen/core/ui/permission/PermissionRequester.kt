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

@Composable
fun rememberPermissionRequester(
    onResult: (Map<AppPermission, Boolean>) -> Unit = {},
): (List<AppPermission>) -> Unit {
    val activity = LocalActivity.current
    val recorder = koinInject<PermissionRequestRecorder>()
    val currentOnResult by rememberUpdatedState(onResult)

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
