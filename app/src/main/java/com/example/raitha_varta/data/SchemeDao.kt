package com.example.raitha_varta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SchemeDao {
    @Query("SELECT * FROM schemes")
    fun getAllSchemes(): Flow<List<Scheme>>

    @Query("SELECT * FROM schemes WHERE category = :category")
    fun getSchemesByCategory(category: String): Flow<List<Scheme>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchemes(schemes: List<Scheme>)

    @Query("DELETE FROM schemes")
    suspend fun clearAll()
}
