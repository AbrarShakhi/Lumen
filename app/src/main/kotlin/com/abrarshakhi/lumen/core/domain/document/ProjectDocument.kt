package com.abrarshakhi.lumen.core.domain.document

enum class DocumentFormat { Markdown, PlainText }

enum class ProjectDocument(val fileName: String, val format: DocumentFormat) {
    About("ABOUT.md", DocumentFormat.Markdown),
    Credits("CREDITS.md", DocumentFormat.Markdown),
    Terms("TERMS.md", DocumentFormat.Markdown),
    Privacy("PRIVACY.md", DocumentFormat.Markdown),
    License("LICENSE", DocumentFormat.PlainText),
}

interface DocumentRepository {
    suspend fun load(document: ProjectDocument): String
}
