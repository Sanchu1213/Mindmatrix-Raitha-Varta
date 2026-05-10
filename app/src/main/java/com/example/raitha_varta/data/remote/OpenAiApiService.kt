package com.example.raitha_varta.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@Serializable
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiResponse(
    val choices: List<OpenAiChoice>
)

@Serializable
data class OpenAiChoice(
    val message: OpenAiMessage
)

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun getCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse

    companion object {
        const val BASE_URL = "https://api.openai.com/"
    }
}
