package com.example.raitha_varta.ui

import com.example.raitha_varta.data.NewsArticle

sealed interface NewsUiState {
    data object Loading : NewsUiState
    data class Success(
        val articles: List<NewsArticle>,
        val categories: List<String>,
        val selectedCategory: String
    ) : NewsUiState
    data class Error(val message: String) : NewsUiState
}
