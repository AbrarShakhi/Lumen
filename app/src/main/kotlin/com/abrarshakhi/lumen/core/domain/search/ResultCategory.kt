package com.abrarshakhi.lumen.core.domain.search

/**
 * The kind of thing a result represents.
 *
 * Carries two independent numbers, because they answer different questions:
 *
 *  - [weight] biases *ranking within* the merged result set, so that at equal textual
 *    relevance a direct answer beats a link to somewhere else.
 *  - [displayOrder] fixes the *order of sections* on screen, ascending.
 *
 * Deriving the second from the first was tried and was wrong: weights are tuned for
 * relevance and collide once two categories share one, which silently reorders the UI.
 * Section order is a presentation decision and is declared explicitly.
 */
enum class ResultCategory(val weight: Float, val displayOrder: Int) {
    /** Synthetic rows the engine injects, e.g. a permission prompt. First: they explain
     *  why results are missing, so burying them would be pointless. */
    System(weight = 0.00f, displayOrder = 0),

    /** Calculator output, unit conversion — an answer, not a destination. */
    Answer(weight = 1.30f, displayOrder = 10),

    App(weight = 1.00f, displayOrder = 20),
    Shortcut(weight = 0.95f, displayOrder = 30),
    Contact(weight = 0.90f, displayOrder = 40),
    Note(weight = 0.85f, displayOrder = 50),
    File(weight = 0.80f, displayOrder = 60),
    CalendarEvent(weight = 0.80f, displayOrder = 70),
    Setting(weight = 0.75f, displayOrder = 80),

    /** Last: always available, so it should never displace a local match. */
    WebSearch(weight = 0.60f, displayOrder = 90),
}
