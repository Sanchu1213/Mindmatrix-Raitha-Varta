package com.example.raitha_varta.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipRepository @Inject constructor(
    private val tipDao: TipDao
) {
    // Firebase Realtime Database instance for fetching tips
    private val database = FirebaseDatabase.getInstance()
    private val tipsRef = database.getReference("tips")
    fun getAllTips(): Flow<List<Tip>> = tipDao.getAllTips()

    fun getTipsByCrop(cropId: String): Flow<List<Tip>> = tipDao.getTipsByCrop(cropId)

    suspend fun insertTips(tips: List<Tip>) = tipDao.insertTips(tips)

    suspend fun clearAllTips() = tipDao.clearAllTips()

    suspend fun fetchTipsFromFirebase() = withContext(Dispatchers.IO) {
        try {
            val snapshot = tipsRef.get().await()
            val fetchedTips = mutableListOf<Tip>()
            
            for (child in snapshot.children) {
                try {
                    val tipId = child.key ?: continue
                    val cropId = child.child("cropId").getValue(String::class.java) ?: continue
                    val cropName = child.child("cropName").getValue(String::class.java) ?: ""
                    val category = child.child("category").getValue(String::class.java) ?: "Daily Tip"
                    val instructionEn = child.child("instructionEn").getValue(String::class.java) ?: ""
                    val instructionKn = child.child("instructionKn").getValue(String::class.java) ?: ""
                    val stage = child.child("stage").getValue(String::class.java) ?: ""
                    val weather = child.child("weather").getValue(String::class.java) ?: ""
                    val action = child.child("action").getValue(String::class.java) ?: ""
                    val reason = child.child("reason").getValue(String::class.java) ?: ""
                    val priority = child.child("priority").getValue(String::class.java) ?: "Normal"
                    
                    val tip = Tip(
                        id = tipId,
                        cropId = cropId,
                        cropName = cropName,
                        category = category,
                        instructionEn = instructionEn,
                        instructionKn = instructionKn,
                        imageRes = 0, // Assigned correctly in ViewModel via cropImageFor
                        stage = stage,
                        weather = weather,
                        action = action,
                        reason = reason,
                        priority = priority
                    )
                    fetchedTips.add(tip)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            if (fetchedTips.isNotEmpty()) {
                clearAllTips()
                insertTips(fetchedTips)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
