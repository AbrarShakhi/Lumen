package com.abrarshakhi.lumen.core.domain.search

enum class ResultCategory(val weight: Float, val displayOrder: Int) {
    System(weight = 0.00f, displayOrder = 0),

    Answer(weight = 1.30f, displayOrder = 10),

    App(weight = 1.00f, displayOrder = 20),
    Shortcut(weight = 0.95f, displayOrder = 30),
    Contact(weight = 0.90f, displayOrder = 40),
    Note(weight = 0.85f, displayOrder = 50),
    File(weight = 0.80f, displayOrder = 60),
    CalendarEvent(weight = 0.80f, displayOrder = 70),
    Setting(weight = 0.75f, displayOrder = 80),

    WebSearch(weight = 0.60f, displayOrder = 90),
}
