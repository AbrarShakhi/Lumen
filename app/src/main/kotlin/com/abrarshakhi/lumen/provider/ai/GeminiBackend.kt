package com.abrarshakhi.lumen.provider.ai

import com.abrarshakhi.lumen.core.domain.ai.AiAnswer
import com.abrarshakhi.lumen.core.domain.ai.AiBackend
import com.abrarshakhi.lumen.core.domain.ai.AiBackendId
import com.abrarshakhi.lumen.core.domain.ai.AiFailure
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class GeminiBackend(
    private val client: HttpClient,
    private val model: String = DEFAULT_MODEL,
    private val baseUrl: String = DEFAULT_BASE_URL,
) : AiBackend {

    override val id = AiBackendId("gemini")
    override val displayName = "Gemini"
    override val keyUrl = "https://aistudio.google.com/apikey"

    override suspend fun answer(prompt: String, apiKey: String): AiAnswer {
        if (apiKey.isBlank()) return AiAnswer.Failed(AiFailure.MissingKey)

        return try {
            val response = client.post("$baseUrl/v1beta/models/$model:generateContent") {
                header("x-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(
                    GenerateRequest(
                        contents = listOf(Content(parts = listOf(Part(prompt)))),
                        generationConfig = GenerationConfig(maxOutputTokens = MAX_TOKENS),
                    ),
                )
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val text = response.body<GenerateResponse>()
                        .candidates.orEmpty()
                        .firstOrNull()
                        ?.content?.parts.orEmpty()
                        .mapNotNull { it.text }
                        .joinToString("")
                        .trim()

                    if (text.isEmpty()) {
                        AiAnswer.Failed(AiFailure.Unknown)
                    } else {
                        AiAnswer.Text(text)
                    }
                }

                HttpStatusCode.Unauthorized, HttpStatusCode.BadRequest ->
                    AiAnswer.Failed(AiFailure.InvalidKey)

                HttpStatusCode.Forbidden -> AiAnswer.Failed(AiFailure.AccessDenied)

                HttpStatusCode.TooManyRequests -> AiAnswer.Failed(AiFailure.RateLimited)

                else -> AiAnswer.Failed(AiFailure.Unknown)
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            AiAnswer.Failed(AiFailure.Network)
        } catch (_: Exception) {
            AiAnswer.Failed(AiFailure.Unknown)
        }
    }

    @Serializable
    private data class GenerateRequest(
        val contents: List<Content>,
        val generationConfig: GenerationConfig? = null,
    )

    @Serializable
    private data class Content(val parts: List<Part>, val role: String? = null)

    @Serializable
    private data class Part(val text: String? = null)

    @Serializable
    private data class GenerationConfig(
        @SerialName("maxOutputTokens") val maxOutputTokens: Int,
    )

    @Serializable
    private data class GenerateResponse(val candidates: List<Candidate>? = null)

    @Serializable
    private data class Candidate(val content: Content? = null)

    private companion object {
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com"
        const val DEFAULT_MODEL = "gemini-3.6-flash"

        const val MAX_TOKENS = 512
    }
}
