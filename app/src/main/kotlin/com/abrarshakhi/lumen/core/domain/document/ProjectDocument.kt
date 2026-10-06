package com.abrarshakhi.lumen.core.domain.document

enum class DocumentFormat { Markdown, PlainText }

enum class ProjectDocument(
    val fileName: String,
    val projectPath: String,
    val format: DocumentFormat,
) {
    About("ABOUT.md", "docs/ABOUT.md", DocumentFormat.Markdown),
    Credits("CREDITS.md", "docs/CREDITS.md", DocumentFormat.Markdown),
    Terms("TERMS.md", "docs/TERMS.md", DocumentFormat.Markdown),
    Privacy("PRIVACY.md", "docs/PRIVACY.md", DocumentFormat.Markdown),
    License("LICENSE", "LICENSE", DocumentFormat.PlainText),
}

interface DocumentRepository {
    suspend fun load(document: ProjectDocument): String
}
