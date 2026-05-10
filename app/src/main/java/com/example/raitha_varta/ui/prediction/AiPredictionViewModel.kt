package com.example.raitha_varta.ui.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.BuildConfig
import com.example.raitha_varta.data.SettingsRepository
import com.example.raitha_varta.data.WeatherRepository
import com.example.raitha_varta.data.remote.GeminiApiService
import com.example.raitha_varta.data.remote.GeminiRequest
import com.example.raitha_varta.data.remote.GeminiContent
import com.example.raitha_varta.data.remote.GeminiPart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class PredictionType {
    YIELD, MARKET, DISEASE
}

sealed class PredictionUiState {
    object Idle : PredictionUiState()
    object Loading : PredictionUiState()
    data class Result(val prediction: String) : PredictionUiState()
    data class Error(val message: String) : PredictionUiState()
}

@HiltViewModel
class AiPredictionViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun makePrediction(type: PredictionType, cropName: String, city: String, soilType: String = "", languageCode: String = "en") {
        viewModelScope.launch {
            _uiState.value = PredictionUiState.Loading
            kotlinx.coroutines.delay(2000)
            
            val isKn = languageCode == "kn"
            val predictionText = when(type) {
                PredictionType.YIELD -> if (isKn) {
                    "ನಿಮ್ಮ ಜಮೀನಿನಲ್ಲಿ $cropName ಇಳುವರಿ ಎಕರೆಗೆ 15-18 ಕ್ವಿಂಟಾಲ್ ನಿರೀಕ್ಷಿಸಬಹುದು. ಪ್ರಸ್ತುತ ಹವಾಮಾನವು ಬೆಳೆ ಬೆಳೆಯಲು ಪೂರಕವಾಗಿದೆ."
                } else {
                    "Expected yield for $cropName in $city is approximately 15-18 quintals per acre. Current weather conditions are favorable for growth."
                }
                PredictionType.MARKET -> if (isKn) {
                    "$cropName ಮಾರುಕಟ್ಟೆ ಬೆಲೆಯು ಮುಂದಿನ 2 ತಿಂಗಳಲ್ಲಿ ಶೇ. 10 ರಷ್ಟು ಏರಿಕೆಯಾಗುವ ಸಾಧ್ಯತೆಯಿದೆ."
                } else {
                    "Market price for $cropName is expected to rise by 10% in the next 2 months due to high demand."
                }
                PredictionType.DISEASE -> if (isKn) {
                    "ಪ್ರಸ್ತುತ ಹವಾಮಾನದಿಂದಾಗಿ $cropName ಬೆಳೆಗೆ ಎಲೆ ಚುಕ್ಕೆ ರೋಗ ಬರುವ ಸಾಧ್ಯತೆಯಿದೆ. ಮುನ್ನೆಚ್ಚರಿಕೆ ಕ್ರಮವಾಗಿ ಬೇವಿನ ಎಣ್ಣೆ ಸಿಂಪಡಿಸಿ."
                } else {
                    "High humidity in $city may lead to Leaf Spot disease in $cropName. Recommended preventive spray of organic pesticides."
                }
            }

            _uiState.value = PredictionUiState.Result(predictionText)
        }
    }

    fun reset() {
        _uiState.value = PredictionUiState.Idle
    }
}
