package com.abrarshakhi.lumen.core.domain.document

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MarkdownParserTest {

    @Test
    fun `headings carry their level`() {
        val blocks = MarkdownParser.parseMarkdown("# Title\n\n## Section\n\n### Detail")

        assertEquals(listOf(1, 2, 3), blocks.map { assertIs<MarkdownBlock.Heading>(it).level })
        assertEquals("Section", (blocks[1] as MarkdownBlock.Heading).content.joinToString("") { it.text })
    }

    @Test
    fun `wrapped lines join into one paragraph`() {
        val blocks = MarkdownParser.parseMarkdown("First line\nsecond line\n\nNext paragraph")

        assertEquals(2, blocks.size)
        assertEquals("First line second line", (blocks[0] as MarkdownBlock.Paragraph).text())
    }

    @Test
    fun `emphasis code and links become styled runs`() {
        val runs = MarkdownParser.parseInline("**Bold** and *italic* with `code` and [docs](https://example.com).")

        assertTrue(runs.first { it.text == "Bold" }.bold)
        assertTrue(runs.first { it.text == "italic" }.italic)
        assertTrue(runs.first { it.text == "code" }.code)
        assertEquals("https://example.com", runs.first { it.text == "docs" }.url)
    }

    @Test
    fun `bare urls are linked without trailing punctuation`() {
        val runs = MarkdownParser.parseInline("Visit https://github.com/AbrarShakhi/Lumen.")

        val link = runs.single { it.url != null }
        assertEquals("https://github.com/AbrarShakhi/Lumen", link.url)
        assertEquals(".", runs.last().text)
    }

    @Test
    fun `underscores inside words are literal`() {
        val runs = MarkdownParser.parseInline("ic_launcher_round")

        assertEquals(listOf(InlineRun("ic_launcher_round")), runs)
    }

    @Test
    fun `bullet and numbered lists keep their items`() {
        val bullets = MarkdownParser.parseMarkdown("- one\n- two\n  continued")
        val numbers = MarkdownParser.parseMarkdown("1. first\n2. second")

        val list = assertIs<MarkdownBlock.ListBlock>(bullets.single())
        assertFalse(list.ordered)
        assertEquals("two continued", list.items[1].joinToString("") { it.text })
        assertTrue(assertIs<MarkdownBlock.ListBlock>(numbers.single()).ordered)
    }

    @Test
    fun `tables drop the separator row`() {
        val table = assertIs<MarkdownBlock.Table>(
            MarkdownParser.parseMarkdown("| A | B |\n| --- | --- |\n| 1 | 2 |\n| 3 | 4 |").single(),
        )

        assertEquals(listOf("A", "B"), table.header.map { cell -> cell.joinToString("") { it.text } })
        assertEquals(2, table.rows.size)
    }

    @Test
    fun `plain text reflows paragraphs and links urls`() {
        val blocks = MarkdownParser.parsePlainText("Apache License\n   Version 2.0\n\nSee http://www.apache.org/licenses/")

        assertEquals("Apache License Version 2.0", (blocks[0] as MarkdownBlock.Paragraph).text())
        assertTrue((blocks[1] as MarkdownBlock.Paragraph).content.any { it.url == "http://www.apache.org/licenses/" })
    }

    @Test
    fun `bundled project documents parse without leftover markup`() {
        ProjectDocument.entries.forEach { document ->
            val file = File("../${document.fileName}")
            assertTrue(file.isFile, "${document.fileName} is missing from the project root")

            val blocks = MarkdownParser.parse(file.readText(), document.format)
            assertTrue(blocks.isNotEmpty(), "${document.fileName} produced no content")

            val text = blocks.flatMap { it.runs() }.joinToString("") { it.text }
            assertFalse("**" in text, "${document.fileName} leaked bold markers")
            assertFalse("](" in text, "${document.fileName} leaked link syntax")
        }
    }

    private fun MarkdownBlock.Paragraph.text() = content.joinToString("") { it.text }

    private fun MarkdownBlock.runs(): List<InlineRun> = when (this) {
        is MarkdownBlock.Heading -> content
        is MarkdownBlock.Paragraph -> content
        is MarkdownBlock.ListBlock -> items.flatten()
        is MarkdownBlock.Table -> (header + rows.flatten()).flatten()
        is MarkdownBlock.Code -> emptyList()
        MarkdownBlock.Rule -> emptyList()
    }
}
