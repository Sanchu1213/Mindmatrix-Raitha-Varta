package com.example.raitha_varta.data

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local Room entity: advisory cards with English and Kannada copy. */
@Entity(tableName = "tips")
data class Tip(
    @PrimaryKey val id: String,
    val cropId: String,
    val cropName: String,
    val category: String,
    val instructionEn: String,
    val instructionKn: String,
    @DrawableRes val imageRes: Int,
    val stage: String = "",
    val weather: String = "",
    val action: String = "",
    val reason: String = "",
    val medicinalBenefit: String = "",
    val farmingMethod: String = "",
    val priority: String = "Normal",
    val createdAt: Long = System.currentTimeMillis()
)
