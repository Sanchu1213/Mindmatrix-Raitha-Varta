package com.example.raitha_varta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.data.NewsArticle
import com.example.raitha_varta.data.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<NewsUiState> = combine(
        repository.getAllArticles(),
        _selectedCategory,
        _searchQuery
    ) { articles, selectedCategory, query ->
        if (articles.isEmpty() && !_isRefreshing.value) {
            NewsUiState.Loading
        } else {
            val categories = listOf("All") + articles.map { it.category }.distinct().sorted()
            
            val filteredArticles = articles.filter { article ->
                val categoryMatch = selectedCategory == "All" || article.category == selectedCategory
                val searchMatch = article.title.contains(query, ignoreCase = true) || 
                                 article.description.contains(query, ignoreCase = true)
                categoryMatch && searchMatch
            }

            NewsUiState.Success(
                articles = filteredArticles,
                categories = categories,
                selectedCategory = selectedCategory
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsUiState.Loading
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun refreshArticles() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshNews()
            _isRefreshing.value = false
        }
    }

    init {
        refreshArticles()
    }
}
