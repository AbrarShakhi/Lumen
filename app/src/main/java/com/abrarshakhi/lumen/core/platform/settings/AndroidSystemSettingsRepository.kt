package com.abrarshakhi.lumen.core.platform.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.SystemSettingEntry
import com.abrarshakhi.lumen.core.domain.repository.SystemSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The catalogue of settings screens, filtered to those this device actually has.
 *
 * Each candidate is resolved against the package manager once and the surviving set is
 * cached: an entry that cannot be resolved would otherwise appear in results and do nothing
 * when tapped, which is worse than not offering it.
 */
class AndroidSystemSettingsRepository(
    private val context: Context,
) : SystemSettingsRepository {

    @Volatile
    private var cache: List<SystemSettingEntry>? = null

    override suspend fun entries(): List<SystemSettingEntry> {
        cache?.let { return it }
        return withContext(Dispatchers.IO) {
            CANDIDATES
                .filter { candidate -> canResolve(candidate.action) }
                .map { candidate ->
                    val title = context.getString(candidate.titleRes)
                    SystemSettingEntry(
                        id = candidate.action,
                        title = title,
                        action = candidate.action,
                        searchable = SearchableText.of(title),
                        keywords = candidate.keywords.map { SearchableText.of(it) },
                    )
                }
                .also { cache = it }
        }
    }

    private fun canResolve(action: String): Boolean = runCatching {
        Intent(action).resolveActivity(context.packageManager) != null
    }.getOrDefault(false)

    private data class Candidate(
        val action: String,
        val titleRes: Int,
        val keywords: List<String> = emptyList(),
    )

    private companion object {
        val CANDIDATES = listOf(
            Candidate(Settings.ACTION_WIFI_SETTINGS, R.string.setting_wifi, listOf("wifi", "wireless", "internet")),
            Candidate(Settings.ACTION_BLUETOOTH_SETTINGS, R.string.setting_bluetooth, listOf("bt", "pair", "headphones")),
            Candidate(Settings.ACTION_DATA_ROAMING_SETTINGS, R.string.setting_mobile_data, listOf("cellular", "sim", "data", "roaming")),
            Candidate(Settings.ACTION_AIRPLANE_MODE_SETTINGS, R.string.setting_airplane, listOf("flight", "plane")),
            Candidate(Settings.ACTION_WIRELESS_SETTINGS, R.string.setting_hotspot, listOf("tether", "hotspot")),
            Candidate(Settings.ACTION_VPN_SETTINGS, R.string.setting_vpn),
            Candidate(Settings.ACTION_NFC_SETTINGS, R.string.setting_nfc, listOf("contactless", "tap")),
            Candidate(Settings.ACTION_CAST_SETTINGS, R.string.setting_cast, listOf("screen mirror", "chromecast")),
            Candidate(Settings.ACTION_DISPLAY_SETTINGS, R.string.setting_display, listOf("brightness", "screen", "dark mode")),
            Candidate(Settings.ACTION_SOUND_SETTINGS, R.string.setting_sound, listOf("volume", "ringtone", "vibrate")),
            // Not a public constant, but a long-standing action on stock and OEM builds.
            // Safe to list unchecked because every candidate is resolve-tested first: if a
            // device lacks it, the entry is simply dropped rather than failing on tap.
            Candidate("android.settings.NOTIFICATION_SETTINGS", R.string.setting_notifications, listOf("alerts", "badges")),
            Candidate(Settings.ACTION_APPLICATION_SETTINGS, R.string.setting_apps, listOf("applications", "uninstall")),
            Candidate(Settings.ACTION_INTERNAL_STORAGE_SETTINGS, R.string.setting_storage, listOf("space", "free up", "disk")),
            Candidate(Settings.ACTION_BATTERY_SAVER_SETTINGS, R.string.setting_battery, listOf("power", "charging", "saver")),
            Candidate(Settings.ACTION_LOCATION_SOURCE_SETTINGS, R.string.setting_location, listOf("gps", "maps")),
            Candidate(Settings.ACTION_SECURITY_SETTINGS, R.string.setting_security, listOf("lock screen", "pin", "fingerprint")),
            Candidate(Settings.ACTION_PRIVACY_SETTINGS, R.string.setting_privacy, listOf("permissions")),
            Candidate(Settings.ACTION_SYNC_SETTINGS, R.string.setting_accounts, listOf("google account", "sign in")),
            Candidate(Settings.ACTION_LOCALE_SETTINGS, R.string.setting_language, listOf("language", "region")),
            Candidate(Settings.ACTION_INPUT_METHOD_SETTINGS, R.string.setting_keyboard, listOf("ime", "typing")),
            Candidate(Settings.ACTION_DATE_SETTINGS, R.string.setting_date_time, listOf("clock", "timezone")),
            Candidate(Settings.ACTION_ACCESSIBILITY_SETTINGS, R.string.setting_accessibility, listOf("talkback", "font size")),
            Candidate(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS, R.string.setting_default_apps, listOf("browser", "default")),
            Candidate(Settings.ACTION_HOME_SETTINGS, R.string.setting_home_app, listOf("launcher", "home")),
            Candidate(Settings.ACTION_VOICE_INPUT_SETTINGS, R.string.setting_assistant, listOf("voice", "assistant")),
            Candidate(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, R.string.setting_developer, listOf("adb", "usb debugging")),
            Candidate(Settings.ACTION_DEVICE_INFO_SETTINGS, R.string.setting_about, listOf("android version", "imei", "model")),
        )
    }
}
