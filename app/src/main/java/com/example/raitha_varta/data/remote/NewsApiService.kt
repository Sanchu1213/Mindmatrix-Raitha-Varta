package com.example.raitha_varta.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getNews(
        @Query("q") query: String = "agriculture farming India",
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponse

    companion object {
        const val BASE_URL = "https://newsapi.org/"
    }
}
