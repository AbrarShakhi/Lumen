package com.abrarshakhi.lumen.core.domain.search

sealed interface IconSource {

    data class App(val packageName: String, val activityName: String? = null) : IconSource

    data class Vector(val icon: LumenIcon) : IconSource

    data class ContactPhoto(val lookupUri: String, val fallbackInitial: Char) : IconSource

    data class Remote(val url: String) : IconSource

    data class Letter(val text: String, val seedColorArgb: Int? = null) : IconSource

    data class Emoji(val character: String) : IconSource
}

enum class LumenIcon {
    Search, App, Contact, Phone, Message, Chat, File, Folder, Image, Video, Music,
    Calendar, Note, Settings, Web, Calculator, Convert, Clock, Currency,
    Ai, Copy, Share, Open, Delete, Edit, Info, Uninstall, Pin, Hide, Trigger,
    Permission, Warning, History,
}
