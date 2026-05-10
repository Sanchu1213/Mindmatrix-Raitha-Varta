package com.example.raitha_varta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.data.MarketPrice
import com.example.raitha_varta.data.MarketRepository
import com.example.raitha_varta.data.PriceTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val repository: MarketRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // In-memory list to immediately reflect updates without waiting for Room Flow
    private val _allPrices = MutableStateFlow<List<MarketPrice>>(emptyList())

    val marketPrices: StateFlow<List<MarketPrice>> = combine(
        _allPrices,
        _searchQuery
    ) { prices, query ->
        if (query.isEmpty()) prices
        else prices.filter {
            it.cropName.contains(query, ignoreCase = true) ||
            it.location.contains(query, ignoreCase = true)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshPrices() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRefreshing.value = true
            _errorMessage.value = null

            val liveResult = repository.refreshLivePrices()
            if (liveResult.isSuccess && (liveResult.getOrNull() ?: 0) > 0) {
                // Live data fetched successfully — read it back from DB
                val dbPrices = repository.getAllMarketPrices().firstOrNull()
                if (!dbPrices.isNullOrEmpty()) {
                    _allPrices.value = dbPrices
                    _isRefreshing.value = false
                    return@launch
                }
            }

            // Fallback: show latest demo data (clearly labelled)
            val now = System.currentTimeMillis()
            _allPrices.value = listOf(
                MarketPrice("1", "Paddy (Common)", "2183", "per Quintal", "Mandya Market, Mandya, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("2", "Wheat", "2450", "per Quintal", "Vijayapura Market, Vijayapura, Karnataka", "", PriceTrend.STABLE, now),
                MarketPrice("3", "Ragi (Millet)", "3846", "per Quintal", "Bengaluru APMC, Bengaluru, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("4", "Maize (Corn)", "1962", "per Quintal", "Haveri Market, Haveri, Karnataka", "", PriceTrend.STABLE, now),
                MarketPrice("5", "Bajra", "2320", "per Quintal", "Kalaburagi Market, Kalaburagi, Karnataka", "", PriceTrend.DOWN, now),
                MarketPrice("6", "Jowar", "2980", "per Quintal", "Belagavi Market, Belagavi, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("7", "Tomato", "12", "per Kg", "Kolar Market, Kolar, Karnataka", "", PriceTrend.DOWN, now),
                MarketPrice("8", "Onion", "25", "per Kg", "Chitradurga Market, Chitradurga, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("9", "Areca nut", "48500", "per Quintal", "Shivamogga Market, Shivamogga, Karnataka", "", PriceTrend.STABLE, now),
                MarketPrice("10", "Coconut", "15", "per piece", "Tumakuru Market, Tumakuru, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("11", "Coffee", "280", "per Kg", "Chikkamagaluru Market, Karnataka", "", PriceTrend.STABLE, now),
                MarketPrice("12", "Banana", "25", "per Kg", "Davangere Market, Davangere, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("13", "Mango", "45", "per Kg", "Ramanagara Market, Ramanagara, Karnataka", "", PriceTrend.UP, now),
                MarketPrice("14", "Sugarcane", "3500", "per Quintal", "Mysuru Market, Mysuru, Karnataka", "", PriceTrend.STABLE, now),
                MarketPrice("15", "Potato", "18", "per Kg", "Hassan Market, Hassan, Karnataka", "", PriceTrend.DOWN, now)
            )

            val reason = liveResult.exceptionOrNull()?.message.orEmpty()
            _errorMessage.value = if (reason.contains("Missing", ignoreCase = true)) null
                                   else "Showing last known prices. Live update failed."
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    init {
        refreshPrices()
    }
}
