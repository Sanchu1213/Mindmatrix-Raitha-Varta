package com.example.raitha_varta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.data.SettingsRepository
import com.example.raitha_varta.data.WeatherRepository
import com.example.raitha_varta.data.remote.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var currentCity: String = "Bangalore"

    init {
        // Observe preferred city and fetch weather whenever it changes
        viewModelScope.launch {
            settingsRepository.preferredCity
                .distinctUntilChanged()
                .collect { city ->
                    currentCity = city
                    fetchWeather(city)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchWeather(city: String = currentCity) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val result = withContext(Dispatchers.IO) {
                weatherRepository.getWeather(city)
            }
            result
                .onSuccess {
                    _uiState.value = WeatherUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = WeatherUiState.Error(it.message ?: "Failed to fetch weather. Check your city name or API key.")
                }
        }
    }
}
