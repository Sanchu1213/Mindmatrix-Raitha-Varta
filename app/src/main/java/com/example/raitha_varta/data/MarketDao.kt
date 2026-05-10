package com.example.raitha_varta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_prices ORDER BY updatedAt DESC")
    fun getAllMarketPrices(): Flow<List<MarketPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrices(prices: List<MarketPrice>)

    @Query("DELETE FROM market_prices")
    suspend fun clearAll()
}
