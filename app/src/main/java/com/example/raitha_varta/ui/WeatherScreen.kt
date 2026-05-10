package com.example.raitha_varta.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_varta.data.remote.WeatherResponse
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isSearchActive) {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { Text("Weather Updates") },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search City")
                        }
                        IconButton(onClick = { viewModel.fetchWeather() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            } else {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Enter city name...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (searchQuery.isNotEmpty()) {
                                        viewModel.fetchWeather(searchQuery)
                                        isSearchActive = false
                                    } else {
                                        isSearchActive = false
                                    }
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Fetch Weather")
                                }
                            },
                            leadingIcon = {
                                IconButton(onClick = { isSearchActive = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            }
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is WeatherUiState.Success -> {
                    WeatherContent(state.weather)
                }
                is WeatherUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.fetchWeather() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherContent(weather: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weather.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Icon(
            imageVector = if (weather.weather.firstOrNull()?.description?.contains("cloud") == true) 
                Icons.Default.Cloud else Icons.Default.WbSunny,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "${weather.main.temp.roundToInt()}°C",
            fontSize = 64.sp,
            fontWeight = FontWeight.ExtraBold
        )
        
        Text(
            text = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeatherDetailItem(
                icon = Icons.Default.WaterDrop,
                label = "Humidity",
                value = "${weather.main.humidity}%"
            )
            WeatherDetailItem(
                icon = Icons.Default.Cloud,
                label = "Pressure",
                value = "${weather.main.pressure} hPa"
            )
        }
    }
}

@Composable
fun WeatherDetailItem(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, fontSize = 12.sp)
            Text(text = value, fontWeight = FontWeight.Bold)
        }
    }
}
