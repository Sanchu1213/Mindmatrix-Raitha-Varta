package com.example.raitha_varta.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.R
import com.example.raitha_varta.data.SettingsRepository
import com.example.raitha_varta.data.Tip
import com.example.raitha_varta.data.TipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed interface TipUiState {
    data object Loading : TipUiState
    data class Success(val tips: List<Tip>) : TipUiState
    data class Error(val message: String) : TipUiState
}

@HiltViewModel
class TipViewModel @Inject constructor(
    private val repository: TipRepository,
    private val settingsRepository: SettingsRepository,
    private val firebaseTipModelManager: com.example.raitha_varta.data.FirebaseTipModelManager,
    private val weatherRepository: com.example.raitha_varta.data.WeatherRepository
) : ViewModel() {
    private data class KnAdvisoryProfile(
        val instruction: String,
        val weather: String,
        val stage: String,
        val action: String,
        val reason: String,
        val farmingMethod: String,
        val medicinalBenefit: String,
        val priority: String
    )


    private val _selectedCropId = MutableStateFlow<String?>(null)
    val selectedCropId = _selectedCropId.asStateFlow()

    val useKannada: StateFlow<Boolean> = settingsRepository.preferredLanguage
        .map { it == "kn" }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TipUiState> = _selectedCropId.flatMapLatest { cropId ->
        repository.getAllTips().map { tips ->
            val filtered = if (cropId == null || cropId == "null") tips 
                           else tips.filter { it.cropId == cropId }
            
            if (filtered.isEmpty()) {
                if (tips.isEmpty()) {
                    TipUiState.Loading
                } else if (cropId != null && cropId != "null") {
                    TipUiState.Success(listOf(fallbackTipForCrop(cropId)))
                } else {
                    TipUiState.Success(emptyList())
                }
            } else {
                // REAL-TIME ML INTEGRATION:
                var temp = 30.0
                var hum = 60
                var userCity = "Bengaluru"
                
                // Fetch weather and location
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        userCity = settingsRepository.preferredCity.first()
                        val weatherDataResult = weatherRepository.getWeather(userCity.ifEmpty { "Bengaluru" })
                        weatherDataResult.onSuccess { weatherData ->
                            temp = weatherData.main.temp
                            hum = weatherData.main.humidity
                        }
                    } catch (e: Exception) { }
                }
                
                // ML Model predicts the best category based on live weather
                val bestCategory = firebaseTipModelManager.predictBestTipCategory(temp, hum)
                
                // Sort the dataset so the real-time ML recommendation is shown FIRST!
                var sortedTips = filtered.sortedByDescending { it.category == bestCategory }
                
                // DYNAMIC LOCATION-BASED MARKET PRICES
                if (cropId != null && cropId != "null") {
                    // Try to find a market record matching the user's city/district
                    val localMarketRecord = com.example.raitha_varta.data.MarketDataset.allRecords.find { 
                        it.cropId == cropId && (it.district.contains(userCity, ignoreCase = true) || it.market.contains(userCity, ignoreCase = true))
                    } ?: com.example.raitha_varta.data.MarketDataset.allRecords.find { it.cropId == cropId } // Fallback to any state
                    
                    if (localMarketRecord != null) {
                        val cropName = cropId.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
                        val marketTip = Tip(
                            id = "${cropId}_market_dynamic",
                            cropId = cropId,
                            cropName = cropName,
                            category = "Market Price",
                            instructionEn = "📍 Local Market Report for ${userCity.ifEmpty{"Your Area"}}: The modal price in ${localMarketRecord.market}, ${localMarketRecord.state} is ₹${localMarketRecord.modalPrice}/quintal. Range: ₹${localMarketRecord.minPrice} - ₹${localMarketRecord.maxPrice}.",
                            instructionKn = "📍 ಮಾರುಕಟ್ಟೆ ವರದಿ: ${localMarketRecord.market} ಮಾರುಕಟ್ಟೆಯಲ್ಲಿ ಕ್ವಿಂಟಾಲ್‌ಗೆ ₹${localMarketRecord.modalPrice} ಬೆಲೆ ಇದೆ.",
                            imageRes = cropImageFor(cropId),
                            weather = "Market Data",
                            stage = "Harvest / Post-Harvest",
                            action = "Sell at ${localMarketRecord.market} if profitable.",
                            reason = "Price data dynamically fetched for your configured location."
                        )
                        // Add the dynamic market tip to the list
                        sortedTips = sortedTips + marketTip
                    }

                    // ADD DAILY CULTIVATION TIMELINE (Real-time days)
                    val timelineTips = generateTimelineTips(cropId)
                    sortedTips = sortedTips + timelineTips
                }
                
                TipUiState.Success(sortedTips)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TipUiState.Loading)

    fun setCropFilter(cropId: String?) {
        _selectedCropId.value = cropId
    }

    fun toggleLanguage() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = settingsRepository.preferredLanguage.first()
            settingsRepository.savePreferredLanguage(if (current == "kn") "en" else "kn")
        }
    }

    fun refreshTips() {
        viewModelScope.launch(Dispatchers.IO) {
            val mockTips = com.example.raitha_varta.data.MockDataset.massiveMockDataset
            val enrichedTips = mockTips.map(::applyCropSpecificKannada)

            // 1. First populate mock tips as fallback
            repository.insertTips(enrichedTips)
            
            // 2. Fetch fresh tips from Firebase to overwrite mock ones if they exist
            repository.fetchTipsFromFirebase()
        }
    }

    init {
        refreshTips()
    }

    private fun fallbackTipForCrop(cropId: String): Tip {
        val cropName = cropId.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
        val profile = knProfileFor(cropId)
        return Tip(
            id = "fallback_$cropId",
            cropId = cropId,
            cropName = cropName,
            category = "KVK Weekly Advisory",
            instructionEn = "1-minute advisory for $cropName: check field moisture and pest hotspots.",
            instructionKn = profile.instruction,
            imageRes = cropImageFor(cropId),
            weather = ensureBi("Weekly outlook: monitor rain spell and humidity before spray decisions.", profile.weather),
            stage = ensureBi("Field scouting: inspect 20 plants and note early symptoms.", profile.stage),
            action = ensureBi("Use crop-stage dose only; avoid blanket chemical mix.", profile.action),
            reason = ensureBi("Early action lowers cost and prevents yield loss.", profile.reason),
            farmingMethod = ensureBi("Method: Drip irrigation + mulching + need-based fertigation.", profile.farmingMethod),
            medicinalBenefit = ensureBi("Extra note: Prefer evening spray; keep regular observation log.", profile.medicinalBenefit),
            priority = "Medium|||${profile.priority}"
        )
    }

    private fun cropImageFor(cropId: String): Int = when (cropId) {
        "paddy" -> R.drawable.rice
        "wheat" -> R.drawable.wheat
        "ragi" -> R.drawable.ragi
        "maize" -> R.drawable.corn
        "bajra" -> R.drawable.bajra
        "jowar" -> R.drawable.jowar
        "tomato" -> R.drawable.tomato
        "onion" -> R.drawable.onion
        "potato" -> R.drawable.potato
        "brinjal" -> R.drawable.brinjal
        "okra" -> R.drawable.lady_finger
        "carrot" -> R.drawable.carrot
        "spinach" -> R.drawable.spinach
        "drumstick" -> R.drawable.drumstick
        "cabbage" -> R.drawable.cabbage
        "red_chilli" -> R.drawable.red_chilli
        "mango" -> R.drawable.mango
        "banana" -> R.drawable.banana
        "pomegranate" -> R.drawable.pomegranate
        "grapes" -> R.drawable.grapes
        "papaya" -> R.drawable.papaya
        "coconut" -> R.drawable.coconuts
        "arecanut" -> R.drawable.arecanut
        "sugarcane" -> R.drawable.sugarcane
        "coffee" -> R.drawable.coffee
        else -> R.drawable.rice
    }

    private fun ensureBi(value: String, knPrefix: String): String {
        if (value.contains("|||")) return value
        return "$value|||$knPrefix."
    }

    private fun applyCropSpecificKannada(tip: Tip): Tip {
        val profile = knProfileFor(tip.cropId)
        return tip.copy(
            instructionKn = profile.instruction,
            weather = ensureBi(tip.weather, profile.weather),
            stage = ensureBi(tip.stage, profile.stage),
            action = ensureBi(tip.action, profile.action),
            reason = ensureBi(tip.reason, profile.reason),
            farmingMethod = ensureBi("Method: Crop-wise package of practices + weekly monitoring.", profile.farmingMethod),
            medicinalBenefit = ensureBi("Extra note: Maintain spray records and observe field response.", profile.medicinalBenefit),
            priority = "Medium|||${profile.priority}"
        )
    }

    private fun knProfileFor(cropId: String): KnAdvisoryProfile = when (cropId) {
        "paddy" -> KnAdvisoryProfile("ಭತ್ತಕ್ಕೆ ವಾರದ ಸಲಹೆ: ಟಿಲ್ಲರ್ ಹಂತದಲ್ಲಿ ಬೋರರ್ ಮತ್ತು ತೇವಾಂಶವನ್ನು ಕಟ್ಟುನಿಟ್ಟಾಗಿ ಗಮನಿಸಿ.", "ಬೆಳಗಿನ ತೇವ ಹೆಚ್ಚಿರುವ ವಾರದಲ್ಲಿ ಭತ್ತ ಹೊಲದಲ್ಲಿ ಬೋರರ್ ಚಟುವಟಿಕೆ ಹೆಚ್ಚಾಗುವ ಸಾಧ್ಯತೆ ಇದೆ", "ಮಧ್ಯ ಟಿಲ್ಲರ್ ಭಾಗದಲ್ಲಿ ಡೆಡ್ ಹಾರ್ಟ್ ಲಕ್ಷಣ ಕಂಡರೆ ತಕ್ಷಣ ಗುರುತು ಮಾಡಿ", "ಭತ್ತಕ್ಕೆ ಶಿಫಾರಸಾದ ಕೀಟನಾಶಕವನ್ನು ಸರಿಯಾದ ಪ್ರಮಾಣದಲ್ಲಿ ಸಂಜೆ ಸಿಂಪಡಿಸಿ", "ಆರಂಭಿಕ ನಿಯಂತ್ರಣದಿಂದ ಕಂಬಗಳ ಉಳಿವು ಹಾಗೂ ಕೊನೆಯ ಇಳುವರಿ ಉತ್ತಮವಾಗುತ್ತದೆ", "ಪರ್ಯಾಯ ನೆನೆಸಿ-ಒಣಗಿಸುವ ನೀರಾವರಿ ಕ್ರಮವನ್ನು ಪಾಲಿಸಿ ಸಾಲು ಪರಿಶೀಲನೆ ಮಾಡಿ", "ಪ್ರತಿ ಎಕರೆಗೆ ಕನಿಷ್ಠ 20 ಗುಡ್ಡೆ ದಾಖಲಿಸಿ ನಂತರವೇ ಸಿಂಪಡಣೆ ನಿರ್ಧಾರ ಮಾಡಿ", "ಉಚ್ಚ")
        "wheat" -> KnAdvisoryProfile("ಗೋಧಿಗೆ ವಾರದ ಸಲಹೆ: ಹೂಬರುವ ಹಂತದಲ್ಲಿ ಉಷ್ಣತೆ ಒತ್ತಡ ಕಡಿಮೆ ಮಾಡುವ ಕ್ರಮ ಕೈಗೊಳ್ಳಿ.", "ತಾಪಮಾನ ಏರಿಕೆಯಿಂದ ಹೂಬರುವ ಗೋಧಿಯಲ್ಲಿ ತೇವ ನಷ್ಟ ವೇಗವಾಗಿ ನಡೆಯುತ್ತದೆ", "ಹೂಬಿಡುವದಿಂದ ಮಿಲ್ಕಿ ಗ್ರೇನ್ ಹಂತದ ಹೊಲಗಳನ್ನು ಬೇರ್ಪಡಿಸಿ ನಿಗಾ ಇಡಿ", "ಸಂಜೆ ನೀರಾವರಿ ನೀಡಿ, ದಿನದ ಬಿಸಿಯಲ್ಲಿ ಯಾವುದೇ ಸಿಂಪಡಣೆ ತಪ್ಪಿಸಿ", "ಈ ಹಂತದ ಉಷ್ಣತೆ ಒತ್ತಡ ಧಾನ್ಯ ತುಂಬುವಿಕೆಯನ್ನು ನೇರವಾಗಿ ಕಡಿಮೆ ಮಾಡುತ್ತದೆ", "ಮಣ್ಣಿನ ತೇವ ಆಧಾರಿತ ಹಗುರ ನೀರಾವರಿ ಚಕ್ರ ಅನುಸರಿಸಿ", "ಹೀಟ್ ಸ್ಟ್ರೆಸ್ ದಿನಗಳಲ್ಲಿ ಹೊಲ ಗಡಿಯ ಗಾಳಿಯ ಅಡ್ಡಪದಾರ್ಥವನ್ನು ಕಾಪಾಡಿ", "ಉಚ್ಚ")
        "ragi" -> KnAdvisoryProfile("ರಾಗಿಗೆ ವಾರದ ಸಲಹೆ: ಕಂಬ ರೂಪಿಸುವ ಹಂತದಲ್ಲಿ ಪೋಷಕ ಸಮತೋಲನವನ್ನು ತಕ್ಷಣ ಸರಿಪಡಿಸಿ.", "ಸಮಶೀತೋಷ್ಣ ಹವಾಮಾನದಲ್ಲಿ ಹಗುರ ಮಳೆಯ ನಂತರ ನೈಟ್ರಜನ್ ಬಳಕೆ ಪರಿಣಾಮಕಾರಿ", "ಪ್ಯಾನಿಕಲ್ ಆರಂಭ ಹಂತದ ರಾಗಿ ಗಿಡಗಳಲ್ಲಿ ಹಳದಿಕರಣ ಕಂಡರೆ ದಾಖಲಿಸಿ", "ಎರಡನೇ ಟಾಪ್ ಡ್ರೆಸಿಂಗ್ ಜೊತೆ ಜಿಂಕ್ ಸಲ್ಫೇಟ್ ನೀಡಿ ಹಗುರ ನೀರಾವರಿ ಮಾಡಿ", "ಈ ಹಂತದ ಪೋಷಕ ಲಭ್ಯತೆ ಕಾಳು ತುಂಬುವಿಕೆ ಮತ್ತು ಕಂಬ ತೂಕ ನಿರ್ಧರಿಸುತ್ತದೆ", "ಸಾಲು ನಡುವಿನ ಗಾಳಿ ಹರಿವು ಉಳಿಯುವಂತೆ ಹಗುರ ಗದ್ದೆ ಕಲ್ಪನೆ ಮಾಡಿ", "ಒಂದೇ ಬಾರಿ ಅಧಿಕ ಡೋಸ್ ಬದಲು ವಿಭಜಿತ ಪೋಷಕಾಂಶ ನೀಡಿ", "ಉಚ್ಚ")
        "maize" -> KnAdvisoryProfile("ಮೆಕ್ಕೆಜೋಳಕ್ಕೆ ವಾರದ ಸಲಹೆ: FAW ನಿಗಾದಲ್ಲಿ ಭುಜ ಹಾನಿ ಕಂಡ ತಕ್ಷಣ ನಿಯಂತ್ರಣ ಆರಂಭಿಸಿ.", "ಬಿಸಿ ಮತ್ತು ಒಣ ವಾತಾವರಣದಲ್ಲಿ ಫಾಲ್ ಆರ್ಮಿವರ್ಮ್ ಇಳುವರಿ ಹಾನಿ ವೇಗವಾಗಿ ಹೆಚ್ಚುತ್ತದೆ", "ಭುಜದೊಳಗಿನ ಕೀಟ ಮಲ ಮತ್ತು ವಿಂಡೋಯಿಂಗ್ ಲಕ್ಷಣಗಳನ್ನು ಬೆಳಿಗ್ಗೆ ಪರಿಶೀಲಿಸಿ", "ಕೀಟನಾಶಕ ದ್ರಾವಣವನ್ನು ಭುಜದ ಕೇಂದ್ರ ಭಾಗಕ್ಕೆ ತಲುಪುವಂತೆ ಸಿಂಪಡಿಸಿ", "ತಡವಾದ ಸಿಂಪಡಣೆ ಮಾಡಿದರೆ ದೊಡ್ಡ ಲಾರ್ವಾ ನಿಯಂತ್ರಣ ಕಷ್ಟವಾಗುತ್ತದೆ", "ರಿಡ್ಜ್-ಫರೋ ನೀರಾವರಿ ಜೊತೆಗೆ ನಿಯಮಿತ ಭುಜ ಪರಿಶೀಲನೆ ಮಾಡಿರಿ", "ಒಂದೇ ಔಷಧಿ ಪುನರಾವರ್ತನೆ ತಪ್ಪಿಸಿ ಪ್ರತಿರೋಧ ನಿರ್ವಹಣೆ ಅನುಸರಿಸಿ", "ಉಚ್ಚ")
        "bajra" -> KnAdvisoryProfile("ಸಜ್ಜೆಗೆ ವಾರದ ಸಲಹೆ: ಆರಂಭಿಕ ಕಂಬ ಹಂತದಲ್ಲಿ ತೇವ ಮತ್ತು ಸಣ್ಣ ಕೀಟ ಹಾನಿ ಪರಿಶೀಲಿಸಿ.", "ಒಣ ಗಾಳಿಯ ವಾರದಲ್ಲಿ ಸಜ್ಜೆ ಹೊಲದಲ್ಲಿ ಎಲೆ ತೇವ ಬೇಗ ಕುಸಿಯುತ್ತದೆ", "ಕಂಬ ಆರಂಭ ಹಂತದ ಗಿಡಗಳಲ್ಲಿ ಎಲೆ ಚುಕ್ಕೆ ಮತ್ತು ಕುಂಠಿತ ಬೆಳವಣಿಗೆ ನೋಡಿರಿ", "ಹಗುರ ನೀರಾವರಿ ನೀಡಿ, ಅಗತ್ಯವಿದ್ದರೆ ಶಿಫಾರಸಾದ ಸೂಕ್ಷ್ಮಪೋಷಕ ಸಿಂಪಡಿಸಿ", "ಪ್ರಾರಂಭಿಕ ಹಂತದಲ್ಲಿ ಸರಿಯಾದ ಕ್ರಮದಿಂದ ಕಂಬ ತುಂಬುವಿಕೆ ಉತ್ತಮವಾಗುತ್ತದೆ", "ಸಾಲು ಅಂತರ ಕಾಪಾಡಿ ನೀರಾವರಿ ಅಂತರವನ್ನು ಮಣ್ಣು ಆಧಾರವಾಗಿ ಹೊಂದಿಸಿ", "ಹೊಲ ನಿಗಾ ದಾಖಲೆಯನ್ನು ವಾರಕ್ಕೊಮ್ಮೆ ನವೀಕರಿಸಿ", "ಮಧ್ಯಮ")
        "jowar" -> KnAdvisoryProfile("ಜೋಳಕ್ಕೆ ವಾರದ ಸಲಹೆ: ಪ್ಯಾನಿಕಲ್ ಪೂರ್ವ ಹಂತದಲ್ಲಿ ಪೋಷಕ ಕೊರತೆ ತಕ್ಷಣ ಸರಿಪಡಿಸಿ.", "ಹವಾಮಾನ ಬದಲಾವಣೆ ವಾರದಲ್ಲಿ ಜೋಳದಲ್ಲಿ ತೇವ ಒತ್ತಡ ಲಕ್ಷಣಗಳು ಕಾಣಿಸಬಹುದು", "ಮೇಲಿನ ಎಲೆಗಳ ವರ್ಣ ಬದಲಾವಣೆ ಮತ್ತು ಬೆಳವಣಿಗೆ ಕುಂಠಿತತೆ ಗಮನಿಸಿ", "ಶಿಫಾರಸಾದ ಟಾಪ್ ಡ್ರೆಸಿಂಗ್ ನೀಡಿ ನಂತರ ಹಗುರ ನೀರಾವರಿ ಮಾಡಿ", "ಈ ಹಂತದ ಪೋಷಕ ನಿರ್ವಹಣೆ ಕಾಳಿನ ಗಾತ್ರ ಮತ್ತು ಸಂಖ್ಯೆಗೆ ಪ್ರಮುಖ", "ಮಣ್ಣಿನ ಪರೀಕ್ಷೆ ಆಧಾರಿತ ಪೋಷಕಾಂಶ ನಿರ್ವಹಣೆಗೆ ಆದ್ಯತೆ ನೀಡಿ", "ಪೋಷಕ ದ್ರಾವಣ ಮಿಶ್ರಣ ಮಾಡುವ ಮೊದಲು ಲೇಬಲ್ ಶಿಫಾರಸು ಪರಿಶೀಲಿಸಿ", "ಮಧ್ಯಮ")
        "tomato" -> KnAdvisoryProfile("ಟೊಮ್ಯಾಟೊಗೆ ಸಲಹೆ: ಬ್ಲೈಟ್ ಅಪಾಯದ ಅವಧಿಯಲ್ಲಿ ತಡೆ ಸಿಂಪಡಣೆ ಕ್ರಮಬದ್ಧವಾಗಿ ಮಾಡಿ.", "ರಾತ್ರಿ ತೇವಾಂಶ ಹೆಚ್ಚಿರುವ ಸಮಯದಲ್ಲಿ ಟೊಮ್ಯಾಟೊ ಎಲೆ ರೋಗ ಬೇಗ ಹರಡುವ ಸಾಧ್ಯತೆ ಇದೆ", "ಕೆಳ ಎಲೆಗಳಲ್ಲಿ ವಲಯಾಕಾರದ ಕಂದು ಚುಕ್ಕೆ ಕಂಡ ತಕ್ಷಣ ಸೋಂಕಿತ ಎಲೆ ತೆಗೆಯಿರಿ", "ಮ್ಯಾಂಕೋಜೆಬ್ ದ್ರಾವಣವನ್ನು ಎಲೆಯ ಎರಡೂ ಬದಿಗೆ ಸಮವಾಗಿ ಸಿಂಪಡಿಸಿ", "ದೀರ್ಘ ಎಲೆ ತೇವಾವಧಿ ಇದ್ದರೆ ಸ್ಪೋರ್ ಮೊಳಕೆಯಾಗಿ ರೋಗ ವಿಸ್ತರಣೆ ವೇಗವಾಗುತ್ತದೆ", "ಡ್ರಿಪ್ ನೀರಾವರಿ ಮತ್ತು ಮಲ್ಚಿಂಗ್ ಮೂಲಕ ಎಲೆ ತೇವ ಕಡಿಮೆ ಇರಿಸಿ", "ಸಿಂಪಡಣೆ ನಡುವೆ ಸರಿಯಾದ ಅಂತರ ಮತ್ತು ರೋಗ ನಿಗಾ ದಾಖಲೆಯನ್ನು ಕಾಪಾಡಿ", "ಉಚ್ಚ")
        "onion" -> KnAdvisoryProfile("ಈರುಳ್ಳಿಗೆ ವಾರದ ಸಲಹೆ: ಗಡ್ಡೆ ವಿಸ್ತರಣೆ ಹಂತದಲ್ಲಿ ಸಮತೋಲನ NPK ನೀಡಿಕೆ ಕಡ್ಡಾಯ.", "ಒಣ ಹವಾಮಾನ ವಾರದಲ್ಲಿ ಗಡ್ಡೆ ರೂಪಿಸುವ ಈರುಳ್ಳಿಗೆ ಸಮನೀರಾವರಿ ಅಗತ್ಯ", "ಗಡ್ಡೆ ವಿಸ್ತರಣೆ ಹಂತದ ಸಾಲುಗಳಲ್ಲಿ ಗಾತ್ರ ವ್ಯತ್ಯಾಸ ಕಂಡರೆ ದಾಖಲೆ ಮಾಡಿ", "ವಿಭಜಿತ ಮೇಲ್ಚೇರಿಕೆ ರೂಪದಲ್ಲಿ ಶಿಫಾರಸಾದ NPK ಪ್ರಮಾಣ ನೀಡಿ", "ಅಸಮ ಪೋಷಕಾಂಶದಿಂದ ಗಡ್ಡೆ ಚೀರಿಕೆ ಮತ್ತು ಮಾರುಕಟ್ಟೆ ಗುಣಮಟ್ಟ ಕುಸಿತವಾಗುತ್ತದೆ", "ಅಲ್ಪ ಪ್ರಮಾಣದ ನೀರಾವರಿ ಚಕ್ರ ಮತ್ತು ರೋಗರಹಿತ ಹೊಲ ಸ್ವಚ್ಛತೆ ಪಾಲಿಸಿ", "ನೈಟ್ರಜನ್ ಹೆಚ್ಚುವರಿ ನೀಡಿಕೆ ತಪ್ಪಿಸಿ ಸಂಗ್ರಹ ಸಾಮರ್ಥ್ಯ ಕಾಪಾಡಿ", "ಮಧ್ಯಮ")
        "potato" -> KnAdvisoryProfile("ಆಲೂಗಡ್ಡೆಗೆ ವಾರದ ಸಲಹೆ: ಗುಡ್ಡೆ ಹಂತದಲ್ಲಿ ಎಲೆಹುಳು ಮತ್ತು ತಡಕಲು ರೋಗ ನಿಗಾ ಹೆಚ್ಚಿಸಿ.", "ತೇವ ಮತ್ತು ತಂಪು ಬೆಳಿಗ್ಗೆಗಳಲ್ಲಿ ಆಲೂಗಡ್ಡೆ ತಡಕಲು ರೋಗದ ಅಪಾಯ ಹೆಚ್ಚುತ್ತದೆ", "ಕೆಳ ಎಲೆಗಳಲ್ಲಿ ನೀರಿನ ಕಲೆಗಳು ಮತ್ತು ಕಂದು ಗಡಿಗಳು ಕಂಡರೆ ತಕ್ಷಣ ಗುರುತು ಮಾಡಿ", "ಶಿಫಾರಸಾದ ಫಂಗಿಸೈಡ್ ಕ್ರಮಬದ್ಧವಾಗಿ ನೀಡಿ ಮತ್ತು ಹೊಲದ ತೇವ ನಿಯಂತ್ರಿಸಿ", "ರೋಗ ಪ್ರಾರಂಭದ ಸಮಯದಲ್ಲೇ ಕ್ರಮ ಕೈಗೊಂಡರೆ ಗಡ್ಡೆ ಹಾನಿ ಬಹಳವಾಗಿ ಕಡಿಮೆಯಾಗುತ್ತದೆ", "ರಿಡ್ಜ್ ವ್ಯವಸ್ಥೆಯಲ್ಲಿ ನೀರು ನಿಲ್ಲದಂತೆ ಒಳಚರಂಡಿ ಕಾಪಾಡಿ", "ಬೀಜಗಡ್ಡೆ ಉಳಿವು ಪ್ರದೇಶವನ್ನು ಸೋಂಕಿತ ಅವಶೇಷಗಳಿಂದ ದೂರ ಇಡಿ", "ಉಚ್ಚ")
        "brinjal" -> KnAdvisoryProfile("ಬದನೆಕಾಯಿಗೆ ವಾರದ ಸಲಹೆ: ಶೂಟ್-ಫ್ರೂಟ್ ಬೋರರ್ ನಿಗಾದಲ್ಲಿ ಸೋಂಕಿತ ಕೊಂಬೆ ತೆಗೆದುಹಾಕಿ.", "ಮಿತ ಉಷ್ಣತೆ ಮತ್ತು ತೇವಾಂಶದ ವಾರದಲ್ಲಿ ಬೋರರ್ ಹಾನಿ ಹೆಚ್ಚುವ ಸಾಧ್ಯತೆ ಇದೆ", "ಹೊಸ ಕೊಂಬೆ ಮತ್ತು ಮುದ್ದೆ ಹಣ್ಣುಗಳಲ್ಲಿ ರಂಧ್ರ ಹಾಗೂ ಒಳಹುಳು ಲಕ್ಷಣ ನೋಡಿ", "ಸೋಂಕಿತ ಕೊಂಬೆ-ಹಣ್ಣು ಸಂಗ್ರಹಿಸಿ ನಾಶ ಮಾಡಿ ನಂತರ ಶಿಫಾರಸಾದ ಸಿಂಪಡಣೆ ಮಾಡಿ", "ಸ್ವಚ್ಛತೆ ಕ್ರಮ ಮತ್ತು ಸಮಯೋಚಿತ ನಿಯಂತ್ರಣದಿಂದ ಮಾರುಕಟ್ಟೆ ಗುಣಮಟ್ಟ ಉಳಿಯುತ್ತದೆ", "ಡ್ರಿಪ್ ನೀರಾವರಿ ಮತ್ತು ಮಧ್ಯಮ ಎಲೆಸಾಂದ್ರತೆ ನಿರ್ವಹಿಸಿ", "ಲೈಟ್ ಟ್ರಾಪ್ ಮತ್ತು ಫೆರೋಮೋನ್ ಟ್ರಾಪ್ ಸಂಯೋಜಿತವಾಗಿ ಬಳಸಿ", "ಮಧ್ಯಮ")
        "okra" -> KnAdvisoryProfile("ಬೆಂಡೆಕಾಯಿಗೆ ವಾರದ ಸಲಹೆ: ಜಸ್ಸಿಡ್ ಮತ್ತು ಹಳದಿ ಎಲೆಮೋಸೈಕ್ ಲಕ್ಷಣಗಳ ನಿಗಾ ಬಲಪಡಿಸಿ.", "ಒಣ-ಬಿಸಿ ವಾರದಲ್ಲಿ ಬೆಂಡೆಕಾಯಿಗೆ ಸಕ್ ಕೀಟ ಒತ್ತಡ ಹೆಚ್ಚಾಗುತ್ತದೆ", "ಎಲೆಗಳ ಅಂಚು ಹಳದಿ, ಮಡಿಕೆ ಅಥವಾ ಮೋಸೈಕ್ ಚಿತ್ತಾರ ಕಂಡರೆ ಗುರುತು ಮಾಡಿ", "ಶಿಫಾರಸಾದ ಕೀಟನಾಶಕ ಅಥವಾ ಬೇವು ತೈಲವನ್ನು ಹಗುರ ಸೋಂಕಿನಲ್ಲೇ ಬಳಸಿ", "ಆರಂಭದಲ್ಲೇ ನಿಯಂತ್ರಿಸಿದರೆ ವೈರಸ್ ಹರಡುವಿಕೆ ಮತ್ತು ಇಳುವರಿ ನಷ್ಟ ಕಡಿಮೆಯಾಗುತ್ತದೆ", "ಸಮತೋಲನ ಗೊಬ್ಬರ ನೀಡಿ ಅತಿಯಾದ ನೈಟ್ರಜನ್ ತಪ್ಪಿಸಿ", "ಸೋಂಕಿತ ಗಿಡಗಳನ್ನು ಬೇರ್ಪಡಿಸಿ ಹೊಲ ಸ್ವಚ್ಛತೆ ಕಟ್ಟುನಿಟ್ಟಾಗಿ ಪಾಲಿಸಿ", "ಮಧ್ಯಮ")
        "carrot" -> KnAdvisoryProfile("ಕ್ಯಾರಟ್‌ಗೆ ಸಲಹೆ: ಬೇರು ವೃದ್ಧಿ ಹಂತದಲ್ಲಿ ಮಣ್ಣಿನ ತೇವ ಸಮತೋಲನ ಕಾಪಾಡಿ.", "ಮಧ್ಯಮ ತಾಪಮಾನ ಸಮಯದಲ್ಲಿ ಮಣ್ಣಿನ ತೇವ ಏರಿಳಿತ ಬೇರು ಚೀರಿಕೆಗೆ ಕಾರಣವಾಗುತ್ತದೆ", "ಬೇರು ವಿಸ್ತರಣೆ ಸಾಲುಗಳಲ್ಲಿ ಮೇಲ್ಮೈ ಬಿರುಕು ಮತ್ತು ಎಲೆ ಬಣ್ಣ ಬದಲಾವಣೆ ಗಮನಿಸಿ", "ಸಣ್ಣ ಅಂತರದಲ್ಲಿ ಹಗುರ ನೀರಾವರಿ ನೀಡಿ ತೇವಾಂಶ ಸ್ಥಿರವಾಗಿರಿಸಿ", "ಸಮತೋಲನ ತೇವದಿಂದ ಬೇರುಗಳ ಗಾತ್ರ, ಬಣ್ಣ ಮತ್ತು ಗುಣಮಟ್ಟ ಉತ್ತಮಗೊಳ್ಳುತ್ತದೆ", "ಎತ್ತರದ ಬೆಡ್ ಪದ್ಧತಿ ಬಳಸಿ ನೀರು ನಿಲ್ಲಿಕೆ ತಪ್ಪಿಸಿ", "ಕೊಯ್ಲಿಗೆ ಮುನ್ನ ಮಿತ ನೀರಾವರಿ ನೀಡಿ ಸಂಗ್ರಹ ಗುಣಮಟ್ಟ ಹೆಚ್ಚಿಸಿ", "ಮಧ್ಯಮ")
        "spinach" -> KnAdvisoryProfile("ಪಾಲಕ್‌ಗೆ ವಾರದ ಸಲಹೆ: ಎಲೆ ಗುಣಮಟ್ಟಕ್ಕಾಗಿ ಪೋಷಕ-ತೇವ ಸಮತೋಲನ ಕಾಪಾಡಿ.", "ಮಿತ ತೇವದ ವಾರದಲ್ಲಿ ಪಾಲಕ್‌ನಲ್ಲಿ ಎಲೆ ರೋಗ ಮತ್ತು ಚುಕ್ಕೆ ಅಪಾಯ ಕಂಡುಬರುತ್ತದೆ", "ಹೊಸ ಎಲೆಗಳಲ್ಲಿ ಕಲೆ, ಮಡಿಕೆ ಅಥವಾ ಬೆಳವಣಿಗೆ ಕುಂಠಿತತೆ ಪರಿಶೀಲಿಸಿ", "ಅಗತ್ಯವಿದ್ದರೆ ಸೂಕ್ಷ್ಮಪೋಷಕ ಎಲೆ ಸಿಂಪಡಣೆ ನೀಡಿ ಹಾಗೂ ಸ್ವಚ್ಛ ನೀರಾವರಿ ಮಾಡಿ", "ಆರೋಗ್ಯಕರ ಎಲೆ ಬೆಳವಣಿಗೆ ಮಾರುಕಟ್ಟೆ ತೂಕ ಮತ್ತು ಕಟಾವು ಅವಧಿ ಹೆಚ್ಚಿಸುತ್ತದೆ", "ಚಿಕ್ಕ ಚಕ್ರದ ಬೆಳೆ ಆದ್ದರಿಂದ ನೀರಾವರಿ ಮತ್ತು ಕಟಾವು ಯೋಜನೆ ಸಮರ್ಪಕ ಇರಲಿ", "ಕಟಾವಿನ ನಂತರ ತಕ್ಷಣ ನೆರಳಿನಲ್ಲಿ ಸಂಗ್ರಹಿಸಿ ಎಲೆ ತಾಜಾತನ ಉಳಿಸಿ", "ಮಧ್ಯಮ")
        "drumstick" -> KnAdvisoryProfile("ನುಗ್ಗೆಕಾಯಿಗೆ ವಾರದ ಸಲಹೆ: ಹೂಹಂತದಲ್ಲಿ ತೇವ ಒತ್ತಡ ಮತ್ತು ಹೂ ಉದುರುವಿಕೆ ನಿಯಂತ್ರಿಸಿ.", "ಬಿಸಿ ಗಾಳಿ ವಾರದಲ್ಲಿ ನುಗ್ಗೆ ಹೂ ಉದುರುವಿಕೆ ಪ್ರಮಾಣ ಹೆಚ್ಚಾಗಬಹುದು", "ಹೊಸ ಹೂಗುಚ್ಚಗಳಲ್ಲಿ ಉದುರುವಿಕೆ ಮತ್ತು ಪುಡಿ ಹುಳು ಲಕ್ಷಣಗಳನ್ನು ಗಮನಿಸಿ", "ಸಮಯೋಚಿತ ನೀರಾವರಿ ನೀಡಿ, ಅಗತ್ಯವಿದ್ದರೆ ಶಿಫಾರಸಾದ ನಿಯಂತ್ರಣ ಕ್ರಮ ಅನುಸರಿಸಿ", "ಹೂಹಂತದ ನಿರ್ವಹಣೆಯಿಂದ ಕೊಂಬೆಗಿಂತ ಕೊಯ್ಲು ಸಂಖ್ಯೆ ಸ್ಪಷ್ಟವಾಗಿ ಹೆಚ್ಚುತ್ತದೆ", "ಪೆರಿನಿಯಲ್ ತೋಟದಲ್ಲಿ ನಿಯಮಿತ ಕತ್ತರಿಕೆ ಮತ್ತು ಗಾಳಿ ಹರಿವು ಕಾಪಾಡಿ", "ಹೂಧಾರಿತ ಅವಧಿಯಲ್ಲಿ ಹೊಲ ಸ್ವಚ್ಛತೆ ಮತ್ತು ಜೀವಾವಶೇಷ ನಿರ್ವಹಣೆ ಮಾಡಿ", "ಮಧ್ಯಮ")
        "cabbage" -> KnAdvisoryProfile("ಕೋಸಿಗೆ ವಾರದ ಸಲಹೆ: ತಲೆ ರೂಪಿಸುವ ಹಂತದಲ್ಲಿ ಸಡಿಲ ಹುಳು ನಿಗಾ ಮಾಡಿರಿ.", "ತಂಪು-ತೇವ ಬೆಳಿಗ್ಗೆಗಳಲ್ಲಿ ಕೋಸಿನಲ್ಲಿ ಎಲೆಹುಳು ಹಾಗೂ ರೋಗ ಒತ್ತಡ ಏರಬಹುದು", "ಹೊರ ಎಲೆಗಳಲ್ಲಿ ಚವಕ ಮತ್ತು ಗುಂಡಿ ಹಾನಿ ಕಂಡ ತಕ್ಷಣ ದಾಖಲಿಸಿ", "ಶಿಫಾರಸಾದ ಕೀಟನಾಶಕವನ್ನು ತಲೆ ಒಳಭಾಗ ತಲುಪದಂತೆ ಜಾಗರೂಕವಾಗಿ ಸಿಂಪಡಿಸಿ", "ಸರಿಯಾದ ಸಮಯದಲ್ಲಿ ನಿಯಂತ್ರಣ ಮಾಡಿದರೆ ತಲೆ ಗುಣಮಟ್ಟ ಮತ್ತು ತೂಕ ಉಳಿಯುತ್ತದೆ", "ಎತ್ತರದ ಬೆಡ್ ಮತ್ತು ಒಳಚರಂಡಿ ನಿರ್ವಹಣೆ ಉತ್ತಮವಾಗಿ ಇರಲಿ", "ಕೊಯ್ಲಿಗೆ ಮುನ್ನ ಅವಶ್ಯಕ ನಿರೀಕ್ಷಾ ಅವಧಿಯನ್ನು ಕಟ್ಟುನಿಟ್ಟಾಗಿ ಪಾಲಿಸಿ", "ಮಧ್ಯಮ")
        "red_chilli" -> KnAdvisoryProfile("ಮೆಣಸಿನಕಾಯಿಗೆ ವಾರದ ಸಲಹೆ: ತ್ರಿಪ್ಸ್ ಮತ್ತು ಎಲೆಮುಡುವಿಕೆ ಲಕ್ಷಣಗಳ ನಿಯಂತ್ರಣ ತಕ್ಷಣ ಮಾಡಿ.", "ಒಣ ಬಿಸಿ ವಾರದಲ್ಲಿ ತ್ರಿಪ್ಸ್ ಸಂಖ್ಯೆಯ ಏರಿಕೆ ವೇಗವಾಗಿ ನಡೆಯುತ್ತದೆ", "ಎಲೆ ಮೇಲ್ಮೈ ಬೆಳ್ಳಿ ಗೆರೆ ಮತ್ತು ಮೇಲ್ಮುಡುವಿಕೆ ಕಂಡರೆ ಹಾಟ್‌ಸ್ಪಾಟ್ ಗುರುತು ಮಾಡಿ", "ಶಿಫಾರಸಾದ ಔಷಧಿ ಅಥವಾ ಬೇವು ತೈಲವನ್ನು ಸಂಜೆ ವೇಳೆಯಲ್ಲಿ ಸಮವಾಗಿ ಸಿಂಪಡಿಸಿ", "ತ್ರಿಪ್ಸ್ ನಿಯಂತ್ರಣ ವಿಳಂಬವಾದರೆ ವೈರಸ್ ಹರಡುವಿಕೆ ಹೆಚ್ಚಾಗಿ ಹಣ್ಣು ಸೆಟ್ಟಿಂಗ್ ಕಡಿಮೆಯಾಗುತ್ತದೆ", "ಡ್ರಿಪ್ ನೀರಾವರಿ ಮತ್ತು ಮಲ್ಚಿಂಗ್ ಮೂಲಕ ತೇವ ಸ್ಥಿರತೆ ಕಾಪಾಡಿ", "ಹಾಟ್‌ಸ್ಪಾಟ್ ಭಾಗವನ್ನು ಮೊದಲಿಗೆ ಚಿಕಿತ್ಸೆ ನೀಡಿ ನಂತರ ಸಂಪೂರ್ಣ ಹೊಲಕ್ಕೆ ವಿಸ್ತರಿಸಿ", "ಉಚ್ಚ")
        "mango" -> KnAdvisoryProfile("ಮಾವಿಗೆ ವಾರದ ಸಲಹೆ: ಹೂಮೂಡು-ಹಣ್ಣು ಸೆಟ್ಟಿಂಗ್ ಹಂತದಲ್ಲಿ ಕೀಟ-ರೋಗ ನಿಗಾ ಬಿಗಿಗೊಳಿಸಿ.", "ಬೆಳಗಿನ ತುಂತುರು ತೇವದಿಂದ ಮಾವಿನ ಹೂಹಂತದಲ್ಲಿ ಪುಡಿ ಹುಳು ಒತ್ತಡ ಕಾಣಬಹುದು", "ಹೂಗುಚ್ಚಗಳಲ್ಲಿ ಬಿಳಿ ಪದರ ಅಥವಾ ಉದುರುವಿಕೆ ಕಂಡರೆ ತಕ್ಷಣ ಗುರುತು ಮಾಡಿ", "ಶಿಫಾರಸಾದ ಫಂಗಿಸೈಡ್/ಕೀಟ ನಿಯಂತ್ರಣ ಕ್ರಮವನ್ನು ಹೂಹಂತಕ್ಕೆ ಅನುಗುಣವಾಗಿ ಅನುಸರಿಸಿ", "ಹೂಹಂತದ ರಕ್ಷಣೆ ಸರಿಯಾಗಿದ್ದರೆ ಹಣ್ಣು ಸೆಟ್ಟಿಂಗ್ ಮತ್ತು ಕೊಯ್ಲು ಪ್ರಮಾಣ ಹೆಚ್ಚುತ್ತದೆ", "ತೋಟದಲ್ಲಿ ಗಾಳಿ ಹರಿವು ಹೆಚ್ಚಿಸಲು ಸೂಕ್ತ ಶಾಖೆ ಕತ್ತರಿಕೆ ಮಾಡಿ", "ಬಿದ್ದ ಹಣ್ಣು ಮತ್ತು ರೋಗಿತ ಅವಶೇಷಗಳನ್ನು ನಿಯಮಿತವಾಗಿ ತೆರವುಗೊಳಿಸಿ", "ಉಚ್ಚ")
        "banana" -> KnAdvisoryProfile("ಬಾಳೆಗೆ ವಾರದ ಸಲಹೆ: ಗೊನೆ ಹಂತದಲ್ಲಿ ಪೊಟಾಶ್ ಪೋಷಣೆಯನ್ನು ಸಮಯಕ್ಕೆ ನೀಡಿ.", "ತೇವ-ಗಾಳಿ ಮಿಶ್ರ ವಾರದಲ್ಲಿ ಗೊನೆ ಬಂದ ಬಾಳೆಗೆ ಪೋಷಕ ಬೇಡಿಕೆ ಹೆಚ್ಚಾಗುತ್ತದೆ", "ಗೊನೆ ಮತ್ತು ಬೆರಳಿನ ಬೆಳವಣಿಗೆ ಸಮತೋಲನಕ್ಕಾಗಿ ಗಿಡಶಕ್ತಿ ಪರಿಶೀಲಿಸಿ", "ಶಿಫಾರಸಾದ K2O ಪ್ರಮಾಣ ನೀಡಿ ನಂತರ ನೀರಾವರಿ ನೀಡಿ", "ಈ ಹಂತದ ಪೊಟಾಶ್ ನಿರ್ವಹಣೆ ಹಣ್ಣಿನ ಗಾತ್ರ, ಗಟ್ಟಿತನ ಮತ್ತು ಶೆಲ್ಫ್ ಲೈಫ್ ಹೆಚ್ಚಿಸುತ್ತದೆ", "ಡ್ರಿಪ್ ಮೂಲಕ ಭಾಗಭಾಗವಾಗಿ ಗೊಬ್ಬರ ನೀಡಿಕೆ ಅನುಸರಿಸಿ", "ಗೊನೆ ಬೆಂಬಲಕ್ಕಾಗಿ ಪ್ರಾಪಿಂಗ್ ಮಾಡಿ ಗಾಳಿ ಹಾನಿ ತಪ್ಪಿಸಿ", "ಉಚ್ಚ")
        "pomegranate" -> KnAdvisoryProfile("ದಾಳಿಂಬೆಗೆ ವಾರದ ಸಲಹೆ: ಅನಾರ ಪುಟ್ಟೆ ಕುಲುಮೆ ಮತ್ತು ಸಕ್ ಕೀಟ ನಿಗಾ ಮಾಡಿರಿ.", "ಒಣ ಮತ್ತು ಬಿಸಿ ವಾರದಲ್ಲಿ ದಾಳಿಂಬೆ ಹಣ್ಣಿನಲ್ಲಿ ಚೀರಿಕೆ ಹಾಗೂ ಕೀಟ ಹಾನಿ ಹೆಚ್ಚಬಹುದು", "ಹಣ್ಣು ಮೇಲ್ಮೈ ಕಲೆ, ಚೀರಿಕೆ ಮತ್ತು ಎಲೆ ಹಳದಿಕರಣ ಲಕ್ಷಣಗಳನ್ನು ಪರಿಶೀಲಿಸಿ", "ನಿರ್ದಿಷ್ಟ ಹಂತದ ಪೋಷಕ ಮತ್ತು ರಕ್ಷಣಾತ್ಮಕ ಸಿಂಪಡಣೆಯನ್ನು ನಿಯಮಿತವಾಗಿ ಮಾಡಿ", "ಸಮತೋಲನ ನೀರಾವರಿ ಹಾಗೂ ರೋಗ ನಿಗಾದಿಂದ ಹಣ್ಣು ಗುಣಮಟ್ಟ ಮತ್ತು ದರ ಉತ್ತಮವಾಗುತ್ತದೆ", "ಬೇಸಿನ್ ಪ್ರದೇಶದಲ್ಲಿ ಮಲ್ಚಿಂಗ್ ಮಾಡಿ ತೇವ ಏರಿಳಿತ ಕಡಿಮೆಮಾಡಿ", "ಹಾನಿಗೊಂಡ ಹಣ್ಣು ತೋಟದಲ್ಲಿ ಬಿಟ್ಟುಬಿಡದೆ ತಕ್ಷಣ ತೆಗೆಯಿರಿ", "ಮಧ್ಯಮ")
        "grapes" -> KnAdvisoryProfile("ದ್ರಾಕ್ಷಿಗೆ ವಾರದ ಸಲಹೆ: ಡೌನಿ ಮಿಲ್ಡ್ಯೂ ತಡೆ ಚಕ್ರವನ್ನು ವ್ಯತ್ಯಯವಿಲ್ಲದೆ ಮುಂದುವರಿಸಿ.", "ಬೆಳಗಿನ ಹನಿ ಮತ್ತು ಮೋಡಾವೃತ ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ ಡೌನಿ ಮಿಲ್ಡ್ಯೂ ವೇಗವಾಗಿ ಹರಡುತ್ತದೆ", "ಎಲೆ ಕೆಳಭಾಗದಲ್ಲಿ ಬಿಳಿ ಬೆಳವಣಿಗೆ ಮತ್ತು ಮೇಲ್ಭಾಗದಲ್ಲಿ ಎಣ್ಣೆ ಚುಕ್ಕೆ ಲಕ್ಷಣ ನೋಡಿ", "ಶಿಫಾರಸಾದ ಮಿಶ್ರ ಫಂಗಿಸೈಡ್ ಅನ್ನು ಕೆಳ ಎಲೆ ಭಾಗಕ್ಕೆ ತಲುಪುವಂತೆ ಸಿಂಪಡಿಸಿ", "ದೀರ್ಘ ಹನಿ ಅವಧಿ ದ್ವಿತೀಯ ಸೋಂಕು ವೇಗ ಹೆಚ್ಚಿಸಿ ಗುಚ್ಚ ಗುಣಮಟ್ಟ ಕುಗ್ಗಿಸುತ್ತದೆ", "ಕ್ಯಾನೊಪಿ ನಿರ್ವಹಣೆ ಮೂಲಕ ಗಾಳಿ ಹರಿವು ಹೆಚ್ಚಿಸಿ ಎಲೆ ತೇವಾವಧಿ ಕಡಿಮೆ ಮಾಡಿ", "ಬ್ಲಾಕ್‌ವಾರು ರೋಗ ನಕ್ಷೆ ಮಾಡಿ ಸ್ಪಾಟ್ ನಿರ್ವಹಣೆ ತಕ್ಷಣ ಕೈಗೊಳ್ಳಿ", "ಉಚ್ಚ")
        "papaya" -> KnAdvisoryProfile("ಪಪ್ಪಾಯಿಗೆ ವಾರದ ಸಲಹೆ: ರಿಂಗ್ಸ್‌ಪಾಟ್ ವೈರಸ್ ಹಾಗೂ ಕೀಟ ವಾಹಕಗಳ ಮೇಲ್ವಿಚಾರಣೆ ಹೆಚ್ಚಿಸಿ.", "ಬಿಸಿ-ತೇವ ಪರಿಸ್ಥಿತಿಯಲ್ಲಿ ಪಪ್ಪಾಯಿ ತೋಟದಲ್ಲಿ ವಾಹಕ ಕೀಟ ಚಟುವಟಿಕೆ ಹೆಚ್ಚಾಗುತ್ತದೆ", "ಎಲೆಗಳಲ್ಲಿ ಮೋಸೈಕ್ ವಲಯ ಚುಕ್ಕೆ ಮತ್ತು ಹಣ್ಣು ಮೇಲ್ಮೈ ಮಚ್ಚೆ ಲಕ್ಷಣ ಪರಿಶೀಲಿಸಿ", "ಸೋಂಕಿತ ಗಿಡಗಳನ್ನು ಬೇರ್ಪಡಿಸಿ ಶಿಫಾರಸಾದ ವಾಹಕ ನಿಯಂತ್ರಣ ಕ್ರಮ ಅನುಸರಿಸಿ", "ಆರಂಭಿಕ ರೋಗಿತ ಗಿಡ ತೆರವು ಮಾಡಿದರೆ ವೈರಸ್ ವಿಸ್ತರಣೆ ನಿಯಂತ್ರಣ ಸಾಧ್ಯವಾಗುತ್ತದೆ", "ತೋಟ ಸ್ವಚ್ಛತೆ ಮತ್ತು ಸಮತೋಲನ ನೀರಾವರಿ ಕಡ್ಡಾಯವಾಗಿ ಪಾಲಿಸಿ", "ಪಕ್ಕದ ಕಳೆ ಗಿಡಗಳನ್ನು ತೆರವು ಮಾಡಿ ವಾಹಕ ನೆಲೆ ತಪ್ಪಿಸಿ", "ಮಧ್ಯಮ")
        "coconut" -> KnAdvisoryProfile("ತೆಂಗಿಗೆ ವಾರದ ಸಲಹೆ: ಕ್ರೌನ್ ಭಾಗದಲ್ಲಿ ಗಂಡೆಕೀಟ ನಿಗಾ ಮತ್ತು ತಕ್ಷಣದ ನಿಯಂತ್ರಣ ಮಾಡಿ.", "ಮಳೆ ನಂತರದ ತೇವ ವಾರದಲ್ಲಿ ತೆಂಗಿನಲ್ಲಿ ರೈನೋಸೆರಸ್ ಬೀಟಲ್ ಹಾನಿ ಹೆಚ್ಚಾಗುತ್ತದೆ", "ಹೊಸ ತೆರೆದ ಎಲೆಗಳಲ್ಲಿ V-ಕಟ್ ಲಕ್ಷಣ ಕಂಡರೆ ಪಾಮ್‌ವಾರು ಗುರುತು ಮಾಡಿ", "ಕ್ರೌನ್‌ನಲ್ಲಿ ಶಿಫಾರಸಾದ ನ್ಯಾಫ್ಥಲೀನ್/ಬೇವು ಕೇಕ್ ನೀಡಿಕೆ ಮಾಡಿ", "ಕ್ರೌನ್ ರಕ್ಷಣೆ ನಿರಂತರ ಇರಿಸಿದರೆ ಹೊಸ ಎಲೆ ಹಾನಿ ಕಡಿಮೆಯಾಗಿ ಮರ ಶಕ್ತಿ ಉಳಿಯುತ್ತದೆ", "ವೃತ್ತಾಕಾರದ ಬೇಸಿನ್ ಮಾಡಿಸಿ ನೀರು ನಿಲ್ಲದಂತೆ ನಿರ್ವಹಿಸಿ", "ತೋಟದ ಕಸದ ರಾಶಿ ತೆರವುಗೊಳಿಸಿ ಕೀಟ ಸಂವರ್ಧನೆ ಸ್ಥಳ ತಪ್ಪಿಸಿ", "ಉಚ್ಚ")
        "arecanut" -> KnAdvisoryProfile("ಅಡಿಕೆಗೆ ವಾರದ ಸಲಹೆ: ಕೊಳೆರೋಗದ ನಿಗಾ ವಹಿಸಿ.", "ಹೆಚ್ಚಿನ ಮಳೆ ಮತ್ತು ತೇವಾಂಶದ ವಾರದಲ್ಲಿ ಅಡಿಕೆಗೆ ಕೊಳೆರೋಗದ ಅಪಾಯ ಹೆಚ್ಚು", "ಬಲಿಯದ ಅಡಿಕೆ ಉದುರುವುದು ಕಂಡರೆ ತಕ್ಷಣ ದಾಖಲಿಸಿ", "ಬೋರ್ಡೋ ದ್ರಾವಣವನ್ನು ಸಿಂಪಡಿಸಿ", "ರೋಗ ಆರಂಭದಲ್ಲೇ ತಡೆಗಟ್ಟಿದರೆ ಇಳುವರಿ ನಷ್ಟ ತಡೆಯಬಹುದು", "ಗಾಳಿ ಆಡುವಂತೆ ತೋಟ ನಿರ್ವಹಿಸಿ", "ರೋಗಿತ ಅಡಿಕೆಗಳನ್ನು ತೋಟದಿಂದ ತೆಗೆಯಿರಿ", "ಉಚ್ಚ")
        "sugarcane" -> KnAdvisoryProfile("ಕಬ್ಬಿಗೆ ವಾರದ ಸಲಹೆ: ಆರಂಭಿಕ ಶೂಟ್ ಬೋರರ್ ನಿಗಾ ಮತ್ತು ಮೇಲ್ಚೇರಿಕೆ ಗೊಬ್ಬರ ನೀಡಿಕೆ ಮಾಡಿರಿ.", "ಬಿಸಿ ಒಣ ಗಾಳಿಯ ವಾರದಲ್ಲಿ ಕಬ್ಬಿನ ಮೊಳೆಯ ಹಾನಿ ವೇಗವಾಗಿ ಹೆಚ್ಚಬಹುದು", "ಪಿನ್‌ಹೋಲ್ ಎಲೆ ಮತ್ತು ಡೆಡ್ ಹಾರ್ಟ್ ಕಂಡ ಸಾಲುಗಳನ್ನು ತಕ್ಷಣ ಗುರುತು ಮಾಡಿ", "ಶಿಫಾರಸಾದ ಮೇಲ್ಚೇರಿಕೆ N ಮತ್ತು K ನೀಡಿ ನಂತರ ನೀರಾವರಿ ನೀಡಿ", "ಮೊಳೆಯ ಉಳಿವು ಹೆಚ್ಚಿದಷ್ಟೂ ಅಂತಿಮ ಕಬ್ಬು ದಪ್ಪ ಮತ್ತು ಇಳುವರಿ ಉತ್ತಮವಾಗುತ್ತದೆ", "ಸಾಲು ಮಧ್ಯ ಮಣ್ಣೆತ್ತುವಿಕೆ ಮತ್ತು ನೀರಾವರಿ ಚಕ್ರ ನಿಯಮಿತವಾಗಿರಲಿ", "ಪ್ರತಿ ವಾರ ಹಾನಿಗೊಂಡ ಮೊಳೆಯ ಪ್ರಮಾಣ ದಾಖಲಿಸಿ ನಿರ್ಧಾರ ಮಾಡಿ", "ಉಚ್ಚ")
        "coffee" -> KnAdvisoryProfile("ಕಾಫಿಗೆ ವಾರದ ಸಲಹೆ: ಬೆರಿ ಬೋರರ್ ಟ್ರಾಪ್ ಮತ್ತು ತೋಟ ಸ್ವಚ್ಛತೆ ಕ್ರಮ ಬಲಪಡಿಸಿ.", "ಮಳೆ ನಂತರದ ತೇವದಲ್ಲಿ ಕಾಫಿ ಬೆರಿ ಬೋರರ್ ಚಟುವಟಿಕೆ ಹೆಚ್ಚಾಗುವ ಸಾಧ್ಯತೆ ಇದೆ", "ಬೆರಿಗಳ ಮೇಲೆ ಸಣ್ಣ ರಂಧ್ರ ಕಂಡರೆ ಬ್ಲಾಕ್‌ವಾರು ಸೋಂಕು ಮಟ್ಟ ದಾಖಲಿಸಿ", "ಆಲ್ಕೊಹಾಲ್ ಟ್ರಾಪ್ ಅಳವಡಿಸಿ, ಸೋಂಕಿತ ಬೆರಿಗಳನ್ನು ಸಂಗ್ರಹಿಸಿ ನಾಶಮಾಡಿ", "ಮೈದಾನದಲ್ಲಿ ಉಳಿದ ಬೆರಿಗಳು ಮುಂದಿನ ಚಕ್ರದ ಪ್ರಮುಖ ಸೋಂಕು ಮೂಲವಾಗುತ್ತವೆ", "ನೆರಳು ಮರ ನಿರ್ವಹಣೆ ಮೂಲಕ ಸೂಕ್ತ ಬೆಳಕು-ತೇವ ಸಮತೋಲನ ಕಾಪಾಡಿ", "ಕೊಯ್ಲಿನ ಬಳಿಕ ಸ್ಯಾನಿಟೇಶನ್ ರೌಂಡ್ ಕಡ್ಡಾಯವಾಗಿ ಪೂರ್ಣಗೊಳಿಸಿ", "ಉಚ್ಚ")
        else -> KnAdvisoryProfile("ಈ ಬೆಳೆಗಾಗಿ ವಾರದ ಸಲಹೆ: ಹೊಲದ ಸ್ಥಿತಿಗೆ ಅನುಗುಣವಾಗಿ ನಿಗಾವಹಿಸಿ ಕ್ರಮ ಕೈಗೊಳ್ಳಿ.", "ಸ್ಥಳೀಯ ಹವಾಮಾನ ಬದಲಾವಣೆಗಳನ್ನು ಪ್ರತಿದಿನ ಪರಿಶೀಲಿಸಿ", "ಆರಂಭಿಕ ಹಾನಿ ಲಕ್ಷಣಗಳನ್ನು 20 ಗಿಡಗಳಲ್ಲಿ ದಾಖಲಿಸಿ", "ಶಿಫಾರಸಾದ ಬೆಳೆ ಹಂತದ ಡೋಸ್ ಮಾತ್ರ ಬಳಸಿ", "ಸಕಾಲಿಕ ಕ್ರಮದಿಂದ ವೆಚ್ಚ ಕಡಿಮೆ ಮತ್ತು ಇಳುವರಿ ರಕ್ಷಣೆ ಸಾಧ್ಯ", "ಮಣ್ಣಿನ ಪರೀಕ್ಷೆ ಆಧರಿತ ಪೋಷಕ ಹಾಗೂ ನೀರಾವರಿ ನಿರ್ವಹಣೆ ಅನುಸರಿಸಿ", "ವಾರದ ಹೊಲ ದಾಖಲೆಯನ್ನು ಕಡ್ಡಾಯವಾಗಿ ಉಳಿಸಿ", "ಮಧ್ಯಮ")
    }

    private fun generateTimelineTips(cropId: String): List<Tip> {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val cropName = cropId.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
        
        val milestones = when (cropId) {
            "paddy" -> listOf(
                Triple(1, "Land Preparation", "ಜಮೀನು ಸಿದ್ಧತೆ: ಉಳುಮೆ ಮತ್ತು ಹಸಿರು ಗೊಬ್ಬರ ಬಳಕೆ."),
                Triple(15, "Seedbed Management", "ಸಸಿಮಡಿ ನಿರ್ವಹಣೆ: ಬಿತ್ತನೆ ಮತ್ತು ಸಸಿಗಳ ರಕ್ಷಣೆ."),
                Triple(30, "Transplanting", "ನಾಟಿ ಕಾರ್ಯ: ಸಾಲು ನಾಟಿ ಮತ್ತು ತೇವ ನಿರ್ವಹಣೆ."),
                Triple(60, "First Weeding", "ಕಳೆ ನಿಯಂತ್ರಣ: ಮೊದಲ ಕಳೆ ತೆಗೆಯುವುದು ಮತ್ತು ಗೊಬ್ಬರ ನೀಡಿಕೆ."),
                Triple(90, "Pest Monitoring", "ಕೀಟ ನಿಗಾ: ಸುಳಿ ಕೊರಕ ಮತ್ತು ಜಿಗಿ ಕೀಟಗಳ ತಪಾಸಣೆ."),
                Triple(120, "Grain Filling", "ಕಾಳು ತುಂಬುವ ಹಂತ: ನೀರಿನ ಸಮತೋಲನ ಕಾಪಾಡಿ."),
                Triple(145, "Harvesting", "ಕಟಾವು: ಸರಿಯಾದ ತೇವಾಂಶದಲ್ಲಿ ಕಟಾವು ಮಾಡಿ.")
            )
            "tomato" -> listOf(
                Triple(1, "Nursery Sowing", "ಸಸಿಮಡಿ ಬಿತ್ತನೆ: ರೋಗಮುಕ್ತ ಬೀಜಗಳ ಬಳಕೆ."),
                Triple(25, "Transplanting", "ನಾಟಿ: ಸಂಜೆ ವೇಳೆಯಲ್ಲಿ ಸಸಿಗಳ ನಾಟಿ."),
                Triple(40, "Staking & Support", "ಆಸರೆ ನೀಡುವುದು: ಗಿಡಗಳು ವಾಗದಂತೆ ಕಟ್ಟುವಿಕೆ."),
                Triple(55, "Flowering Care", "ಹೂಬಿಡುವ ಹಂತ: ಬೋರಾನ್ ಮತ್ತು ಕ್ಯಾಲ್ಸಿಯಂ ನೀಡಿಕೆ."),
                Triple(70, "Fruit Development", "ಹಣ್ಣಿನ ಬೆಳವಣಿಗೆ: ತೇವಾಂಶ ಏರಿಳಿತ ತಡೆಯಿರಿ."),
                Triple(90, "First Harvest", "ಮೊದಲ ಕಟಾವು: ಬಣ್ಣ ಆಧಾರಿತ ಕೊಯ್ಲು.")
            )
            "onion" -> listOf(
                Triple(1, "Land Preparation", "ಜಮೀನು ಸಿದ್ಧತೆ: ಕೊಟ್ಟಿಗೆ ಗೊಬ್ಬರ ಸೇರಿಸುವುದು."),
                Triple(10, "Sowing/Planting", "ಬಿತ್ತನೆ: ಸಾಲುಗಳ ನಡುವೆ ಅಂತರ ಕಾಪಾಡಿ."),
                Triple(40, "Weeding", "ಕಳೆ ನಿರ್ವಹಣೆ: ಗಡ್ಡೆ ಬೆಳೆಯಲು ಅನುವು ಮಾಡಿಕೊಡಿ."),
                Triple(70, "Bulb Enlargement", "ಗಡ್ಡೆ ದಪ್ಪವಾಗುವ ಹಂತ: ಪೊಟಾಶ್ ನೀಡಿಕೆ."),
                Triple(100, "Harvesting", "ಕಟಾವು: ಎಲೆಗಳು ಒಣಗಿದ ನಂತರ ಕೊಯ್ಲು.")
            )
            else -> listOf(
                Triple(1, "Early Stage", "ಆರಂಭಿಕ ಹಂತ: ಭೂಮಿ ಸಿದ್ಧತೆ ಮತ್ತು ಬಿತ್ತನೆ."),
                Triple(45, "Growth Stage", "ಬೆಳವಣಿಗೆ ಹಂತ: ಪೋಷಕಾಂಶ ಮತ್ತು ನೀರು ನಿರ್ವಹಣೆ."),
                Triple(90, "Maturity", "ಪಕ್ವತೆಯ ಹಂತ: ಕೀಟ ಬಾಧೆ ತಡೆಯುವುದು."),
                Triple(120, "Harvest", "ಕಟಾವು: ಸರಿಯಾದ ಸಮಯದಲ್ಲಿ ಕೊಯ್ಲು.")
            )
        }

        return milestones.map { (dayOffset, taskEn, taskKn) ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, dayOffset - 1)
            val realTimeDate = dateFormat.format(calendar.time)
            
            Tip(
                id = "${cropId}_timeline_$dayOffset",
                cropId = cropId,
                cropName = cropName,
                category = "Daily Timeline",
                instructionEn = "🌱 $taskEn",
                instructionKn = "🌱 $taskKn",
                imageRes = cropImageFor(cropId),
                weather = "Timeline Data",
                stage = "Cultivation Step",
                action = taskEn,
                reason = "Based on standard cultivation practices for $cropName.",
                farmingMethod = "Step-by-step guidance for your crop cycle.",
                priority = if (dayOffset == 1) "High" else "Medium"
            )
        }
    }
}
