package com.example.raitha_varta.ui.disease

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDiseaseScreen(
    languageCode: String = "en",
    onBack: () -> Unit,
    viewModel: CropDiseaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val capturedImage by viewModel.capturedImage.collectAsState()
    val context = LocalContext.current
    val isKn = languageCode == "kn"

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
        bmp?.let { viewModel.setImage(it) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bmp = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val src = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(src)
            }
            viewModel.setImage(bmp)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(if (isKn) "ಬೆಳೆ ರೋಗ ಪರೀಕ್ಷೆ" else "AI Crop Disease Detection",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20), titleContentColor = Color.White,
                    navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .background(Color(0xFFF1F8E9)).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Info card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFF2E7D32)),
                shape = RoundedCornerShape(14.dp)) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Biotech, null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (isKn) "AI ರೋಗ ಪತ್ತೆ" else "AI-Powered Detection",
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text(if (isKn) "ಫೋಟೋ ತೆಗೆಯಿರಿ — AI ರೋಗ, ತೀವ್ರತೆ & ಚಿಕಿತ್ಸೆ ಹೇಳುತ್ತದೆ"
                            else "Snap or upload a crop photo — AI identifies disease, severity & treatment.",
                            color = Color.White.copy(0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            // Image preview or placeholder
            if (capturedImage == null) {
                Card(modifier = Modifier.fillMaxWidth().height(220.dp),
                    border = BorderStroke(2.dp, Color(0xFF81C784).copy(0.6f)),
                    colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(if (isKn) "ಬೆಳೆ ಫೋಟೋ ತೆಗೆಯಿರಿ" else "Take a Crop Photo",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                        Text(if (isKn) "ಅಥವಾ ಗ್ಯಾಲರಿಯಿಂದ ಆಯ್ಕೆ ಮಾಡಿ" else "or choose from gallery",
                            fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))) {
                    Image(bitmap = capturedImage!!.asImageBitmap(), null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    IconButton(onClick = { viewModel.reset() },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            .size(32.dp).background(Color.Black.copy(0.5f), CircleShape)) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Buttons row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { cameraLauncher.launch() }, modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFF2E7D32)), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isKn) "ಕ್ಯಾಮೆರಾ" else "Camera", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF2E7D32))) {
                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp), tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isKn) "ಗ್ಯಾಲರಿ" else "Gallery", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }

            if (capturedImage != null && uiState !is DiseaseUiState.Analyzing) {
                Button(onClick = { viewModel.analyzeDisease(languageCode) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFFF57C00)), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isKn) "AI ವಿಶ್ಲೇಷಣೆ ಮಾಡಿ" else "Analyze with AI",
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            when (val s = uiState) {
                is DiseaseUiState.Analyzing -> {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White)) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2E7D32), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(if (isKn) "AI ವಿಶ್ಲೇಷಣೆ..." else "AI analyzing your crop...",
                                fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                        }
                    }
                }
                is DiseaseUiState.Result -> DiseaseResultCard(s, isKn)
                is DiseaseUiState.Error -> {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(14.dp)) {
                            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(s.message, color = Color.Red, fontSize = 13.sp)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun DiseaseResultCard(result: DiseaseUiState.Result, isKn: Boolean) {
    val isHealthy = result.diseaseName.contains("Healthy", ignoreCase = true)
    val headerColor = if (isHealthy) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val severityColor = when (result.severity.lowercase()) {
        "high", "critical" -> Color(0xFFD32F2F)
        "moderate", "medium" -> Color(0xFFF57C00)
        "low" -> Color(0xFFFBC02D)
        else -> Color(0xFF388E3C)
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(headerColor)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                    null, tint = Color.White, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(result.diseaseName, fontWeight = FontWeight.Black, color = Color.White, fontSize = 17.sp)
                    if (!isHealthy) {
                        Spacer(Modifier.height(4.dp))
                        Surface(color = severityColor, shape = RoundedCornerShape(6.dp)) {
                            Column {
                                Text("Severity: ${result.severity}",
                                    color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Confidence: ${(result.confidence * 100).toInt()}%",
                                    color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        DisResultRow(Icons.Default.Info, Color(0xFF1976D2), if (isKn) "ವಿವರ" else "Description", result.description)
        if (!isHealthy) {
            DisResultRow(Icons.Default.Visibility, Color(0xFFF57C00), if (isKn) "ಲಕ್ಷಣ" else "Symptoms", result.symptoms)
            DisResultRow(Icons.Default.LocalHospital, Color(0xFFD32F2F), if (isKn) "ಚಿಕಿತ್ಸೆ" else "Treatment", result.treatment)
            DisResultRow(Icons.Default.Shield, Color(0xFF388E3C), if (isKn) "ತಡೆಗಟ್ಟುವಿಕೆ" else "Prevention", result.prevention)
            if (result.organicRemedy.isNotBlank() && result.organicRemedy != "N/A") {
                DisResultRow(Icons.Default.Spa, Color(0xFF689F38), if (isKn) "ಸಾವಯವ" else "Organic Remedy", result.organicRemedy)
            }
        }
    }
}

@Composable
private fun DisResultRow(icon: ImageVector, tint: Color, title: String, content: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(12.dp)) {
            Box(Modifier.size(34.dp).background(tint.copy(0.12f), CircleShape), Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = tint, fontSize = 11.sp, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(3.dp))
                Text(content, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF1A1A1A))
            }
        }
    }
}
