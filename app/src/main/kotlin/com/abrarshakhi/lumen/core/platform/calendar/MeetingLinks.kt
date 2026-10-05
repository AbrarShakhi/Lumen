package com.abrarshakhi.lumen.core.platform.calendar

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
