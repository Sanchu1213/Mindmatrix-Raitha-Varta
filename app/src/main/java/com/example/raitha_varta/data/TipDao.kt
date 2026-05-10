package com.example.raitha_varta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TipDao {
    @Query("SELECT * FROM tips ORDER BY createdAt DESC")
    fun getAllTips(): Flow<List<Tip>>

    @Query("SELECT * FROM tips WHERE cropId = :cropId ORDER BY createdAt DESC")
    fun getTipsByCrop(cropId: String): Flow<List<Tip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTips(tips: List<Tip>)

    @Query("DELETE FROM tips")
    suspend fun clearAllTips()
}
