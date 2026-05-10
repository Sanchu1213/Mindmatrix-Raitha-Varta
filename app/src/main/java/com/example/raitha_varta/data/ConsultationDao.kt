package com.example.raitha_varta.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey val id: String,
    val lastMessage: String,
    val timestamp: Long,
    val status: String
)

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations ORDER BY timestamp DESC")
    fun getAllConsultations(): Flow<List<ConsultationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEntity)

    @Delete
    suspend fun deleteConsultation(consultation: ConsultationEntity)

    @Query("UPDATE consultations SET lastMessage = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("DELETE FROM consultations")
    suspend fun deleteAll()
}
