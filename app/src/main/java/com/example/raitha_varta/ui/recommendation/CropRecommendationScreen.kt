package com.example.raitha_varta.ui.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(
    languageCode: String = "en",
    onBack: () -> Unit,
    viewModel: CropRecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isKn = languageCode == "kn"

    var soilType by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var season by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }

    var soilExpanded by remember { mutableStateOf(false) }
    var seasonExpanded by remember { mutableStateOf(false) }
    var waterExpanded by remember { mutableStateOf(false) }

    val soilTypes = listOf("Loamy", "Clay", "Sandy", "Black Cotton", "Red Laterite", "Alluvial", "Silt")
    val seasons = listOf("Kharif (June-Oct)", "Rabi (Nov-Mar)", "Zaid (Mar-Jun)")
    val waterOptions = listOf("Irrigated", "Rain-fed", "Drip Irrigation", "Sprinkler", "Limited Water")

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(if (isKn) "ಬೆಳೆ ಶಿಫಾರಸು" else "Crop Recommendation",
                    fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0), titleContentColor = Color.White,
                    navigationIconContentColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .background(Color(0xFFF3F6FF)).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(Color(0xFF1565C0)),
                shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = Color(0xFF90CAF9), modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (isKn) "AI ಆಧಾರಿತ ಶಿಫಾರಸು" else "AI-Powered Crop Recommendation",
                            fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text(if (isKn) "ನಿಮ್ಮ ಮಣ್ಣು, ಸ್ಥಳ & ಋತುವಿಗೆ ಸೂಕ್ತ ಬೆಳೆ"
                            else "Get best crops for your soil, location & season with expected yield.",
                            color = Color.White.copy(0.8f), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }

            Text(if (isKn) "ನಿಮ್ಮ ಮಾಹಿತಿ ನೀಡಿ" else "Enter Your Farm Details",
                fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1565C0))

            // Soil Type dropdown
            ExposedDropdownMenuBox(expanded = soilExpanded, onExpandedChange = { soilExpanded = !soilExpanded }) {
                OutlinedTextField(value = soilType, onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    label = { Text(if (isKn) "ಮಣ್ಣಿನ ಪ್ರಕಾರ *" else "Soil Type *") },
                    leadingIcon = { Icon(Icons.Default.Terrain, null, tint = Color(0xFF795548)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(soilExpanded) },
                    shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = soilExpanded, onDismissRequest = { soilExpanded = false }) {
                    soilTypes.forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { soilType = s; soilExpanded = false })
                    }
                }
            }

            // Location
            OutlinedTextField(value = location, onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isKn) "ಜಿಲ್ಲೆ/ಸ್ಥಳ *" else "District / Location *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1976D2)) },
                shape = RoundedCornerShape(12.dp), singleLine = true)

            // Season dropdown
            ExposedDropdownMenuBox(expanded = seasonExpanded, onExpandedChange = { seasonExpanded = !seasonExpanded }) {
                OutlinedTextField(value = season, onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    label = { Text(if (isKn) "ಋತು *" else "Season *") },
                    leadingIcon = { Icon(Icons.Default.WbSunny, null, tint = Color(0xFFF9A825)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(seasonExpanded) },
                    shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = seasonExpanded, onDismissRequest = { seasonExpanded = false }) {
                    seasons.forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { season = s; seasonExpanded = false })
                    }
                }
            }

            // Water dropdown
            ExposedDropdownMenuBox(expanded = waterExpanded, onExpandedChange = { waterExpanded = !waterExpanded }) {
                OutlinedTextField(value = water, onValueChange = {}, readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    label = { Text(if (isKn) "ನೀರಿನ ಲಭ್ಯತೆ" else "Water Availability") },
                    leadingIcon = { Icon(Icons.Default.Water, null, tint = Color(0xFF0288D1)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(waterExpanded) },
                    shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = waterExpanded, onDismissRequest = { waterExpanded = false }) {
                    waterOptions.forEach { w ->
                        DropdownMenuItem(text = { Text(w) }, onClick = { water = w; waterExpanded = false })
                    }
                }
            }

            // Farm size
            OutlinedTextField(value = farmSize, onValueChange = { farmSize = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isKn) "ಜಮೀನಿನ ಗಾತ್ರ (ಎಕರೆ)" else "Farm Size (acres)") },
                leadingIcon = { Icon(Icons.Default.Agriculture, null, tint = Color(0xFF388E3C)) },
                shape = RoundedCornerShape(12.dp), singleLine = true)

            // Get recommendations button
            Button(
                onClick = {
                    viewModel.getRecommendations(soilType, location, season, water, farmSize, languageCode)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = soilType.isNotBlank() && location.isNotBlank() && season.isNotBlank()
                        && uiState !is RecommendationUiState.Loading,
                colors = ButtonDefaults.buttonColors(Color(0xFF1565C0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isKn) "ಶಿಫಾರಸು ಪಡೆಯಿರಿ" else "Get AI Recommendations",
                    fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }

            // State-driven results
            when (val s = uiState) {
                is RecommendationUiState.Loading -> {
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White)) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF1565C0), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(if (isKn) "AI ಶಿಫಾರಸು ತಯಾರಿಸುತ್ತಿದೆ..." else "AI is analyzing your farm...",
                                fontWeight = FontWeight.SemiBold, color = Color(0xFF1565C0))
                        }
                    }
                }
                is RecommendationUiState.Result -> RecommendationResults(s, isKn)
                is RecommendationUiState.Error -> {
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
private fun RecommendationResults(result: RecommendationUiState.Result, isKn: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Soil summary
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(Color(0xFFE3F2FD))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terrain, null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(if (isKn) "ಮಣ್ಣಿನ ಮೌಲ್ಯಮಾಪನ" else "Soil Assessment",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Text(result.soilHealth, fontSize = 13.sp, lineHeight = 18.sp, color = Color(0xFF1A1A1A))
                }
            }
        }

        Text(if (isKn) "ಶ್ರೇಷ್ಠ ಬೆಳೆಗಳು" else "Top Recommended Crops",
            fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1565C0))

        result.crops.forEachIndexed { index, crop ->
            CropRecCard(rank = index + 1, crop = crop, isKn = isKn)
        }
    }
}

