package com.abrarshakhi.lumen.core.domain.document

data class InlineRun(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val url: String? = null,
)

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val content: List<InlineRun>) : MarkdownBlock
    data class Paragraph(val content: List<InlineRun>) : MarkdownBlock
    data class ListBlock(val ordered: Boolean, val items: List<List<InlineRun>>) : MarkdownBlock
    data class Table(val header: List<List<InlineRun>>, val rows: List<List<List<InlineRun>>>) : MarkdownBlock
    data class Code(val text: String) : MarkdownBlock
    data object Rule : MarkdownBlock
}

object MarkdownParser {

    fun parse(document: String, format: DocumentFormat): List<MarkdownBlock> = when (format) {
        DocumentFormat.Markdown -> parseMarkdown(document)
        DocumentFormat.PlainText -> parsePlainText(document)
    }

    fun parsePlainText(source: String): List<MarkdownBlock> =
        source.normalizeLineEndings()
            .split(BLANK_LINES)
            .map { paragraph -> paragraph.lines().joinToString(" ") { it.trim() }.trim() }
            .filter { it.isNotEmpty() }
            .map { MarkdownBlock.Paragraph(parseInline(it, autolinkOnly = true)) }

    fun parseMarkdown(source: String): List<MarkdownBlock> {
        val lines = source.normalizeLineEndings().lines()
        val blocks = mutableListOf<MarkdownBlock>()
        val paragraph = mutableListOf<String>()
        var index = 0

        fun flushParagraph() {
            if (paragraph.isNotEmpty()) {
                blocks += MarkdownBlock.Paragraph(parseInline(paragraph.joinToString(" ") { it.trim() }))
                paragraph.clear()
            }
        }

        while (index < lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> {
                    flushParagraph()
                    index++
                }

                trimmed.startsWith(FENCE) -> {
                    flushParagraph()
                    val code = mutableListOf<String>()
                    index++
                    while (index < lines.size && !lines[index].trim().startsWith(FENCE)) {
                        code += lines[index]
                        index++
                    }
                    index++
                    blocks += MarkdownBlock.Code(code.joinToString("\n"))
                }

                HEADING.matches(trimmed) -> {
                    flushParagraph()
                    val match = HEADING.matchEntire(trimmed)!!
                    blocks += MarkdownBlock.Heading(
                        level = match.groupValues[1].length,
                        content = parseInline(match.groupValues[2].trim()),
                    )
                    index++
                }

                RULE.matches(trimmed) && paragraph.isEmpty() -> {
                    blocks += MarkdownBlock.Rule
                    index++
                }

                trimmed.startsWith("|") -> {
                    flushParagraph()
                    val tableLines = mutableListOf<String>()
                    while (index < lines.size && lines[index].trim().startsWith("|")) {
                        tableLines += lines[index].trim()
                        index++
                    }
                    blocks += parseTable(tableLines)
                }

                LIST_ITEM.matches(line) -> {
                    flushParagraph()
                    val ordered = ORDERED_ITEM.matches(line)
                    val items = mutableListOf<MutableList<String>>()
                    while (index < lines.size) {
                        val current = lines[index]
                        val match = LIST_ITEM.matchEntire(current)
                        when {
                            match != null -> items += mutableListOf(match.groupValues[2])
                            current.isNotBlank() && current.startsWith(" ") && items.isNotEmpty() ->
                                items.last() += current.trim()
                            else -> break
                        }
                        index++
                    }
                    blocks += MarkdownBlock.ListBlock(
                        ordered = ordered,
                        items = items.map { parseInline(it.joinToString(" ") { part -> part.trim() }) },
                    )
                }

                line.startsWith(INDENT) && paragraph.isEmpty() -> {
                    val code = mutableListOf<String>()
                    while (index < lines.size && (lines[index].startsWith(INDENT) || lines[index].isBlank())) {
                        code += lines[index].removePrefix(INDENT)
                        index++
                    }
                    blocks += MarkdownBlock.Code(code.joinToString("\n").trimEnd())
                }

                else -> {
                    paragraph += line
                    index++
                }
            }
        }
        flushParagraph()
        return blocks
    }

    fun parseInline(text: String, autolinkOnly: Boolean = false): List<InlineRun> {
        val runs = mutableListOf<InlineRun>()
        val buffer = StringBuilder()
        var bold = false
        var italic = false
        var index = 0

        fun flush() {
            if (buffer.isNotEmpty()) {
                runs += InlineRun(buffer.toString(), bold = bold, italic = italic)
                buffer.clear()
            }
        }

        while (index < text.length) {
            val char = text[index]
            val autolink = AUTOLINK.matchAt(text, index)
            when {
                autolink != null && (index == 0 || !text[index - 1].isLetterOrDigit()) -> {
                    flush()
                    val url = autolink.value.trimEnd(*TRAILING_PUNCTUATION)
                    runs += InlineRun(url, bold = bold, italic = italic, url = url)
                    index += url.length
                }

                autolinkOnly -> {
                    buffer.append(char)
                    index++
                }

                char == '\\' && index + 1 < text.length -> {
                    buffer.append(text[index + 1])
                    index += 2
                }

                char == '`' -> {
                    val end = text.indexOf('`', index + 1)
                    if (end == -1) {
                        buffer.append(char)
                        index++
                    } else {
                        flush()
                        runs += InlineRun(text.substring(index + 1, end), code = true)
                        index = end + 1
                    }
                }

                text.startsWith("**", index) -> {
                    flush()
                    bold = !bold
                    index += 2
                }

                char == '*' || (char == '_' && (index == 0 || !text[index - 1].isLetterOrDigit())) ||
                    (char == '_' && italic) -> {
                    flush()
                    italic = !italic
                    index++
                }

                char == '[' -> {
                    val link = LINK.matchAt(text, index)
                    if (link == null) {
                        buffer.append(char)
                        index++
                    } else {
                        flush()
                        runs += InlineRun(
                            text = link.groupValues[1],
                            bold = bold,
                            italic = italic,
                            url = link.groupValues[2],
                        )
                        index += link.value.length
                    }
                }

                char == '<' && AUTOLINK.matchAt(text, index + 1) != null -> {
                    index++
                }

                char == '>' && index > 0 && runs.lastOrNull()?.url != null && buffer.isEmpty() -> {
                    index++
                }

                else -> {
                    buffer.append(char)
                    index++
                }
            }
        }
        flush()
        return runs
    }

    private fun parseTable(lines: List<String>): MarkdownBlock.Table {
        val rows = lines
            .filterNot { TABLE_SEPARATOR.matches(it) }
            .map { line ->
                line.trim().removePrefix("|").removeSuffix("|")
                    .split("|")
                    .map { cell -> parseInline(cell.trim()) }
            }
        return MarkdownBlock.Table(header = rows.firstOrNull().orEmpty(), rows = rows.drop(1))
    }

    private fun String.normalizeLineEndings(): String = replace("\r\n", "\n").replace('\r', '\n')

    private const val FENCE = "```"
    private const val INDENT = "    "
    private val BLANK_LINES = Regex("\n\\s*\n")
    private val HEADING = Regex("^(#{1,6})\\s+(.+?)\\s*#*$")
    private val RULE = Regex("^(-{3,}|\\*{3,}|_{3,})$")
    private val LIST_ITEM = Regex("^\\s{0,3}([-*+]|\\d+[.)])\\s+(.*)$")
    private val ORDERED_ITEM = Regex("^\\s{0,3}\\d+[.)]\\s+.*$")
    private val TABLE_SEPARATOR = Regex("^\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)*\\|?$")
    private val LINK = Regex("\\[([^\\]]+)]\\(([^)\\s]+)\\)")
    private val AUTOLINK = Regex("https?://[^\\s<>)\\]]+")
    private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ';', ':', '!', '?', '\'', '"')
}
