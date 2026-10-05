package com.abrarshakhi.lumen.core.domain.text

/**
 * A string the UI will render, expressed without depending on Android resources.
 *
 * The domain layer has no access to `R`, but plenty of domain-owned strings are chrome
 * ("Open", "Copy", "Search the web") that must be localised. [Res] defers that lookup to
 * the UI layer; [Raw] carries text that is already data.
 *
 * Note that a [com.abrarshakhi.lumen.core.domain.search.SearchResult]'s title and subtitle
 * are deliberately plain `String` — a contact's name or a file's name is never localisable.
 */
sealed interface TextValue {

    /** Text that is already data and must not be translated. */
    data class Raw(val value: String) : TextValue

    /** A string resource id, resolved by the UI layer. [args] are format arguments. */
    data class Res(val id: Int, val args: List<Any> = emptyList()) : TextValue

    companion object {
        fun of(value: String): TextValue = Raw(value)
        fun res(id: Int, vararg args: Any): TextValue = Res(id, args.toList())
    }
}
