package com.example.raitha_varta.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchemeRepository @Inject constructor(
    private val schemeDao: SchemeDao
) {
    fun getAllSchemes(): Flow<List<Scheme>> = schemeDao.getAllSchemes()

    fun getSchemesByCategory(category: String): Flow<List<Scheme>> = schemeDao.getSchemesByCategory(category)

    suspend fun insertSchemes(schemes: List<Scheme>) = schemeDao.insertSchemes(schemes)

    suspend fun clearAll() = schemeDao.clearAll()
}
