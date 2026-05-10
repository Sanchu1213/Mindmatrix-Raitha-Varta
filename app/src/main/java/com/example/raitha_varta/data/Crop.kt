package com.example.raitha_varta.data

import androidx.annotation.DrawableRes

data class Crop(
    val id: String,
    val name: String,
    @DrawableRes val imageRes: Int,
    val category: String,
    val season: String,
    val isRecommended: Boolean = false,
    val growthProgress: Float = 0f
)
