package com.example.raitha_varta.ui.recommendation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CropRec(
    val cropName: String,
    val expectedYield: String,
    val profitability: String,
    val reason: String,
    val tips: String
)

sealed class RecommendationUiState {
    object Idle : RecommendationUiState()
    object Loading : RecommendationUiState()
    data class Result(val crops: List<CropRec>, val soilHealth: String, val bestSeason: String) : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}

@HiltViewModel
class CropRecommendationViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun getRecommendations(
        soilType: String,
        location: String,
        season: String,
        waterAvailability: String,
        farmSize: String,
        languageCode: String = "en"
    ) {
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            kotlinx.coroutines.delay(2000)
            
            val isKn = languageCode == "kn"
            val crops = if (isKn) {
                listOf(
                    CropRec("ಭತ್ತ (Paddy)", "20-25 ಕ್ವಿಂಟಾಲ್/ಎಕರೆ", "High", "ನಿಮ್ಮ ಮಣ್ಣು ಮತ್ತು ನೀರಿನ ಲಭ್ಯತೆಗೆ ಸೂಕ್ತ.", "ಸಕಾಲಕ್ಕೆ ಗೊಬ್ಬರ ನೀಡಿ."),
                    CropRec("ರಾಗಿ (Ragi)", "12-15 ಕ್ವಿಂಟಾಲ್/ಎಕರೆ", "Medium", "ಕಡಿಮೆ ನೀರಿನಲ್ಲೂ ಬೆಳೆಯಬಹುದು.", "ಕಳೆ ನಿಯಂತ್ರಣ ಮಾಡಿ."),
                    CropRec("ಟೊಮ್ಯಾಟೊ (Tomato)", "15-20 ಟನ್/ಎಕರೆ", "High", "ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಉತ್ತಮ ಬೇಡಿಕೆ ಇದೆ.", "ಕೀಟಬಾಧೆ ಬಗ್ಗೆ ಎಚ್ಚರವಿರಲಿ.")
                )
            } else {
                listOf(
                    CropRec("Paddy", "20-25 quintals/acre", "High", "Perfect for your soil type and water availability.", "Ensure timely fertilization."),
                    CropRec("Ragi", "12-15 quintals/acre", "Medium", "Drought resistant and suits your season.", "Focus on weed management."),
                    CropRec("Tomato", "15-20 tons/acre", "High", "Good market demand in your region.", "Watch for early blight symptoms.")
                )
            }

            _uiState.value = RecommendationUiState.Result(
                crops = crops,
                soilHealth = if (isKn) "ನಿಮ್ಮ ಮಣ್ಣು ಉತ್ತಮ ಫಲವತ್ತತೆ ಹೊಂದಿದೆ." else "Your soil shows good fertility levels.",
                bestSeason = season
            )
        }
    }

    fun reset() { _uiState.value = RecommendationUiState.Idle }
}
