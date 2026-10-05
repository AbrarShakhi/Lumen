package com.abrarshakhi.lumen.core.domain.repository

/**
 * A messaging app Lumen can hand a phone number to.
 *
 * Each is reached by a documented deep link rather than an internal component, so this
 * stays stable across their updates. Adding one is a line here plus a `<queries>` entry.
 */
enum class MessagingApp(
    val packageName: String,
    val displayName: String,
) {
    WhatsApp("com.whatsapp", "WhatsApp"),
    Telegram("org.telegram.messenger", "Telegram"),
    Signal("org.thoughtcrime.securesms", "Signal");

    /** [digits] should include the country code; `wa.me` rejects anything else. */
    fun chatUri(digits: String): String {
        val plain = digits.removePrefix("+")
        return when (this) {
            WhatsApp -> "https://wa.me/$plain"
            Telegram -> "tg://resolve?phone=$plain"
            Signal -> "https://signal.me/#p/+$plain"
        }
    }
}

/**
 * Reports whether a package is present.
 *
 * Only answers for packages Lumen declares in `<queries>` — Android 11+ package visibility
 * hides everything else, so a false here can mean "not installed" or "not visible".
 */
interface InstalledAppsProbe {
    fun isInstalled(packageName: String): Boolean
}
