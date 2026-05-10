package com.example.raitha_varta.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE id = :articleId")
    suspend fun getArticleById(articleId: String): NewsArticle?

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("DELETE FROM news_articles WHERE isBookmarked = 0")
    suspend fun clearNonBookmarked()
}
