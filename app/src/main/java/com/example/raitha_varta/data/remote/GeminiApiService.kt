package com.example.raitha_varta.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)

@Serializable
data class GeminiCandidate(val content: GeminiContent)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/"
    }
}
