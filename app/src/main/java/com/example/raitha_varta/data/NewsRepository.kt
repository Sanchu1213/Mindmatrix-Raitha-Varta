package com.example.raitha_varta.data

import com.example.raitha_varta.data.remote.NewsApiService
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsDao: NewsDao,
    private val newsApiService: NewsApiService
) {
    fun getAllArticles(): Flow<List<NewsArticle>> = newsDao.getAllArticles()

    fun getBookmarkedArticles(): Flow<List<NewsArticle>> = newsDao.getBookmarkedArticles()

    suspend fun refreshNews() {
        try {
            // Note: In a real app, the API key should be in a secure location (e.g., BuildConfig)
            val response = newsApiService.getNews(apiKey = "YOUR_API_KEY")
            if (response.status == "ok") {
                val articles = response.articles.map { dto ->
                    NewsArticle(
                        id = dto.url,
                        title = dto.title,
                        description = dto.description ?: "",
                        content = dto.content ?: "",
                        imageUrl = dto.urlToImage,
                        publishedAt = parseDate(dto.publishedAt),
                        source = dto.source.name,
                        category = "General"
                    )
                }
                newsDao.insertArticles(articles)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseDate(dateString: String): Long {
        return try {
            // NewsAPI format: 2024-03-20T10:00:00Z
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    suspend fun toggleBookmark(article: NewsArticle) {
        newsDao.updateArticle(article.copy(isBookmarked = !article.isBookmarked))
    }

    suspend fun clearNonBookmarked() = newsDao.clearNonBookmarked()
}
