package com.example.raitha_varta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_prices")
data class MarketPrice(
    @PrimaryKey val id: String,
    val cropName: String,
    val price: String,
    val unit: String,
    /**
     * Human-readable location, e.g. "Bengaluru APMC, Bengaluru, Karnataka".
     */
    val location: String,
    /**
     * Market report date as provided by data source (usually dd/MM/yyyy).
     * Used to show date + day, and for trend calculations.
     */
    val reportDate: String = "",
    val trend: PriceTrend,
    val updatedAt: Long
)

enum class PriceTrend {
    UP, DOWN, STABLE
}
