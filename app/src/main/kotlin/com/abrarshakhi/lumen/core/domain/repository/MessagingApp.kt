package com.abrarshakhi.lumen.core.domain.repository

enum class MessagingApp(
    val packageName: String,
    val displayName: String,
) {
    WhatsApp("com.whatsapp", "WhatsApp"),
    Telegram("org.telegram.messenger", "Telegram"),
    Signal("org.thoughtcrime.securesms", "Signal");

    fun chatUri(digits: String): String {
        val plain = digits.removePrefix("+")
        return when (this) {
            WhatsApp -> "https://wa.me/$plain"
            Telegram -> "tg://resolve?phone=$plain"
            Signal -> "https://signal.me/#p/+$plain"
        }
    }
}

interface InstalledAppsProbe {
    fun isInstalled(packageName: String): Boolean
}
