package com.example.raitha_varta.data

import com.example.raitha_varta.BuildConfig
import com.example.raitha_varta.data.remote.MarketApiService
import com.example.raitha_varta.data.remote.MandiRecord
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepository @Inject constructor(
    private val marketDao: MarketDao,
    private val marketApi: MarketApiService
) {
    fun getAllMarketPrices(): Flow<List<MarketPrice>> = marketDao.getAllMarketPrices()

    suspend fun insertMarketPrices(prices: List<MarketPrice>) = marketDao.insertMarketPrices(prices)

    suspend fun clearAll() = marketDao.clearAll()

    suspend fun refreshLivePrices(
        limit: Int = 200
    ): Result<Int> {
        val apiKey = BuildConfig.DATA_GOV_IN_API_KEY
        val resourceId = BuildConfig.AGMARKNET_RESOURCE_ID
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Missing DATA_GOV_IN_API_KEY"))
        }
        if (resourceId.isBlank()) {
            return Result.failure(IllegalStateException("Missing AGMARKNET_RESOURCE_ID"))
        }

        return runCatching {
            // Fetch without state filter — the API doesn't support filters[state] for Karnataka correctly
            val response = marketApi.fetchMandiPrices(
                resourceId = resourceId,
                apiKey = apiKey,
                limit = limit,
                state = null
            )

            val mapped = response.records
                .filter { it.state?.contains("Karnataka", ignoreCase = true) == true }
                .mapNotNull { it.toMarketPriceOrNull() }
                .distinctBy { it.id }

            if (mapped.isNotEmpty()) {
                marketDao.clearAll()
                marketDao.insertMarketPrices(mapped)
            }
            mapped.size
        }
    }

    private fun MandiRecord.toMarketPriceOrNull(): MarketPrice? {
        val commodityName = commodity?.trim().orEmpty()
        val marketName = market?.trim().orEmpty()
        val districtName = district?.trim().orEmpty()
        val stateName = state?.trim().orEmpty()
        val dateStr = arrivalDate?.trim().orEmpty()
        val modal = modalPrice
        if (commodityName.isBlank() || marketName.isBlank() || modal == null) return null

        val updatedAtMillis = parseArrivalDateMillis(dateStr) ?: System.currentTimeMillis()
        val loc = listOf(marketName, districtName, stateName).filter { it.isNotBlank() }.joinToString(", ")
        val id = "${commodityName.lowercase(Locale.ROOT)}|${loc.lowercase(Locale.ROOT)}|$dateStr"

        val pricing = normalizeUnitAndPrice(
            commodity = commodityName,
            modalPricePerQuintal = modal
        )

        // Determine trend from min/max spread
        val minP = minPrice ?: modal
        val maxP = maxPrice ?: modal
        val spread = maxP - minP
        val trend = when {
            spread > modal * 0.15 -> PriceTrend.UP
            spread < modal * 0.05 -> PriceTrend.STABLE
            else -> PriceTrend.DOWN
        }

        return MarketPrice(
            id = id,
            cropName = commodityName,
            price = pricing.price,
            unit = pricing.unit,
            location = loc.ifBlank { marketName },
            reportDate = dateStr,
            trend = trend,
            updatedAt = updatedAtMillis
        )
    }

    private data class Pricing(val price: String, val unit: String)

    /**
     * Agmarknet prices are in ₹/Quintal. For vegetables present ₹/Kg.
     * Conversion: 1 Quintal = 100 Kg.
     */
    private fun normalizeUnitAndPrice(commodity: String, modalPricePerQuintal: Double): Pricing {
        val isVeg = commodity.lowercase(Locale.ROOT) in setOf(
            "tomato", "onion", "potato", "brinjal", "okra",
            "lady finger", "carrot", "spinach", "cabbage",
            "drumstick", "chilli", "green chilli", "red chilli",
            "beetroot", "cucumbar(kheera)", "cucumber", "amaranthus", "banana"
        )

        return if (isVeg) {
            val perKg = modalPricePerQuintal / 100.0
            Pricing(price = formatMoney(perKg), unit = "per Kg")
        } else {
            Pricing(price = formatMoney(modalPricePerQuintal), unit = "per Quintal")
        }
    }

    private fun formatMoney(value: Double): String {
        return if (value >= 100) {
            value.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }

    private fun parseArrivalDateMillis(date: String): Long? {
        if (date.isBlank()) return null
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = true
            sdf.parse(date)?.time
        } catch (_: Exception) {
            null
        }
    }
}
