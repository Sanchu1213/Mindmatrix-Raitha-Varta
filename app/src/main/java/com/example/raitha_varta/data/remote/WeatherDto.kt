package com.example.raitha_varta.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    val name: String, // City name
    val dt: Long // Timestamp
)

@Serializable
data class MainDto(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

@Serializable
data class WeatherDescriptionDto(
    val description: String,
    val icon: String
)
