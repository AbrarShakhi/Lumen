package com.abrarshakhi.lumen.core.platform.calendar

/**
 * Finds a joinable meeting link in event text.
 *
 * Meeting URLs turn up in the location field, the description, or neither, depending on
 * which tool created the event — so both are searched, location first because a link there
 * is deliberate rather than incidental.
 */
object MeetingLinks {

    private val PROVIDERS = listOf(
        Regex("""https://meet\.google\.com/[a-z\-]+""", RegexOption.IGNORE_CASE),
        Regex("""https://[\w.\-]*zoom\.us/j/\d+(\?[^\s<>"]*)?""", RegexOption.IGNORE_CASE),
        Regex("""https://teams\.microsoft\.com/l/meetup-join/[^\s<>"]+""", RegexOption.IGNORE_CASE),
        Regex("""https://[\w.\-]*webex\.com/[^\s<>"]+""", RegexOption.IGNORE_CASE),
        Regex("""https://meet\.jit\.si/[^\s<>"]+""", RegexOption.IGNORE_CASE),
    )

    fun find(vararg sources: String?): String? {
        for (source in sources) {
            if (source.isNullOrBlank()) continue
            for (pattern in PROVIDERS) {
                pattern.find(source)?.let { return it.value.trimEnd('.', ',', ')') }
            }
        }
        return null
    }
}
