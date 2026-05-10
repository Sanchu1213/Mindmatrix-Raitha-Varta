package com.example.raitha_varta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticle(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String?,
    val publishedAt: Long,
    val source: String,
    val category: String = "General",
    val isBookmarked: Boolean = false
)
