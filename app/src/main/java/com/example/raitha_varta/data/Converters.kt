package com.example.raitha_varta.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPriceTrend(value: PriceTrend): String {
        return value.name
    }

    @TypeConverter
    fun toPriceTrend(value: String): PriceTrend {
        return PriceTrend.valueOf(value)
    }
}
