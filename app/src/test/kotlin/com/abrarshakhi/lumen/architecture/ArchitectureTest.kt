package com.abrarshakhi.lumen.architecture

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ArchitectureTest {

    private data class Rule(
        val description: String,
        val appliesTo: (String) -> Boolean,
        val forbiddenImports: List<String>,
    )

    private val rules = listOf(
        Rule(
            description = "core/domain must stay pure Kotlin (no Android, Compose, Room, Ktor or DataStore)",
            appliesTo = { it.startsWith("core/domain/") && !it.startsWith("core/domain/di/") },
            forbiddenImports = listOf(
                "android.",
                "androidx.compose.",
                "androidx.room.",
                "androidx.datastore.",
                "io.ktor.",
                "kotlinx.serialization.",
            ),
        ),
        Rule(
            description = "core/mvi may depend on coroutines and lifecycle only",
            appliesTo = { it.startsWith("core/mvi/") },
            forbiddenImports = listOf("android.", "androidx.compose.", "androidx.room.", "io.ktor."),
        ),
        Rule(
            description = "the data layer must not reach into the UI",
            appliesTo = { it.startsWith("core/data/") },
            forbiddenImports = listOf("androidx.compose.", "com.abrarshakhi.lumen.feature.", "com.abrarshakhi.lumen.app."),
        ),
        Rule(
            description = "the platform layer must not reach into the UI",
            appliesTo = { it.startsWith("core/platform/") },
            forbiddenImports = listOf("androidx.compose.", "com.abrarshakhi.lumen.feature.", "com.abrarshakhi.lumen.app."),
        ),
        Rule(
            description = "providers must not depend on features or on each other",
            appliesTo = { it.startsWith("provider/") },
            forbiddenImports = listOf("com.abrarshakhi.lumen.feature."),
        ),
        Rule(
            description = "launch surfaces mount the app shell; they must not know about providers",
            appliesTo = { it.startsWith("surface/") },
            forbiddenImports = listOf("com.abrarshakhi.lumen.provider."),
        ),
        Rule(
            description = "core must never depend on features or providers",
            appliesTo = { it.startsWith("core/") },
            forbiddenImports = listOf(
                "com.abrarshakhi.lumen.feature.",
                "com.abrarshakhi.lumen.provider.",
            ),
        ),
    )

    @Test
    fun `layer dependencies point in one direction only`() {
        val violations = mutableListOf<String>()

        sourceFiles().forEach { (relativePath, file) ->
            val imports = file.readLines()
                .filter { it.startsWith("import ") }
                .map { it.removePrefix("import ").trim() }

            rules.filter { it.appliesTo(relativePath) }.forEach { rule ->
                imports.forEach { imported ->
                    if (rule.forbiddenImports.any { imported.startsWith(it) }) {
                        violations += "$relativePath imports '$imported' — ${rule.description}"
                    }
                }
            }
        }

        assertTrue(
            violations.isEmpty(),
            "Architecture violations:\n" + violations.joinToString("\n") { "  - $it" },
        )
    }

    @Test
    fun `no provider imports another provider`() {
        val violations = mutableListOf<String>()

        sourceFiles()
            .filter { (path, _) -> path.startsWith("provider/") }
            .forEach { (relativePath, file) ->
                val ownPackage = relativePath.removePrefix("provider/").substringBefore('/')
                file.readLines()
                    .filter { it.startsWith("import com.abrarshakhi.lumen.provider.") }
                    .forEach { line ->
                        val other = line
                            .removePrefix("import com.abrarshakhi.lumen.provider.")
                            .substringBefore('.')
                        if (other != ownPackage) {
                            violations += "$relativePath imports provider '$other'"
                        }
                    }
            }

        assertTrue(
            violations.isEmpty(),
            "Providers must be independent so each can be added or removed in isolation:\n" +
                violations.joinToString("\n") { "  - $it" },
        )
    }

    @Test
    fun `the search engine does not know about any concrete provider`() {
        val engineSources = sourceFiles()
            .filter { (path, _) -> path.startsWith("core/domain/search/") }

        val leaks = engineSources.filter { (_, file) ->
            file.readText().contains("com.abrarshakhi.lumen.provider.")
        }

        assertTrue(
            leaks.isEmpty(),
            "Search engine references concrete providers: ${leaks.map { it.first }}",
        )
    }

    private fun sourceFiles(): List<Pair<String, File>> {
        val root = File("src/main/kotlin/com/abrarshakhi/lumen")
        check(root.isDirectory) { "Source root not found at ${root.absolutePath}" }
        return root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.relativeTo(root).path to it }
            .toList()
    }
}
