package com.abrarshakhi.lumen.feature.about

import com.abrarshakhi.lumen.core.domain.document.DocumentRepository
import com.abrarshakhi.lumen.core.domain.document.MarkdownBlock
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeRepository(var source: String? = null) : DocumentRepository {
        override suspend fun load(document: ProjectDocument): String =
            source ?: throw FileNotFoundException(document.fileName)
    }

    @Test
    fun `a loaded document drops its title heading`() = runTest {
        val viewModel = DocumentViewModel(
            ProjectDocument.Privacy,
            FakeRepository("# Privacy Policy\n\n## Summary\n\nNothing leaves the device."),
        )
        advanceUntilIdle()

        val content = assertIs<DocumentContent.Ready>(viewModel.state.value.content)
        assertEquals(2, content.blocks.size)
        assertIs<MarkdownBlock.Heading>(content.blocks.first())
    }

    @Test
    fun `a missing document can be retried`() = runTest {
        val repository = FakeRepository()
        val viewModel = DocumentViewModel(ProjectDocument.License, repository)
        advanceUntilIdle()
        assertEquals(DocumentContent.Unavailable, viewModel.state.value.content)

        repository.source = "Apache License"
        viewModel.dispatch(DocumentIntent.Load)
        advanceUntilIdle()

        assertIs<DocumentContent.Ready>(viewModel.state.value.content)
    }
}
