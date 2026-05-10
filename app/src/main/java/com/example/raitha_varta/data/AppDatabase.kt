package com.example.raitha_varta.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [NewsArticle::class, MarketPrice::class, Scheme::class, Tip::class, ConsultationEntity::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
    abstract fun marketDao(): MarketDao
    abstract fun schemeDao(): SchemeDao
    abstract fun tipDao(): TipDao
    abstract fun consultationDao(): ConsultationDao
}
