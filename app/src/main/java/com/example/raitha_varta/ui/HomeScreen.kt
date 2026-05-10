package com.example.raitha_varta.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.raitha_varta.data.Tip
import com.example.raitha_varta.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: TipViewModel,
    cropFilter: String? = null,
    onBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null
) {
    LaunchedEffect(cropFilter) {
        viewModel.setCropFilter(cropFilter)
    }

    val uiState by viewModel.uiState.collectAsState()
    val useKannada by viewModel.useKannada.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val state = uiState) {
            is TipUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF4CAF50))
            }
            is TipUiState.Success -> {
                if (state.tips.isEmpty()) {
                    EmptyTipsState(cropFilter, onBack)
                } else {
                    val pagerState = rememberPagerState(pageCount = { state.tips.size })
                    val scope = rememberCoroutineScope()
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        TipCard(
                            tip = state.tips[page],
                            useKannada = useKannada
                        )
                    }
                    
                    TipPageIndicator(
                        currentPage = pagerState.currentPage,
                        pageCount = state.tips.size,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 80.dp)
                    )

                    // Scroll navigation buttons — white elevated circular style (right side)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous tip button
                        AnimatedVisibility(visible = pagerState.currentPage > 0) {
                            Surface(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(6.dp, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                                color = Color.White,
                                tonalElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous tip",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                        // Divider line between buttons
                        if (pagerState.currentPage > 0 && pagerState.currentPage < state.tips.size - 1) {
                            Box(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(0.5.dp)
                                    .background(Color.Transparent) // Removed divider for row layout
                            )
                        }
                        // Next tip button
                        AnimatedVisibility(visible = pagerState.currentPage < state.tips.size - 1) {
                            Surface(
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .shadow(6.dp, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
                                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                                color = Color.White,
                                tonalElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Next tip",
                                        tint = Color(0xFF388E3C),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is TipUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }

        // Global navigation/language overlay for all Tip states
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateToSettings != null) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = { viewModel.toggleLanguage() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Language, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (useKannada) "English" else "ಕನ್ನಡ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: Tip, useKannada: Boolean) {
    val context = LocalContext.current
    val speechText = if (useKannada) tip.instructionKn else tip.instructionEn
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        var localTts: TextToSpeech? = null
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = if (useKannada) Locale("kn", "IN") else Locale.US
                val result = localTts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                isTtsReady = false
            }
        }
        localTts = textToSpeech
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            isSpeaking = false
            tts = null
        }
    }

    LaunchedEffect(useKannada) {
        tts?.let { engine ->
            val locale = if (useKannada) Locale("kn", "IN") else Locale.US
            val result = engine.setLanguage(locale)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    LaunchedEffect(tip.id) {
        tts?.stop()
        isSpeaking = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val cropName = localizedCropName(tip.cropId, tip.cropName, useKannada)
        val categoryName = localizedCategoryName(tip.category, useKannada)

        // 1. FULLSCREEN IMAGE
        Image(
            painter = painterResource(id = tip.imageRes),
            contentDescription = cropName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. LAYERED GRADIENT SCRIM
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.95f)
                        ),
                        startY = 0f
                    )
                )
        )

        // 3. CONTENT AREA — two-column bottom layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT COLUMN: Badge + Crop name + Headline + Speaker
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Crop Category Badge
                Surface(
                    color = when(tip.category) {
                        "Pest Alert" -> Color(0xFFD32F2F)
                        "Weather Data" -> Color(0xFF1976D2)
                        "Fertilizer Table" -> Color(0xFF388E3C)
                        "Daily Timeline" -> Color(0xFF673AB7)
                        else -> Color(0xFFF57C00)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Category, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = categoryName.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crop Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = cropName.uppercase(),
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Headline
                Text(
                    text = if (useKannada) tip.instructionKn else tip.instructionEn,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 30.sp,
                    fontSize = 23.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Speaker Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledIconButton(
                        onClick = {
                            val engine = tts ?: return@FilledIconButton
                            if (!isTtsReady) return@FilledIconButton
                            if (isSpeaking) {
                                engine.stop()
                                isSpeaking = false
                            } else {
                                val speakResult = engine.speak(
                                    speechText,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "tip_${tip.id}"
                                )
                                isSpeaking = speakResult == TextToSpeech.SUCCESS
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSpeaking) Color(0xFF81C784).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.2f)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = when {
                            !isTtsReady -> if (useKannada) "ಧ್ವನಿ ಲಭ್ಯವಿಲ್ಲ" else "Voice unavailable"
                            isSpeaking -> if (useKannada) "ನಿಲ್ಲಿಸಲು ಮತ್ತೆ ಒತ್ತಿ" else "Tap again to stop"
                            else -> if (useKannada) "ಸ್ವೈಪ್ ಮಾಡಿ" else "Swipe for more"
                        },
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // RIGHT COLUMN: Advisory details
            Column(
                modifier = Modifier
                    .width(175.dp)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val weatherLabel = if (useKannada) "ಹವಾಮಾನ" else "Weather"
                val stageLabel = if (useKannada) "ಕೀಟ/ರೋಗ" else "Pest"
                val actionLabel = if (useKannada) "ಡೋಸ್" else "Dose"
                val reasonLabel = if (useKannada) "ಏಕೆ?" else "Why now?"
                val methodLabel = if (useKannada) "ವಿಧಾನ" else "Method"
                val priorityLabel = if (useKannada) "ಆದ್ಯತೆ" else "Priority"
                val updateLabel = if (useKannada) "ನವೀಕರಿಸಲಾಗಿದೆ" else "Updated"

                if (tip.weather.isNotBlank()) {
                    AdvisoryRow(
                        icon = { Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFFF176), modifier = Modifier.size(14.dp)) },
                        label = weatherLabel,
                        value = localizeBi(tip.weather, useKannada)
                    )
                }
                if (tip.stage.isNotBlank()) {
                    AdvisoryRow(
                        icon = { Icon(Icons.Default.BugReport, null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(14.dp)) },
                        label = stageLabel,
                        value = localizeBi(tip.stage, useKannada)
                    )
                }
                if (tip.action.isNotBlank()) {
                    AdvisoryRow(
                        icon = { Icon(Icons.Default.Science, null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(14.dp)) },
                        label = actionLabel,
                        value = localizeBi(tip.action, useKannada)
                    )
                }
                if (tip.reason.isNotBlank()) {
                    AdvisoryRow(
                        icon = { Icon(Icons.Default.Bolt, null, tint = Color(0xFF90CAF9), modifier = Modifier.size(14.dp)) },
                        label = reasonLabel,
                        value = localizeBi(tip.reason, useKannada)
                    )
                }
                if (tip.farmingMethod.isNotBlank()) {
                    AdvisoryRow(
                        icon = { Icon(Icons.Default.Info, null, tint = Color(0xFFCE93D8), modifier = Modifier.size(14.dp)) },
                        label = methodLabel,
                        value = localizeBi(tip.farmingMethod, useKannada)
                    )
                }
                AdvisoryRow(
                    icon = { Icon(Icons.Default.Info, null, tint = Color(0xFFE0E0E0), modifier = Modifier.size(14.dp)) },
                    label = priorityLabel,
                    value = localizeBi(tip.priority, useKannada)
                )
            }
        }
    }
}

