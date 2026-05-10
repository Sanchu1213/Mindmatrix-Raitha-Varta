package com.example.raitha_varta.ui.disease

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.ui.disease.data.CropDiseaseModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DiseaseUiState {
    object Idle : DiseaseUiState()
    object Analyzing : DiseaseUiState()
    data class Result(
        val diseaseName: String,
        val severity: String,
        val description: String,
        val symptoms: String,
        val treatment: String,
        val prevention: String,
        val organicRemedy: String,
        val confidence: Float = 0.0f
    ) : DiseaseUiState()
    data class Error(val message: String) : DiseaseUiState()
}

/**
 * A simple data class to hold 5 related values, solving destructuring ambiguity.
 */
data class Tuple5<out A, out B, out C, out D, out E>(
    val v1: A,
    val v2: B,
    val v3: C,
    val v4: D,
    val v5: E
)

@HiltViewModel
class CropDiseaseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diseaseModelManager: CropDiseaseModelManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiseaseUiState>(DiseaseUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage = _capturedImage.asStateFlow()

    fun setImage(bitmap: Bitmap) {
        _capturedImage.value = bitmap
        _uiState.value = DiseaseUiState.Idle
    }

    fun analyzeDisease(languageCode: String = "en") {
        val image = _capturedImage.value ?: return
        viewModelScope.launch {
            _uiState.value = DiseaseUiState.Analyzing
            try {
                // Get top prediction from TensorFlow Lite model
                val prediction = diseaseModelManager.getTopPrediction(image)
                val diseaseName = prediction?.first ?: "Unknown"
                val confidence = prediction?.second ?: 0.0f
                
                // Determine severity based on confidence (this is a simplified approach)
                val severity = when {
                    diseaseName.equals("Healthy", ignoreCase = true) -> "None"
                    confidence >= 0.8 -> "High"
                    confidence >= 0.6 -> "Moderate"
                    confidence >= 0.4 -> "Low"
                    else -> "Very Low"
                }
                
                // Generate detailed information based on the disease
                // Tuple5 provides component1() through component5() for destructuring
                val (description, symptoms, treatment, prevention, organicRemedy) = 
                    generateDiseaseInfo(diseaseName, languageCode)
                
                _uiState.value = DiseaseUiState.Result(
                    diseaseName = diseaseName,
                    severity = severity,
                    description = description,
                    symptoms = symptoms,
                    treatment = treatment,
                    prevention = prevention,
                    organicRemedy = organicRemedy,
                    confidence = confidence
                )
            } catch (e: Exception) {
                _uiState.value = DiseaseUiState.Error("Disease analysis failed: ${e.message}")
            }
        }
    }

    /**
     * Generates disease information based on the detected disease and language.
     * Fixed Kannada translations and resolved Tuple5 ambiguity.
     */
    private fun generateDiseaseInfo(diseaseName: String, languageCode: String): 
            Tuple5<String, String, String, String, String> {
        val isKn = languageCode == "kn"
        
        return when (diseaseName.lowercase()) {
            "healthy" -> Tuple5(
                if (isKn) "ಸಸ್ಯವು ಆರೋಗ್ಯಕರವಾಗಿ ಕಂಡುಬರುತ್ತದೆ ಮತ್ತು ಯಾವುದೇ ರೋಗದ ಲಕ್ಷಣಗಳಿಲ್ಲ." else "The plant appears healthy with no signs of disease.",
                if (isKn) "ಯಾವುದೇ ಲಕ್ಷಣಗಳು ಕಂಡುಬಂದಿಲ್ಲ." else "No symptoms present.",
                if (isKn) "ಯಾವುದೇ ಚಿಕಿತ್ಸೆಯ ಅಗತ್ಯವಿಲ್ಲ." else "No treatment required.",
                if (isKn) "ನಿಯಮಿತ ಮೇಲ್ವಿಚಾರಣೆ ಮತ್ತು ಸರಿಯಾದ ನೀರಾವರಿ." else "Regular monitoring and proper irrigation.",
                if (isKn) "ಯಾವುದೇ ಸಾವಯವ ಪರಿಹಾರದ ಅಗತ್ಯವಿಲ್ಲ." else "No organic remedy needed."
            )
            "blight" -> Tuple5(
                if (isKn) "ಬ್ಲೈಟ್: ಎಲೆಗಳು, ಕಾಂಡಗಳು ಅಥವಾ ಹೂವುಗಳು ವೇಗವಾಗಿ ಕಂದು ಬಣ್ಣಕ್ಕೆ ತಿರುಗಿ ಬಾಡುತ್ತವೆ." else "Blight: Rapid browning and wilting of leaves, stems, or flowers.",
                if (isKn) "ಎಲೆಗಳ ಮೇಲೆ ಕಂದು ಬಣ್ಣದ ಚುಕ್ಕೆಗಳು, ಕಾಂಡದ ಗಾಯಗಳು, ಬಾಡುವಿಕೆ." else "Brown spots on leaves, stem lesions, wilting.",
                if (isKn) "ಪೀಡಿತ ಭಾಗಗಳನ್ನು ತೆಗೆದುಹಾಕಿ, ಶಿಲೀಂಧ್ರನಾಶಕವನ್ನು ಅನ್ವಯಿಸಿ." else "Remove affected parts, apply fungicide.",
                if (isKn) "ಬಸಿದುಹೋಗುವಿಕೆಯನ್ನು ಸುಧಾರಿಸಿ, ಎಲೆಗಳ ಮೇಲೆ ನೀರು ಹಾಕಬೇಡಿ, ಉತ್ತಮ ಗಾಳಿಯ ಸಂಚಾರವನ್ನು ಖಚಿತಪಡಿಸಿಕೊಳ್ಳಿ." else "Improve drainage, avoid overhead watering, ensure good air circulation.",
                if (isKn) "ಬೇವಿನ ಎಣ್ಣೆ ಸಿಂಪಡಣೆ, ಅಡುಗೆ ಸೋಡಾ ದ್ರಾವಣ." else "Neem oil spray, baking soda solution"
            )
            "brown spot" -> Tuple5(
                if (isKn) "ಬ್ರೌನ್ ಸ್ಪಾಟ್: ಎಲೆಗಳ ಮೇಲೆ ಸಣ್ಣ ಕಂದು ಬಣ್ಣದ ಗಾಯಗಳು ಉಂಟಾಗುತ್ತವೆ." else "Brown Spot: Small brown lesions develop on leaves.",
                if (isKn) "ಎಲೆಗಳ ಮೇಲೆ ಹಳದಿ ಬಣ್ಣದ ವಲಯಗಳಿರುವ ವೃತ್ತಾಕಾರದ ಕಂದು ಚುಕ್ಕೆಗಳು." else "Circular brown spots with yellow halos on leaves.",
                if (isKn) "ಪೀಡಿತ ಎಲೆಗಳನ್ನು ತೆಗೆದುಹಾಕಿ, ತಾಮ್ರ ಆಧಾರಿತ ಶಿಲೀಂಧ್ರನಾಶಕವನ್ನು ಬಳಸಿ." else "Remove affected leaves, apply copper-based fungicide.",
                if (isKn) "ಎಲೆಗಳನ್ನು ಒದ್ದೆ ಮಾಡಬೇಡಿ, ಗಿಡಗಳ ನಡುವೆ ಸರಿಯಾದ ಅಂತರವನ್ನು ಕಾಯ್ದುಕೊಳ್ಳಿ." else "Avoid wetting foliage, provide adequate plant spacing.",
                if (isKn) "ಬೇವಿನ ಎಣ್ಣೆ, ತಾಮ್ರದ ಸಾಬೂನು ಸಿಂಪಡಣೆ." else "Neem oil, copper soap spray"
            )
            "leaf curl" -> Tuple5(
                if (isKn) "ಎಲೆ ಸುರುಳಿ: ಎಲೆಗಳು ಅಂಚುಗಳಿಂದ ಒಳಕ್ಕೆ ಸುರುಳಿಯಾಗಲು ಪ್ರಾರಂಭಿಸುತ್ತವೆ." else "Leaf Curl: Leaves curl upward or downward from the edges.",
                if (isKn) "ಎಲೆಗಳ ವಿರೂಪ ಮತ್ತು ಹಳದಿ ಬಣ್ಣ, ಬೆಳವಣಿಗೆ ಕುಂಠಿತವಾಗುವುದು." else "Leaf distortion and yellowing, stunted growth.",
                if (isKn) "ವೈರಸ್ ಹರಡುವ ಕೀಟಗಳನ್ನು ನಿಯಂತ್ರಿಸಿ, ಪೀಡಿತ ಗಿಡಗಳನ್ನು ನಾಶಪಡಿಸಿ." else "Control insect vectors, remove and destroy infected plants.",
                if (isKn) "ವೈರಸ್ ಮುಕ್ತ ಸಸಿಗಳನ್ನು ಬಳಸಿ, ಕೀಟ ನಿರೋಧಕ ಬಲೆಗಳನ್ನು ಅಳವಡಿಸಿ." else "Use virus-free planting material, install insect nets.",
                if (isKn) "ಬೆಳ್ಳುಳ್ಳಿ ಸಾರ, ಸಮುದ್ರ ಪಾಚಿ ಸಿಂಪಡಣೆ." else "Garlic extract, seaweed spray"
            )
            "mosaic virus" -> Tuple5(
                if (isKn) "ಮೊಸಾಯಿಕ್ ವೈರಸ್: ಎಲೆಗಳ ಮೇಲೆ ತಿಳಿ ಮತ್ತು ಗಾಢ ಹಸಿರು ಬಣ್ಣದ ಕಲೆಗಳು ಕಾಣಿಸಿಕೊಳ್ಳುತ್ತವೆ." else "Mosaic Virus: Mottled light and dark green patterns on leaves.",
                if (isKn) "ಎಲೆಗಳ ಸುಕ್ಕುಗಟ್ಟುವಿಕೆ ಮತ್ತು ವಿರೂಪ, ಇಳುವರಿ ಕಡಿಮೆ." else "Leaf puckering and distortion, reduced yield.",
                if (isKn) "ಯಾವುದೇ ರಾಸಾಯನಿಕ ಚಿಕಿತ್ಸೆ ಇಲ್ಲ. ಪೀಡಿತ ಗಿಡಗಳನ್ನು ನಾಶಪಡಿಸಿ." else "No chemical cure. Remove and destroy infected plants.",
                if (isKn) "ಪ್ರಮಾಣೀಕೃತ ವೈರಸ್ ಮುಕ್ತ ಬೀಜಗಳನ್ನು ಬಳಸಿ, ಕೀಟಗಳನ್ನು ನಿಯಂತ್ರಿಸಿ." else "Use certified virus-free seeds, control aphids and whiteflies.",
                if (isKn) "ಹಾಲಿನ ದ್ರಾವಣ (1:9 ಅನುಪಾತ), ಸಮುದ್ರ ಪಾಚಿ ಸಾರ." else "Milk solution (1:9 dilution), seaweed extract"
            )
            "powdery mildew" -> Tuple5(
                if (isKn) "ಬೂದಿ ರೋಗ: ಎಲೆಗಳ ಮೇಲ್ಮೈಯಲ್ಲಿ ಬಿಳಿ ಬಣ್ಣದ ಪುಡಿಯಂತಹ ಬೆಳವಣಿಗೆ ಕಾಣಿಸಿಕೊಳ್ಳುತ್ತದೆ." else "Powdery Mildew: White powdery growth on leaf surfaces.",
                if (isKn) "ಬಿಳಿ ಪುಡಿಯ ಚುಕ್ಕೆಗಳು, ಎಲೆಗಳ ವಿರೂಪ, ಅಕಾಲಿಕ ಎಲೆ ಉದುರುವಿಕೆ." else "White powdery patches, leaf distortion, premature leaf drop.",
                if (isKn) "ಗಂಧಕ ಆಧಾರಿತ ಶಿಲೀಂಧ್ರನಾಶಕ ಅಥವಾ ಪೊಟ್ಯಾಸಿಯಮ್ ಬೈಕಾರ್ಬನೇಟ್ ಬಳಸಿ." else "Apply sulfur-based fungicide or potassium bicarbonate.",
                if (isKn) "ಗಾಳಿಯ ಸಂಚಾರಕ್ಕೆ ಸರಿಯಾದ ಅಂತರವನ್ನು ಖಚಿತಪಡಿಸಿಕೊಳ್ಳಿ, ಮೇಲೆ ನೀರು ಹಾಕಬೇಡಿ." else "Ensure proper spacing for air flow, avoid overhead watering.",
                if (isKn) "ಹಾಲು ಸಿಂಪಡಣೆ, ಬೇವಿನ ಎಣ್ಣೆ ಬಳಕೆ." else "Milk spray, neem oil application"
            )
            "rust" -> Tuple5(
                if (isKn) "ತುಕ್ಕು ರೋಗ: ಎಲೆಗಳ ಕೆಳಭಾಗದಲ್ಲಿ ಕಿತ್ತಳೆ-ಕಂದು ಬಣ್ಣದ ಗುಳ್ಳೆಗಳು ಉಂಟಾಗುತ್ತವೆ." else "Rust: Orange-brown pustules form on the undersides of leaves.",
                if (isKn) "ಕಿತ್ತಳೆ-ಕಂದು ಚುಕ್ಕೆಗಳು, ಎಲೆಗಳ ಹಳದಿ ಬಣ್ಣ ಮತ್ತು ಉದುರುವಿಕೆ." else "Orange-brown spots on leaves, yellowing and leaf drop.",
                if (isKn) "ಟ್ರಯಾಜೋಲ್ ಆಧಾರಿತ ಶಿಲೀಂಧ್ರನಾಶಕವನ್ನು ಬಳಸಿ." else "Apply triazole-based fungicide.",
                if (isKn) "ಆರ್ದ್ರತೆಯ ಮಟ್ಟವನ್ನು ನಿರ್ವಹಿಸಿ, ಎಲೆಗಳನ್ನು ಒದ್ದೆ ಮಾಡಬೇಡಿ." else "Manage humidity levels, avoid wetting foliage.",
                if (isKn) "ಬೇವಿನ ಎಣ್ಣೆ ಸಿಂಪಡಣೆ, ಗಂಧಕದ ಪುಡಿ." else "Neem oil spray, sulfur dust"
            )
            "yellow vein mosaic" -> Tuple5(
                if (isKn) "ಹಳದಿ ನರ ಮೊಸಾಯಿಕ್: ಎಲೆಗಳ ನರಗಳು ಹಳದಿಯಾಗುತ್ತವೆ, ಆದರೆ ಉಳಿದ ಭಾಗ ಹಸಿರಾಗಿರುತ್ತದೆ." else "Yellow Vein Mosaic: Veins turn yellow while interveinal areas remain green.",
                if (isKn) "ಬೆಳವಣಿಗೆ ಕುಂಠಿತವಾಗುವುದು ಮತ್ತು ವಿರೂಪಗೊಳ್ಳುವುದು." else "Leaf stunting and distortion, reduced plant vigor.",
                if (isKn) "ಬಿಳಿ ನೊಣಗಳನ್ನು ನಿಯಂತ್ರಿಸಿ, ಪೀಡಿತ ಗಿಡಗಳನ್ನು ನಾಶಪಡಿಸಿ." else "Control whitefly vectors, remove infected plants.",
                if (isKn) "ವೈರಸ್ ಮುಕ್ತ ಸಸಿಗಳನ್ನು ಬಳಸಿ, ಹಳದಿ ಜಿಗುಟು ಬಲೆಗಳನ್ನು ಅಳವಡಿಸಿ." else "Use virus-free transplants, install yellow sticky traps.",
                if (isKn) "ಬೇವಿನ ಎಣ್ಣೆ, ಕೀಟನಾಶಕ ಸಾಬೂನು." else "Neem oil, insecticidal soap"
            )
            else -> Tuple5(
                if (isKn) "ಅಜ್ಞಾತ ಸ್ಥಿತಿ ಪತ್ತೆಯಾಗಿದೆ." else "Unknown condition detected",
                if (isKn) "ವಿವರಗಳು ಲಭ್ಯವಿಲ್ಲ." else "Details not available",
                if (isKn) "ಕೃಷಿ ವಿಸ್ತರಣಾ ಸೇವೆಗಳನ್ನು ಸಂಪರ್ಕಿಸಿ." else "Consult agricultural extension services",
                if (isKn) "ಸಾಮಾನ್ಯ ಸಸ್ಯ ಆರೈಕೆ ಕ್ರಮಗಳನ್ನು ಅನುಸರಿಸಿ." else "Follow general plant care practices",
                if (isKn) "ಸಾವಯವ ವಿಧಾನಗಳು." else "Organic approaches"
            )
        }
    }

    private fun parseResult(text: String) {
        try {
            val cleaned = text
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val disease = extractJson(cleaned, "disease_name")
            val severity = extractJson(cleaned, "severity")
            val desc = extractJson(cleaned, "description")
            val symptoms = extractJson(cleaned, "symptoms")
            val treatment = extractJson(cleaned, "treatment")
            val prevention = extractJson(cleaned, "prevention")
            val organic = extractJson(cleaned, "organic_remedy")
            _uiState.value = DiseaseUiState.Result(
                diseaseName = disease,
                severity = severity,
                description = desc,
                symptoms = symptoms,
                treatment = treatment,
                prevention = prevention,
                organicRemedy = organic
            )
        } catch (e: Exception) {
            // Fallback: show raw text
            _uiState.value = DiseaseUiState.Result(
                diseaseName = "Analysis Complete",
                severity = "See details",
                description = text,
                symptoms = "-", treatment = "-", prevention = "-", organicRemedy = "-"
            )
        }
    }

    private fun extractJson(json: String, key: String): String {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]*?)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: "N/A"
    }

    fun reset() {
        _capturedImage.value = null
        _uiState.value = DiseaseUiState.Idle
    }

    private fun userFriendlyAiError(e: Exception): String {
        val raw = e.message.orEmpty()
        return when {
            raw.contains("NOT_FOUND", ignoreCase = true) || raw.contains("model", ignoreCase = true) ->
                "AI model unavailable currently. Please retry shortly."
            raw.contains("API_KEY", ignoreCase = true) || raw.contains("PERMISSION", ignoreCase = true) ->
                "AI authentication failed. Check app AI configuration."
            raw.contains("NETWORK", ignoreCase = true) || raw.contains("timeout", ignoreCase = true) ->
                "Network issue during disease scan. Check internet and retry."
            else -> "Disease analysis failed. Please try again."
        }
    }
}
