package com.example.raitha_varta.di

import com.example.raitha_varta.BuildConfig
import com.example.raitha_varta.data.remote.NewsApiService
import com.example.raitha_varta.data.remote.MarketApiService
import com.example.raitha_varta.data.remote.WeatherApiService
import com.example.raitha_varta.data.remote.GeminiApiService
import com.google.ai.client.generativeai.GenerativeModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsApiService(okHttpClient: OkHttpClient, json: Json): NewsApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(NewsApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(NewsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApiService(okHttpClient: OkHttpClient, json: Json): WeatherApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(WeatherApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(WeatherApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMarketApiService(okHttpClient: OkHttpClient, json: Json): MarketApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(MarketApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(MarketApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(okHttpClient: OkHttpClient, json: Json): GeminiApiService {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(GeminiApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(GeminiApiService::class.java)
    }

}
