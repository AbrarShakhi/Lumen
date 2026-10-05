package com.abrarshakhi.lumen.core.platform.intent

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.platform.app.LauncherAppsDataSource
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidIntentLauncher(
    private val context: Context,
    private val launcherApps: LauncherAppsDataSource,
) : IntentLauncher {

    override suspend fun launch(intent: PlatformIntent): Boolean =
        withContext(Dispatchers.Main.immediate) {
            if (intent is PlatformIntent.AppShortcut) {
                return@withContext launcherApps.startShortcut(intent.packageName, intent.shortcutId)
            }

            val androidIntent = intent.toAndroidIntent()
            try {
                androidIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(androidIntent)
                true
            } catch (_: ActivityNotFoundException) {
                false
            } catch (_: SecurityException) {
                false
            }
        }

    private fun PlatformIntent.toAndroidIntent(): Intent = when (this) {
        is PlatformIntent.Component -> Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(packageName, activityName)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }

        is PlatformIntent.ViewUri -> Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        is PlatformIntent.ViewDocument -> Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(uri), mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        is PlatformIntent.Dial -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))

        is PlatformIntent.Call -> Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null))

        is PlatformIntent.Sms -> Intent(
            Intent.ACTION_SENDTO,
            Uri.fromParts("smsto", phoneNumber, null),
        ).apply { body?.let { putExtra("sms_body", it) } }

        is PlatformIntent.Share -> Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_TEXT, text)
            },
            null,
        )

        is PlatformIntent.SystemSetting -> Intent(action).apply {
            packageName?.let { data = Uri.fromParts("package", it, null) }
        }

        is PlatformIntent.AppDetails -> Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        )

        is PlatformIntent.Uninstall -> Intent(
            Intent.ACTION_DELETE,
            Uri.fromParts("package", packageName, null),
        )

        is PlatformIntent.AppShortcut -> error("Shortcuts are launched via LauncherApps")

        is PlatformIntent.CalendarEvent -> Intent(Intent.ACTION_VIEW).apply {
            data = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        }
    }
}
