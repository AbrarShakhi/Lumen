package com.abrarshakhi.lumen.provider.ai

import com.abrarshakhi.lumen.core.domain.ai.AiAnswer
import com.abrarshakhi.lumen.core.domain.ai.AiFailure
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Pins the wire contract and the failure mapping, with `MockEngine` standing in for the
 * service. This is where an AI integration actually breaks — a changed response shape or an
 * unhandled status code — and none of it needs a real API key.
 */
class GeminiBackendTest {

    private fun backend(engine: MockEngine) = GeminiBackend(
        client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        },
        baseUrl = "",
    )

    private fun jsonEngine(body: String, status: HttpStatusCode = HttpStatusCode.OK) = MockEngine {
        respond(
            content = body,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    @Test
    fun `a successful answer is extracted`() = runTest {
        val body = """{"candidates":[{"content":{"parts":[{"text":"42"}],"role":"model"}}]}"""

        val answer = backend(jsonEngine(body)).answer("meaning of life", "key")

        assertEquals("42", assertIs<AiAnswer.Text>(answer).content)
    }

    @Test
    fun `multi-part answers are joined`() = runTest {
        val body = """{"candidates":[{"content":{"parts":[{"text":"Hello "},{"text":"world"}]}}]}"""

        assertEquals("Hello world", assertIs<AiAnswer.Text>(backend(jsonEngine(body)).answer("hi", "key")).content)
    }

    @Test
    fun `a blank key never reaches the network`() = runTest {
        var called = false
        val engine = MockEngine { called = true; respond("") }

        val answer = backend(engine).answer("question", "")

        assertEquals(AiFailure.MissingKey, assertIs<AiAnswer.Failed>(answer).reason)
        assertTrue(!called, "no request should be made without a key")
    }

    @Test
    fun `a rejected key is reported as an invalid key`() = runTest {
        for (status in listOf(HttpStatusCode.Unauthorized, HttpStatusCode.BadRequest)) {
            val answer = backend(MockEngine { respondError(status) }).answer("q", "bad-key")
            assertEquals(
                AiFailure.InvalidKey,
                assertIs<AiAnswer.Failed>(answer).reason,
                "status $status should map to InvalidKey",
            )
        }
    }

    @Test
    fun `a denied project is not reported as a bad key`() = runTest {
        // Observed against a real key: 403 PERMISSION_DENIED means the key is fine but its
        // Google project is not allowed. Calling that "invalid key" sends the user off to
        // regenerate a perfectly good one.
        val answer = backend(MockEngine { respondError(HttpStatusCode.Forbidden) }).answer("q", "good-key")

        assertEquals(AiFailure.AccessDenied, assertIs<AiAnswer.Failed>(answer).reason)
    }

    @Test
    fun `a retired model is not reported as a bad key either`() = runTest {
        // Also observed for real: a 404 naming a replacement model. Reporting that as a key
        // problem would be actively misleading.
        val answer = backend(MockEngine { respondError(HttpStatusCode.NotFound) }).answer("q", "good-key")

        assertEquals(AiFailure.Unknown, assertIs<AiAnswer.Failed>(answer).reason)
    }

    @Test
    fun `rate limiting is distinguished from other errors`() = runTest {
        val answer = backend(MockEngine { respondError(HttpStatusCode.TooManyRequests) }).answer("q", "key")

        assertEquals(AiFailure.RateLimited, assertIs<AiAnswer.Failed>(answer).reason)
    }

    @Test
    fun `a server error is not mistaken for a bad key`() = runTest {
        // Telling someone their key is wrong when the service is down sends them to
        // regenerate a perfectly good key.
        val answer = backend(MockEngine { respondError(HttpStatusCode.InternalServerError) }).answer("q", "key")

        assertEquals(AiFailure.Unknown, assertIs<AiAnswer.Failed>(answer).reason)
    }

    @Test
    fun `an empty candidate list is a failure, not an empty answer`() = runTest {
        val answer = backend(jsonEngine("""{"candidates":[]}""")).answer("q", "key")

        assertEquals(AiFailure.Unknown, assertIs<AiAnswer.Failed>(answer).reason)
    }

    @Test
    fun `unknown response fields do not break parsing`() = runTest {
        // Providers add fields without warning; failing on one Lumen ignores would be brittle.
        val body = """{"modelVersion":"x","usageMetadata":{"totalTokenCount":9},
            "candidates":[{"finishReason":"STOP","content":{"parts":[{"text":"ok"}]}}]}"""

        assertEquals("ok", assertIs<AiAnswer.Text>(backend(jsonEngine(body)).answer("q", "key")).content)
    }
}
