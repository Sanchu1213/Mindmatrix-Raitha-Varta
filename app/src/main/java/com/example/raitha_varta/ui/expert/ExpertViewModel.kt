package com.example.raitha_varta.ui.expert

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.BuildConfig
import com.example.raitha_varta.data.ChatMessage
import com.example.raitha_varta.data.Consultation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ExpertViewModel @Inject constructor(
    private val consultationDao: com.example.raitha_varta.data.ConsultationDao
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val consultations = consultationDao.getAllConsultations()
        .map { list ->
            list.map { Consultation(it.id, it.lastMessage, it.timestamp, it.status) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val GEMINI_MODEL = "gemini-2.5-flash"
    private val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"

    fun sendMessage(userQuery: String, image: Bitmap? = null, languageCode: String = "en") {
        if (userQuery.isBlank() && image == null) return

        val userMessage = ChatMessage(text = userQuery, isFromUser = true, image = image)
        _chatMessages.value += userMessage

        viewModelScope.launch {
            _isLoading.value = true

            if (image != null) {
                val analyzingText = "Analyzing image... Consulting agricultural database. ||| ಚಿತ್ರವನ್ನು ವಿಶ್ಲೇಷಿಸಲಾಗುತ್ತಿದೆ... ತಜ್ಞರನ್ನು ಸಂಪರ್ಕಿಸಲಾಗುತ್ತಿದೆ."
                val analyzingMessage = ChatMessage(text = analyzingText, isFromUser = false)
                _chatMessages.update { it + analyzingMessage }

                val finalResponse = try {
                    callGeminiWithImage(image)
                } catch (e: Exception) {
                    android.util.Log.e("ExpertVM", "Gemini image error: ${e.message}", e)
                    getFallbackResponse()
                }

                // Replace placeholder with real response
                _chatMessages.update { current ->
                    current.map { if (it == analyzingMessage) ChatMessage(text = finalResponse, isFromUser = false) else it }
                }
            } else {
                val textResponse = try {
                    callGeminiText(userQuery)
                } catch (e: Exception) {
                    android.util.Log.e("ExpertVM", "Gemini text error: ${e.message}", e)
                    "Thank you for your query. Our agricultural experts will review this shortly. ||| ನಿಮ್ಮ ಪ್ರಶ್ನೆಗೆ ಧನ್ಯವಾದಗಳು. ಶೀಘ್ರದಲ್ಲೇ ಉತ್ತರ ನೀಡಲಾಗುವುದು."
                }
                _chatMessages.update { it + ChatMessage(text = textResponse, isFromUser = false) }
            }
            _isLoading.value = false

            // Persist to DB
            val autoTitle = when {
                userQuery.isNotBlank() ->
                    if (userQuery.length > 40) userQuery.take(37) + "..." else userQuery
                image != null -> {
                    val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                    "Crop Image – ${sdf.format(java.util.Date())}"
                }
                else -> if (languageCode == "kn") "ಚಿತ್ರ ಸಮಾಲೋಚನೆ" else "Image Consultation"
            }
            consultationDao.insertConsultation(
                com.example.raitha_varta.data.ConsultationEntity(
                    id = System.currentTimeMillis().toString(),
                    lastMessage = autoTitle,
                    timestamp = System.currentTimeMillis(),
                    status = "PENDING"
                )
            )
        }
    }

    private suspend fun callGeminiWithImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        // Step 1: Vision - Identify crop and issue using gemini-2.5-flash
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val imageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val idPrompt = "Look carefully and identify this crop and any visible disease, spoilage, or health status. Be brief. Format: Crop: [Name], Issue: [Issue]. If healthy, say 'Healthy'. Common crops: Paddy, Ragi, Maize, Sugarcane, Tomato, Brinjal, Okra, Chilli, Onion, Potato, Mango, Banana, Coconut, Areca nut, Coffee, Mulberry, Cotton, Aloe Vera."

        val visionRequestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", imageBase64)
                            })
                        })
                        put(JSONObject().apply {
                            put("text", idPrompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
            })
        }

        val visionResponse = makeGeminiRequest(visionRequestBody.toString(), "gemini-2.5-flash")
        val identification = extractTextFromResponse(visionResponse) ?: "Unknown Crop / Issue"

        // Step 2: Expert Analysis - Generate detailed bilingual report using gemini-2.5-pro
        val reportPrompt = buildImagePrompt(identification)

        val textRequestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", reportPrompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 8192) // Increased significantly to prevent truncation
            })
        }

        val textResponse = makeGeminiRequest(textRequestBody.toString(), "gemini-2.5-flash")
        extractTextFromResponse(textResponse) ?: getFallbackResponse()
    }

    private suspend fun callGeminiText(query: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are an expert agricultural advisor for Karnataka farmers.
            Answer this farming question clearly and practically: "$query"
            
            Include specific advice relevant to Karnataka's climate and soil conditions.
            Keep response under 120 words. Be direct and helpful.
            
            Respond with English answer first, then Kannada translation, separated by |||
            Format: [English answer] ||| [Kannada translation]
        """.trimIndent()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
        }

        val response = makeGeminiRequest(requestBody.toString(), "gemini-2.5-flash")
        extractTextFromResponse(response)
            ?: "Thank you for your query. Our agricultural experts will review this. ||| ನಿಮ್ಮ ಪ್ರಶ್ನೆಗೆ ಧನ್ಯವಾದಗಳು."
    }

    private fun makeGeminiRequest(body: String, model: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw Exception("API error ${response.code}: $errorBody")
            }
            return response.body?.string() ?: throw Exception("Empty response")
        }
    }

    private fun extractTextFromResponse(responseJson: String): String? {
        return try {
            val json = JSONObject(responseJson)
            val candidates = json.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text").trim()
        } catch (e: Exception) {
            android.util.Log.e("ExpertVM", "Parse error: ${e.message}")
            null
        }
    }

    private fun buildImagePrompt(identification: String): String = """
        You are an expert agricultural scientist. A farmer has shown you a crop that has been identified as follows:
        "$identification"
        
        Provide a highly detailed structured report based on this identification.
        
        Respond in EXACTLY this format with two sections separated by |||:
        
        Expert Analysis Report:
        
        1. What it is: [Expand on the crop name and current condition]
        2. Why it is spoiled: [Thorough explanation of the root cause - environmental, biological, pests, or soil-based]
        3. How to prevent: [Step-by-step methods to prevent this issue from happening in the future]
        4. Solution: [Specific organic and chemical treatments with product names and exact dosage]
        
        Advantages:
        - [Key advantage of growing this crop]
        - [Another advantage]
        
        Disadvantages:
        - [Main challenge or disadvantage of this crop]
        - [Another disadvantage]
        |||
        ತಜ್ಞರ ವಿಶ್ಲೇಷಣಾ ವರದಿ:
        
        ೧. ಇದು ಏನು: [ಬೆಳೆ/ಸಸ್ಯದ ಹೆಸರು ಮತ್ತು ಅದರ ಪ್ರಸ್ತುತ ಸ್ಥಿತಿ/ರೋಗದ ವಿಸ್ತೃತ ಮಾಹಿತಿ]
        ೨. ಇದು ಏಕೆ ಹಾಳಾಗಿದೆ: [ರೋಗ ಅಥವಾ ಸಮಸ್ಯೆಯ ಮೂಲ ಕಾರಣದ ಸಂಪೂರ್ಣ ವಿವರಣೆ - ಪರಿಸರ, ಕೀಟಗಳು, ಅಥವಾ ಮಣ್ಣು]
        ೩. ತಡೆಗಟ್ಟುವ ಕ್ರಮ: [ಭವಿಷ್ಯದಲ್ಲಿ ಈ ಸಮಸ್ಯೆಯನ್ನು ತಡೆಯುವ ಹಂತ-ಹಂತದ ವಿಧಾನಗಳು]
        ೪. ಪರಿಹಾರ: [ನಿರ್ದಿಷ್ಟ ಸಾವಯವ ಮತ್ತು ರಾಸಾಯನಿಕ ಚಿಕಿತ್ಸೆಗಳು, ಔಷಧದ ಹೆಸರು ಮತ್ತು ನಿಖರ ಪ್ರಮಾಣ]
        
        ಅನುಕೂಲಗಳು:
        - [ಈ ಬೆಳೆಯ ಮುಖ್ಯ ಅನುಕೂಲ]
        - [ಮತ್ತೊಂದು ಅನುಕೂಲ]
        
        ಅನಾನುಕೂಲಗಳು:
        - [ಈ ಬೆಳೆಯ ಮುಖ್ಯ ಸವಾಲು ಅಥವಾ ಅನಾನುಕೂಲ]
        - [ಮತ್ತೊಂದು ಅನಾನುಕೂಲ]
        
        Do not output anything else outside this format. Ensure the response is complete and not truncated.
    """.trimIndent()

    private fun getFallbackResponse(): String {
        val en = """
            Expert Analysis Report:
            
            Unable to analyze the image at this time. Please ensure:
            - The image is clear and well-lit
            - The crop/plant is clearly visible in the frame
            - Internet connection is active
            
            Please try again or describe your crop issue in the text box below.
            
            Advantages:
            - Early crop disease detection saves up to 40% yield loss.
            
            Disadvantages:
            - Photo analysis requires a stable internet connection.
            
            Info: For best results, photograph the affected leaf or stem in natural daylight.
        """.trimIndent()

        val kn = """
            ತಜ್ಞರ ವಿಶ್ಲೇಷಣಾ ವರದಿ:
            
            ಈ ಸಮಯದಲ್ಲಿ ಚಿತ್ರವನ್ನು ವಿಶ್ಲೇಷಿಸಲು ಸಾಧ್ಯವಾಗಲಿಲ್ಲ.
            ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ ಅಥವಾ ಕೆಳಗೆ ನಿಮ್ಮ ಬೆಳೆ ಸಮಸ್ಯೆಯನ್ನು ವಿವರಿಸಿ.
            
            ಅನುಕೂಲಗಳು:
            - ಬೆಳೆ ರೋಗ ಮುಂಚಿತ ಪತ್ತೆ ೪೦% ಇಳುವರಿ ನಷ್ಟ ತಡೆಯುತ್ತದೆ.
            
            ಅನಾನುಕೂಲಗಳು:
            - ಚಿತ್ರ ವಿಶ್ಲೇಷಣೆಗೆ ಇಂಟರ್ನೆಟ್ ಅಗತ್ಯ.
            
            ಸಲಹೆ: ಹಗಲು ಬೆಳಕಿನಲ್ಲಿ ಪೀಡಿತ ಎಲೆ ಅಥವಾ ಕಾಂಡದ ಚಿತ್ರ ತೆಗೆಯಿರಿ.
        """.trimIndent()

        return "$en ||| $kn"
    }

    fun startNewConsultation(initialImage: Bitmap? = null, languageCode: String = "en") {
        _chatMessages.value = listOf(
            ChatMessage(
                text = if (languageCode == "kn")
                    "ತಜ್ಞರ ಪ್ರಶ್ನೆಗೆ ಸ್ವಾಗತ. ಬೆಳೆ ಚಿತ್ರವನ್ನು ಅಪ್‌ಲೋಡ್ ಮಾಡಿ ಅಥವಾ ಪ್ರಶ್ನೆ ಕೇಳಿ — ನಮ್ಮ Gemini 2.5 AI ತಜ್ಞರು ನಿಮಗೆ ಸಹಾಯ ಮಾಡುತ್ತಾರೆ."
                else "Welcome! Upload a crop photo or ask a question — our Gemini 2.5 AI expert will identify the crop and provide detailed guidance.",
                isFromUser = false
            )
        )
        if (initialImage != null) {
            sendMessage("", initialImage, languageCode)
        }
    }

    fun renameConsultation(consultationId: String, newTitle: String) {
        viewModelScope.launch {
            consultationDao.updateTitle(consultationId, newTitle.trim())
        }
    }

    fun deleteConsultation(consultationId: String) {
        viewModelScope.launch {
            consultationDao.deleteConsultation(
                com.example.raitha_varta.data.ConsultationEntity(
                    id = consultationId,
                    lastMessage = "",
                    timestamp = 0,
                    status = ""
                )
            )
        }
    }
}
