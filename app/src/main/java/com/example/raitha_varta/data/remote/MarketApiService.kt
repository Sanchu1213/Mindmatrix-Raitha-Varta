package com.example.raitha_varta.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Live mandi prices (Agmarknet) via data.gov.in.
 * Requires an API key configured at build time.
 */
interface MarketApiService {

    @GET("resource/{resourceId}")
    suspend fun fetchMandiPrices(
        @Path("resourceId") resourceId: String,
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 200,
        @Query("offset") offset: Int = 0,
        @Query("filters[state.keyword]") state: String? = null
    ): MarketApiResponse

    companion object {
        const val BASE_URL = "https://api.data.gov.in/"
    }
}

@Serializable
data class MarketApiResponse(
    val records: List<MandiRecord> = emptyList()
)

@Serializable
data class MandiRecord(
    @SerialName("commodity") val commodity: String? = null,
    @SerialName("state") val state: String? = null,
    @SerialName("district") val district: String? = null,
    @SerialName("market") val market: String? = null,
    @SerialName("variety") val variety: String? = null,
    @SerialName("grade") val grade: String? = null,
    @SerialName("arrival_date") val arrivalDate: String? = null,
    @SerialName("min_price") val minPrice: Double? = null,
    @SerialName("max_price") val maxPrice: Double? = null,
    @SerialName("modal_price") val modalPrice: Double? = null
)

