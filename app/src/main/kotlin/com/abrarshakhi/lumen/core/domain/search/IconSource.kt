package com.abrarshakhi.lumen.core.domain.search

/**
 * Describes an icon without referencing any Android or Compose type.
 *
 * A closed set, rendered by a single composable in the UI layer. Closed is the point:
 * providers choose from these shapes, and exactly one renderer has to know how to draw
 * them, so adding a provider never means touching icon rendering.
 */
sealed interface IconSource {

    /** An installed app's launcher icon, loaded from the package manager. */
    data class App(val packageName: String, val activityName: String? = null) : IconSource

    /** A built-in vector from the app's own icon set. */
    data class Vector(val icon: LumenIcon) : IconSource

    /** A contact photo, falling back to [fallbackInitial] when the contact has none. */
    data class ContactPhoto(val lookupUri: String, val fallbackInitial: Char) : IconSource

    /** A remote image, e.g. a search engine favicon. */
    data class Remote(val url: String) : IconSource

    /** A generated monogram. [seedColorArgb] pins the tint; null derives one from [text]. */
    data class Letter(val text: String, val seedColorArgb: Int? = null) : IconSource

    data class Emoji(val character: String) : IconSource
}

/**
 * Icons the domain layer can name. Mapped to concrete vectors in the UI layer, which
 * keeps Material icon types out of the domain.
 */
enum class LumenIcon {
    Search, App, Contact, Phone, Message, Chat, File, Folder, Image, Video, Music,
    Calendar, Note, Settings, Web, Calculator, Convert, Clock, Currency,
    Ai, Copy, Share, Open, Delete, Edit, Info, Uninstall, Pin, Hide, Trigger,
    Permission, Warning, History,
}
