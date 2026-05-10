package com.example.raitha_varta.ui.crop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_varta.data.Crop
import com.example.raitha_varta.ui.appStrings
import com.example.raitha_varta.ui.components.PrimaryButton
import com.example.raitha_varta.viewmodel.CropViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSelectionScreen(
    viewModel: CropViewModel,
    languageCode: String,
    onToggleLanguage: () -> Unit,
    onBackToWelcome: () -> Unit,
    onNavigateToTips: (String?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val crops by viewModel.crops.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val appText = remember(languageCode) { appStrings(languageCode) }

    // Group crops by category for better visual organization
    val groupedCrops = remember(crops) {
        crops.groupBy { it.category }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackToWelcome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = appText.cropsDashboardTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
                    }
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9FBF9))
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar for quick crop finding
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(appText.cropsSearchHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. CATEGORIZED CROP GRID
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groupedCrops.forEach { (category, categoryCrops) ->
                    // Category Header Section
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = category.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(categoryCrops) { crop ->
                        CropSymbol(
                            crop = crop,
                            languageCode = languageCode,
                            onClick = { onNavigateToTips(crop.id) } // Using ID for exact mapping
                        )
                    }
                }
            }

            // Global button to see all general farming tips
            PrimaryButton(
                text = appText.cropsDailyTips,
                onClick = { onNavigateToTips(null) },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun CropSymbol(crop: Crop, languageCode: String, onClick: () -> Unit) {
    val cropName = remember(crop.id, crop.name, languageCode) {
        localizedCropName(crop.id, crop.name, languageCode)
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Crop Image - Using local resources as requested
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(2.dp, Color(0xFFE8F5E9)),
            shadowElevation = 4.dp
        ) {
            Image(
                painter = painterResource(id = crop.imageRes),
                contentDescription = cropName,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = cropName,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        // Recommended/Season Badge
        Surface(
            color = if (crop.isRecommended) Color(0xFFFFEB3B) else Color(0xFFE8F5E9),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = crop.season.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (crop.isRecommended) Color.Black else Color(0xFF2E7D32),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

private fun localizedCropName(cropId: String, defaultName: String, languageCode: String): String {
    if (languageCode != "kn") return defaultName
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

