package com.example.raitha_varta.ui.prediction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPredictionScreen(
    viewModel: AiPredictionViewModel,
    languageCode: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedType by remember { mutableStateOf(PredictionType.YIELD) }
    var cropName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bangalore") }
    var soilType by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (languageCode == "kn") "AI ಸುಧಾರಿತ ಮುನ್ಸೂಚನೆಗಳು" else "AI Advanced Predictions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Prediction Type Selector
            Text(
                text = if (languageCode == "kn") "ಮುನ್ಸೂಚನೆ ಪ್ರಕಾರವನ್ನು ಆಯ್ಕೆಮಾಡಿ" else "Select Prediction Type",
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = selectedType == PredictionType.YIELD,
                    onClick = { selectedType = PredictionType.YIELD },
                    label = { Text(if (languageCode == "kn") "ಇಳುವರಿ" else "Yield") }
                )
                FilterChip(
                    selected = selectedType == PredictionType.MARKET,
                    onClick = { selectedType = PredictionType.MARKET },
                    label = { Text(if (languageCode == "kn") "ಮಾರುಕಟ್ಟೆ" else "Market") }
                )
                FilterChip(
                    selected = selectedType == PredictionType.DISEASE,
                    onClick = { selectedType = PredictionType.DISEASE },
                    label = { Text(if (languageCode == "kn") "ರೋಗ" else "Disease") }
                )
            }

            // Inputs
            OutlinedTextField(
                value = cropName,
                onValueChange = { cropName = it },
                label = { Text(if (languageCode == "kn") "ಬೆಳೆಯ ಹೆಸರು (ಉದಾ. ಟೊಮ್ಯಾಟೊ)" else "Crop Name (e.g. Tomato)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(if (languageCode == "kn") "ನಗರ / ಸ್ಥಳ" else "City / Location") },
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedType == PredictionType.YIELD) {
                OutlinedTextField(
                    value = soilType,
                    onValueChange = { soilType = it },
                    label = { Text(if (languageCode == "kn") "ಮಣ್ಣಿನ ಪ್ರಕಾರ (ಉದಾ. ಕೆಂಪು ಮಣ್ಣು)" else "Soil Type (e.g. Red Soil)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    viewModel.makePrediction(selectedType, cropName, city, soilType, languageCode)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cropName.isNotBlank() && city.isNotBlank() && uiState !is PredictionUiState.Loading
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (languageCode == "kn") "ಮುನ್ಸೂಚನೆ ರಚಿಸಿ" else "Generate Prediction")
            }

            // Result Area
            when (val state = uiState) {
                is PredictionUiState.Idle -> {}
                is PredictionUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PredictionUiState.Error -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                is PredictionUiState.Result -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (languageCode == "kn") "AI ಮುನ್ಸೂಚನೆ ಫಲಿತಾಂಶ" else "AI Prediction Result",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            MarkdownText(
                                markdown = state.prediction,
                                style = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
