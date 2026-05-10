package com.example.raitha_varta.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onToggleLanguage: () -> Unit,
    onNavigateToWeather: (() -> Unit)? = null,
    onNavigateToExpert: (() -> Unit)? = null,
    onNavigateToCrops: (() -> Unit)? = null,
    onNavigateToDisease: (() -> Unit)? = null,
    onNavigateToRecommend: (() -> Unit)? = null,
    onNavigateToPrediction: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val preferredCity by viewModel.preferredCity.collectAsState()
    val preferredCrop by viewModel.preferredCrop.collectAsState()
    val preferredLanguage by viewModel.preferredLanguage.collectAsState()
    val alertsEnabled by viewModel.alertsEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val appText = remember(preferredLanguage) { appStrings(preferredLanguage) }
    val isKn = preferredLanguage == "kn"
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var cityText by remember(preferredCity) { mutableStateOf(preferredCity) }
    var showDistrictSuggestions by remember { mutableStateOf(false) }
    var expandedCrop by remember { mutableStateOf(false) }

    val karnatakaDistricts = listOf(
        "Bagalkot", "Ballari", "Belagavi", "Bengaluru Rural", "Bengaluru Urban",
        "Bidar", "Chamarajanagar", "Chikkaballapur", "Chikkamagaluru", "Chitradurga",
        "Dakshina Kannada", "Davanagere", "Dharwad", "Gadag", "Hassan",
        "Haveri", "Kalaburagi", "Kodagu", "Kolar", "Koppal",
        "Mandya", "Mysuru", "Raichur", "Ramanagara", "Shivamogga",
        "Tumakuru", "Udupi", "Uttara Kannada", "Vijayapura", "Yadgir"
    )
    val districtSuggestions = remember(cityText) {
        if (cityText.length >= 2)
            karnatakaDistricts.filter { it.contains(cityText, ignoreCase = true) }
        else emptyList()
    }

    val crops = listOf(
        "All", "Paddy", "Wheat", "Ragi", "Maize", "Bajra", "Jowar",
        "Tomato", "Onion", "Potato", "Brinjal", "Okra", "Carrot",
        "Spinach", "Drumstick", "Cabbage", "Red Chilli",
        "Mango", "Banana", "Pomegranate", "Grapes", "Papaya",
        "Coconut", "Areca nut", "Sugarcane", "Coffee"
    )

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                (context.getSystemService(NotificationManager::class.java)?.areNotificationsEnabled() == true)
            }
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> cameraGranted = granted }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> locationGranted = granted }

    val notificationsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> notificationPermissionGranted = granted }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(appText.settingsTitle, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {


            // ── Section 1: Location ───────────────────────────────
            item {
                Column {
                    Text(
                        text = appText.settingsLocation,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cityText,
                        onValueChange = {
                            cityText = it
                            showDistrictSuggestions = it.length >= 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null) },
                        trailingIcon = {
                            if (cityText.isNotEmpty()) {
                                IconButton(onClick = {
                                    cityText = ""
                                    showDistrictSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        label = { Text(appText.settingsDistrictHint) },
                        singleLine = true
                    )
                    AnimatedVisibility(visible = showDistrictSuggestions && districtSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column {
                                districtSuggestions.take(5).forEach { district ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                cityText = district
                                                showDistrictSuggestions = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(district, fontSize = 14.sp)
                                    }
                                    if (district != districtSuggestions.take(5).last()) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 2: Crop Preference ────────────────────────
            item {
                Column {
                    Text(
                        text = appText.settingsPrimaryCrop,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedCrop,
                        onExpandedChange = { expandedCrop = !expandedCrop }
                    ) {
                        OutlinedTextField(
                            value = preferredCrop,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Agriculture, contentDescription = null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCrop) }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCrop,
                            onDismissRequest = { expandedCrop = false }
                        ) {
                            crops.forEach { crop ->
                                DropdownMenuItem(
                                    text = { Text(crop, fontSize = 18.sp) },
                                    onClick = {
                                        viewModel.updateCrop(crop)
                                        expandedCrop = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Section 3: Language ───────────────────────────────
            item {
                Column {
                    Text(
                        text = appText.settingsLanguage,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = preferredLanguage == "en",
                            onClick = { viewModel.updateLanguage("en") },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = preferredLanguage == "kn",
                            onClick = { viewModel.updateLanguage("kn") },
                            label = { Text("ಕನ್ನಡ") }
                        )
                    }
                }
            }

            // ── Section Theme ───────────────────────────────────
            item {
                val isDarkModePref by viewModel.isDarkMode.collectAsState()
                Column {
                    Text(
                        text = if (isKn) "ಥೀಮ್ (Theme)" else "Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = isDarkModePref == false,
                            onClick = { viewModel.updateDarkMode(false) },
                            label = { Text(if (isKn) "ಲೈಟ್" else "Light") },
                            leadingIcon = { Icon(Icons.Default.WbSunny, null, Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = isDarkModePref == true,
                            onClick = { viewModel.updateDarkMode(true) },
                            label = { Text(if (isKn) "ಡಾರ್ಕ್" else "Dark") },
                            leadingIcon = { Icon(Icons.Default.NightsStay, null, Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = isDarkModePref == null,
                            onClick = { viewModel.updateDarkMode(null) },
                            label = { Text(if (isKn) "ಸಿಸ್ಟಮ್" else "System") },
                            leadingIcon = { Icon(Icons.Default.SettingsSuggest, null, Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            // ── Section 4: Permissions & Alerts ───────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isKn) "ಅನುಮತಿಗಳು ಮತ್ತು ಅಲರ್ಟ್‌ಗಳು" else "Permissions & Alerts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    PermissionSettingRow(
                        icon = Icons.Default.CameraAlt,
                        title = if (isKn) "ಕ್ಯಾಮೆರಾ ಅನುಮತಿ" else "Camera permission",
                        subtitle = if (isKn) "ರೋಗ ಪತ್ತೆಗೆ ಫೋಟೋ ತೆಗೆದುಕೊಳ್ಳಲು ಅಗತ್ಯ" else "Required for crop photo and disease scan",
                        granted = cameraGranted,
                        grantedLabel = if (isKn) "ಅನುಮತಿಸಲಾಗಿದೆ" else "Granted",
                        deniedLabel = if (isKn) "ಅನುಮತಿ ಇಲ್ಲ" else "Not granted",
                        actionLabel = if (isKn) "ಅನುಮತಿ ನೀಡಿ" else "Allow"
                    ) {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }

                    PermissionSettingRow(
                        icon = Icons.Default.LocationOn,
                        title = if (isKn) "ಸ್ಥಳ ಅನುಮತಿ" else "Location permission",
                        subtitle = if (isKn) "ನಿಮ್ಮ ಜಿಲ್ಲೆಗೆ ಹವಾಮಾನ ಮತ್ತು ಮಾರುಕಟ್ಟೆ ತೋರಿಸಲು" else "For district weather and localized market updates",
                        granted = locationGranted,
                        grantedLabel = if (isKn) "ಅನುಮತಿಸಲಾಗಿದೆ" else "Granted",
                        deniedLabel = if (isKn) "ಅನುಮತಿ ಇಲ್ಲ" else "Not granted",
                        actionLabel = if (isKn) "ಅನುಮತಿ ನೀಡಿ" else "Allow"
                    ) {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    PermissionSettingRow(
                        icon = Icons.Default.Notifications,
                        title = if (isKn) "ನೋಟಿಫಿಕೇಶನ್ ಅನುಮತಿ" else "Notification permission",
                        subtitle = if (isKn) "ಹವಾಮಾನ/ಬೆಲೆ ಅಲರ್ಟ್‌ಗಳಿಗೆ ಅಗತ್ಯ" else "Needed for weather and market alerts",
                        granted = notificationPermissionGranted,
                        grantedLabel = if (isKn) "ಅನುಮತಿಸಲಾಗಿದೆ" else "Granted",
                        deniedLabel = if (isKn) "ಅನುಮತಿ ಇಲ್ಲ" else "Not granted",
                        actionLabel = if (isKn) "ಸಕ್ರಿಯಗೊಳಿಸಿ" else "Enable"
                    ) {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    }

                    ToggleSettingRow(
                        icon = Icons.Default.Security,
                        title = if (isKn) "ಕೃಷಿ ಎಚ್ಚರಿಕೆಗಳು" else "Farming alerts",
                        subtitle = if (isKn) "ರೋಗ, ಹವಾಮಾನ, ಬೆಲೆ ಬಗ್ಗೆ ಅಲರ್ಟ್‌ಗಳು" else "Disease, weather and price alerts",
                        checked = alertsEnabled,
                        onCheckedChange = viewModel::updateAlertsEnabled
                    )

                    ToggleSettingRow(
                        icon = Icons.Default.ToggleOn,
                        title = if (isKn) "ಆಪ್ ನೋಟಿಫಿಕೇಶನ್‌ಗಳು" else "App notifications",
                        subtitle = if (isKn) "ಮುಖ್ಯ ಅಪ್‌ಡೇಟ್‌ಗಳ ನೋಟಿಫಿಕೇಶನ್" else "Receive important update notifications",
                        checked = notificationsEnabled,
                        onCheckedChange = viewModel::updateNotificationsEnabled
                    )
                }
            }

            // ── Save Button ───────────────────────────────────────
            item {
                Button(
                    onClick = {
                        viewModel.updateCity(cityText)
                        showDistrictSuggestions = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (isKn) "✓ ಜಿಲ್ಲೆ ಉಳಿಸಲಾಗಿದೆ: $cityText" else "✓ District saved: $cityText",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = cityText.isNotBlank(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(appText.settingsSave, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // ── App Info ──────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isKn) "ರೈತ ವಾರ್ತಾ" else "Raitha Varta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isKn) "ಆವೃತ್ತಿ 1.0 • ರೈತರಿಗಾಗಿ ಸಮರ್ಪಿತ" else "Version 1.0 • Built for Karnataka farmers",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PermissionSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    grantedLabel: String,
    deniedLabel: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = if (granted) grantedLabel else deniedLabel,
                    fontSize = 12.sp,
                    color = if (granted) Color(0xFF2E7D32) else Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
            if (!granted) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun QuickAccessCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1A1A1A)
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = Color(0xFF1A1A1A).copy(alpha = 0.6f)
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.NavigateNext,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.7f)
            )
        }
    }
}
