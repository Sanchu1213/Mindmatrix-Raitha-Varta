package com.example.raitha_varta.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.raitha_varta.data.MarketPrice
import com.example.raitha_varta.data.PriceTrend
import com.example.raitha_varta.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    languageCode: String,
    onToggleLanguage: () -> Unit
) {
    val prices by viewModel.marketPrices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    val appText = remember(languageCode) { appStrings(languageCode) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showScrollUp by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showScrollDown by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible < layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(errorMessage) {
        val msg = errorMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = msg)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!isSearchActive) {
                TopAppBar(
                    windowInsets = WindowInsets(0),
                    title = { Text(appText.marketTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        IconButton(onClick = onToggleLanguage) {
                            Icon(Icons.Default.Language, contentDescription = "Language")
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { viewModel.refreshPrices() }) {
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
                            placeholder = { Text(appText.marketSearchHint) },
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
                                        viewModel.updateSearchQuery("")
                                    } else {
                                        isSearchActive = false
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                                }
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = onToggleLanguage) {
                            Icon(Icons.Default.Language, contentDescription = "Language")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    ) { innerPadding ->
        if (prices.isEmpty() && isRefreshing) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (prices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) 
                            "${appText.marketNoResults} \"$searchQuery\"" 
                        else 
                            if (languageCode == "kn") "ಮಾರುಕಟ್ಟೆ ಮಾಹಿತಿ ಲಭ್ಯವಿಲ್ಲ" else "Market data unavailable",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refreshPrices() }) {
                        Text(if (languageCode == "kn") "ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ" else "Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(prices) { price ->
                    MarketPriceItem(
                        marketPrice = price,
                        updatedLabel = appText.marketUpdated,
                        languageCode = languageCode
                    )
                }
            }
        }
    }

    // Floating scroll buttons — white elevated pill style (right side, mid-screen)
    Column(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showScrollUp) {
            Surface(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                modifier = Modifier
                    .size(40.dp)
                    .shadow(6.dp, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)),
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        if (showScrollUp && showScrollDown) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
        }
        if (showScrollDown) {
            Surface(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .shadow(6.dp, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
                shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
    } // end Box
}

@Composable
fun MarketPriceItem(
    marketPrice: MarketPrice,
    updatedLabel: String,
    languageCode: String
) {
    val isKn = languageCode == "kn"
    val cropLabel = localizedMarketCropName(marketPrice.cropName, isKn)
    val unitLabel = when (marketPrice.unit.lowercase(Locale.ROOT)) {
        "per kg" -> if (isKn) "ಕೆ.ಜಿ ಗೆ" else "per Kg"
        "per quintal" -> if (isKn) "ಕ್ವಿಂಟಲ್ ಗೆ" else "per Quintal"
        else -> marketPrice.unit
    }
    val locale = if (isKn) Locale("kn", "IN") else Locale.ENGLISH

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cropLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = marketPrice.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${DateUtils.formatDayAndDate(marketPrice.updatedAt, locale)} • $updatedLabel: ${DateUtils.getTimeAgo(marketPrice.updatedAt)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${marketPrice.price}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TrendIcon(marketPrice.trend)
                }
                Text(
                    text = unitLabel,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun localizedMarketCropName(name: String, isKn: Boolean): String {
    if (!isKn) return name
    return when (name.lowercase(Locale.ROOT)) {
        "paddy (common)" -> "ಭತ್ತ (ಸಾಮಾನ್ಯ)"
        "paddy" -> "ಭತ್ತ"
        "wheat" -> "ಗೋಧಿ"
        "ragi (millet)" -> "ರಾಗಿ (ಮಿಲ್ಲೆಟ್)"
        "ragi" -> "ರಾಗಿ"
        "maize (corn)" -> "ಮೆಕ್ಕೆಜೋಳ"
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
        "chilli", "red chilli", "green chilli" -> "ಮೆಣಸಿನಕಾಯಿ"
        "mango" -> "ಮಾವು"
        "banana" -> "ಬಾಳೆಹಣ್ಣು"
        "pomegranate" -> "ದಾಳಿಂಬೆ"
        "grapes" -> "ದ್ರಾಕ್ಷಿ"
        "papaya" -> "ಪಪ್ಪಾಯಿ"
        "coconut" -> "ತೆಂಗು"
        "sugarcane" -> "ಕಬ್ಬು"
        "coffee" -> "ಕಾಫಿ"
        else -> name
    }
}

@Composable
fun TrendIcon(trend: PriceTrend) {
    when (trend) {
        PriceTrend.UP -> Icon(
            imageVector = Icons.Default.ArrowUpward,
            contentDescription = "Up",
            tint = Color.Red,
            modifier = Modifier.size(16.dp)
        )
        PriceTrend.DOWN -> Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Down",
            tint = Color.Green,
            modifier = Modifier.size(16.dp)
        )
        PriceTrend.STABLE -> Icon(
            imageVector = Icons.Default.HorizontalRule,
            contentDescription = "Stable",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}
