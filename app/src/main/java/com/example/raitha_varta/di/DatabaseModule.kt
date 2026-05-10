package com.example.raitha_varta.di

import android.content.Context
import androidx.room.Room
import com.example.raitha_varta.data.AppDatabase
import com.example.raitha_varta.data.MarketDao
import com.example.raitha_varta.data.NewsDao
import com.example.raitha_varta.data.SchemeDao
import com.example.raitha_varta.data.TipDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "raitha_varta_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideNewsDao(database: AppDatabase): NewsDao {
        return database.newsDao()
    }

    @Provides
    fun provideMarketDao(database: AppDatabase): MarketDao {
        return database.marketDao()
    }

    @Provides
    fun provideSchemeDao(database: AppDatabase): SchemeDao {
        return database.schemeDao()
    }

    @Provides
    fun provideTipDao(database: AppDatabase): TipDao {
        return database.tipDao()
    }

    @Provides
    fun provideConsultationDao(database: AppDatabase): com.example.raitha_varta.data.ConsultationDao {
        return database.consultationDao()
    }
}