private fun localizeBi(value: String, useKannada: Boolean): String {
    val parts = value.split("|||")
    if (parts.size < 2) return value
    return if (useKannada) parts[1].trim() else parts[0].trim()
}

private fun tipSource(useKannada: Boolean): String =
    if (useKannada) {
        "ICAR-KVK ಸಾಪ್ತಾಹಿಕ ಬುಲೆಟಿನ್ + ಜಿಲ್ಲಾ ಕೃಷಿ ಹವಾಮಾನ ಘಟಕ."
    } else {
        "ICAR-KVK weekly bulletin + district agromet unit."
    }

private fun localizedCategoryName(category: String, useKannada: Boolean): String {
    if (!useKannada) return category
    return when (category) {
        "Pest Alert" -> "ಕೀಟ ಎಚ್ಚರಿಕೆ"
        "Disease Alert" -> "ರೋಗ ಎಚ್ಚರಿಕೆ"
        "Weather Data" -> "ಹವಾಮಾನ ಮಾಹಿತಿ"
        "Fertilizer Table" -> "ಗೊಬ್ಬರ ಮಾರ್ಗದರ್ಶಿ"
        "Nutrient Advisory" -> "ಪೋಷಕಾಂಶ ಸಲಹೆ"
        "KVK Weekly Advisory" -> "KVK ವಾರದ ಸಲಹೆ"
        "Daily Timeline" -> "ದಿನನಿತ್ಯದ ವೇಳಾಪಟ್ಟಿ"
        else -> category
    }
}

private fun localizedCropName(cropId: String, defaultName: String, useKannada: Boolean): String {
    if (!useKannada) return defaultName
    return when (cropId) {
        "paddy" -> "ಭತ್ತ"
        "wheat" -> "ಗೋಧಿ"
        "ragi" -> "ರಾಗಿ"
        "maize" -> "ಮೆಕ್ಕೆಜೋಳ"
        "bajra" -> "ಸಜ್ಜೆ"
        "jowar" -> "ಜೋಳ"
        "tomato" -> "ಟೊಮ್ಯಾಟೊ"
        "onion" -> "ಈರುಳ್ಳಿ"
        "potato" -> "ಆಲೂಗಡ್ಡೆ"
        "brinjal" -> "ಬದನೆಕಾಯಿ"
        "okra" -> "ಬೆಂಡೆಕಾಯಿ"
        "carrot" -> "ಕ್ಯಾರಟ್"
        "spinach" -> "ಪಾಲಕ್"
        "drumstick" -> "ನುಗ್ಗೆಕಾಯಿ"
        "cabbage" -> "ಕೋಸು"
        "red_chilli" -> "ಮೆಣಸಿನಕಾಯಿ"
        "mango" -> "ಮಾವು"
        "banana" -> "ಬಾಳೆ"
        "pomegranate" -> "ದಾಳಿಂಬೆ"
        "grapes" -> "ದ್ರಾಕ್ಷಿ"
        "papaya" -> "ಪಪ್ಪಾಯಿ"
        "coconut" -> "ತೆಂಗು"
        "arecanut" -> "ಅಡಿಕೆ"
        "sugarcane" -> "ಕಬ್ಬು"
        "coffee" -> "ಕಾಫಿ"
        else -> defaultName
    }
}

@Composable
private fun QuickInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.25f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label: $value",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun AdvisoryRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Surface(
        color = Color.Black.copy(alpha = 0.45f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.Top
        ) {
            icon()
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmptyTipsState(cropId: String?, onBack: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Agriculture, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Loading Latest KVK Advisories...",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (onBack != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Explore Other Crops", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TipPageIndicator(currentPage: Int, pageCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(4.dp)
                    .width(if (isSelected) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.3f))
                    .animateContentSize()
            )
        }
    }
}
