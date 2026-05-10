package com.example.raitha_varta.ui.disease.data

import android.content.Context
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the crop disease detection model using Firebase ML Model Downloader
 * and TensorFlow Lite for inference.
 */
@Singleton
class CropDiseaseModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null
    private val modelName = "crop_disease_model" // Name of the model in Firebase Console
    private val labelList: List<String> = arrayListOf(
        "Healthy",
        "Blight",
        "Brown Spot",
        "Leaf Curl",
        "Mosaic Virus",
        "Powdery Mildew",
        "Rust",
        "Yellow Vein Mosaic"
    )

    private val imageSize = 224
    private val imageMean = 127.5f
    private val imageStd = 127.5f
    private val probabilityThreshold = 0.5f

    private val imgProcessor: ImageProcessor
    private val probabilityProcessor: TensorProcessor

    init {
        // Initialize image processor for model input
        imgProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(imageMean, imageStd))
            .build()

        // Initialize probability processor for model output
        probabilityProcessor = TensorProcessor.Builder().build()

        // Initialize the model
        initializeModel()
    }

    /**
     * Downloads the crop disease detection model from Firebase and initializes the interpreter.
     */
    private fun initializeModel() {
        val conditions = CustomModelDownloadConditions.Builder()
            // Allow downloads over any network (WiFi or mobile data) for better accessibility
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel(modelName, DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
            .addOnSuccessListener { model: CustomModel ->
                val modelFile = model.file
                if (modelFile != null) {
                    Log.d("CropDiseaseML", "Successfully downloaded crop disease model: ${modelFile.absolutePath}")
                    try {
                        interpreter = Interpreter(modelFile)
                        Log.d("CropDiseaseML", "TensorFlow Lite interpreter initialized for crop disease detection.")
                    } catch (e: Exception) {
                        Log.e("CropDiseaseML", "Failed to initialize TFLite interpreter", e)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CropDiseaseML", "Failed to download crop disease model: $modelName", exception)
                // Fallback to local asset model if Firebase download fails
                loadFallbackModel()
            }
    }

    /**
     * Loads a fallback model from assets if Firebase download fails.
     */
    private fun loadFallbackModel() {
        try {
            interpreter = Interpreter(loadModelFile(context, "crop_disease_model.tflite"))
            Log.d("CropDiseaseML", "Loaded fallback crop disease model from assets.")
        } catch (e: Exception) {
            Log.e("CropDiseaseML", "Failed to load fallback model", e)
        }
    }

    /**
     * Loads a model file from assets.
     */
    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, filename)
    }

    /**
     * Runs inference on the provided bitmap image to detect crop diseases.
     *
     * @param bitmap The input image as a Bitmap
     * @return A map of disease names to their confidence scores
     */
    fun predictDisease(bitmap: android.graphics.Bitmap): Map<String, Float> {
        val currentInterpreter = interpreter
        if (currentInterpreter == null) {
            Log.w("CropDiseaseML", "Interpreter not initialized")
            return emptyMap()
        }

        // Preprocess the image
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = imgProcessor.process(tensorImage)

        // Get output tensor info
        val outputShape = currentInterpreter.getOutputTensor(0).shape() // [1, num_classes]
        val probabilityBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        // Run inference
        currentInterpreter.run(processedImage.buffer, probabilityBuffer.buffer.rewind())

        // Process output
        val labels = try {
            FileUtil.loadLabels(context, "crop_disease_labels.txt")
        } catch (e: Exception) {
            Log.w("CropDiseaseML", "Could not load labels file, using fallback list")
            labelList
        }
        
        val probabilities = TensorLabel(labels, probabilityProcessor.process(probabilityBuffer))
            .mapWithFloatValue

        // Filter by threshold and return results
        return probabilities.filterValues { it >= probabilityThreshold }
    }

    /**
     * Gets the top prediction from the model.
     *
     * @param bitmap The input image as a Bitmap
     * @return A pair containing the disease name and confidence score, or null if below threshold
     */
    fun getTopPrediction(bitmap: android.graphics.Bitmap): Pair<String, Float>? {
        val predictions = predictDisease(bitmap)
        return if (predictions.isNotEmpty()) {
            predictions.maxByOrNull { it.value }?.let { it.key to it.value }
        } else {
            // Return healthy as default if no disease detected above threshold
            Pair("Healthy", 1.0f)
        }
    }
}
