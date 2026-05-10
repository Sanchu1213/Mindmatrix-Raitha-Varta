package com.example.raitha_varta.data

import com.example.raitha_varta.data.remote.WeatherApiService
import com.example.raitha_varta.data.remote.WeatherResponse
import com.example.raitha_varta.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {
    suspend fun getWeather(city: String): Result<WeatherResponse> {
        return try {
            val response = weatherApiService.getWeather(city = city, apiKey = BuildConfig.WEATHER_API_KEY)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