@Composable
private fun CropRecCard(rank: Int, crop: CropRec, isKn: Boolean) {
    val rankColor = when (rank) { 1 -> Color(0xFFFFD700); 2 -> Color(0xFFB0BEC5); else -> Color(0xFFCD7F32) }
    val profitColor = when (crop.profitability.lowercase()) {
        "high" -> Color(0xFF2E7D32); "medium" -> Color(0xFFF57C00); else -> Color(0xFF9E9E9E)
    }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(rankColor, CircleShape), Alignment.Center) {
                    Text("#$rank", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                }
                Spacer(Modifier.width(10.dp))
                Text(crop.cropName, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f))
                Surface(color = profitColor.copy(0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(crop.profitability, Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = profitColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(10.dp))

            RecRow(Icons.Default.BarChart, Color(0xFF1565C0), if (isKn) "ನಿರೀಕ್ಷಿತ ಇಳುವರಿ" else "Expected Yield", crop.expectedYield)
            Spacer(Modifier.height(6.dp))
            RecRow(Icons.Default.Lightbulb, Color(0xFFF57C00), if (isKn) "ಏಕೆ ಸೂಕ್ತ?" else "Why suitable?", crop.reason)
            Spacer(Modifier.height(6.dp))
            RecRow(Icons.Default.Spa, Color(0xFF388E3C), if (isKn) "ಪ್ರಮುಖ ಸಲಹೆ" else "Growing Tip", crop.tips)
        }
    }
}

@Composable
private fun RecRow(icon: ImageVector, tint: Color, label: String, value: String) {
    Row {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tint, letterSpacing = 0.3.sp)
            Text(value, fontSize = 13.sp, lineHeight = 17.sp, color = Color(0xFF333333))
        }
    }
}
