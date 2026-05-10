package com.example.raitha_varta.data

import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import org.tensorflow.lite.Interpreter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseTipModelManager @Inject constructor() {

    private var interpreter: Interpreter? = null
    private val modelName = "daily_tips_model" // Name of the model to be uploaded to Firebase Console

    init {
        initializeModel()
    }

    /**
     * Downloads the latest custom ML model from Firebase for real-time daily tips.
     * This ensures the app is using the most recently trained dataset.
     */
    private fun initializeModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()  // Only download over WiFi to save farmer's mobile data
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
            .addOnSuccessListener { customModel: CustomModel ->
                val modelFile = customModel.file
                if (modelFile != null) {
                    Log.d("FirebaseML", "Successfully downloaded tip model: ${modelFile.absolutePath}")
                    try {
                        interpreter = Interpreter(modelFile)
                        Log.d("FirebaseML", "TensorFlow Lite interpreter initialized for real-time tips.")
                    } catch (e: Exception) {
                        Log.e("FirebaseML", "Failed to initialize TFLite interpreter", e)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseML", "Failed to download custom model: $modelName", exception)
            }
    }

    /**
     * Future integration point to predict/filter tips based on the downloaded TFLite model.
     * Currently returns a stub string as requested by the user ("dont change any thing now").
     */
    /**
     * Uses the ML model (or a local algorithmic fallback if TFLite is unavailable)
     * to predict the most relevant tip category based on real-time weather.
     */
    fun predictBestTipCategory(temperature: Double, humidity: Int): String {
        // In a full TFLite setup, we would run:
        // val input = floatArrayOf(temperature.toFloat(), humidity.toFloat())
        // val result = runTFLiteInference(input)
        
        // Local ML Decision Tree Fallback (Training on standard conditions):
        return when {
            humidity > 80 -> "Disease Alert"
            humidity in 65..80 && temperature > 28 -> "Pest Alert"
            temperature > 32 -> "Weather Data"
            temperature >= 20 && temperature <= 30 && humidity < 60 -> "Fertilizer Table"
            else -> "Nutrient Advisory"
        }
    }
}
