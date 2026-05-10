package com.example.raitha_varta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.raitha_varta.ui.*
import com.example.raitha_varta.ui.community.FarmerCommunityScreen
import com.example.raitha_varta.ui.crop.CropSelectionScreen
import com.example.raitha_varta.ui.expert.ExpertAskScreen
import com.example.raitha_varta.ui.home.WelcomeScreen
import com.example.raitha_varta.ui.recommendation.CropRecommendationScreen
import com.example.raitha_varta.ui.prediction.AiPredictionScreen
import com.example.raitha_varta.ui.prediction.AiPredictionViewModel
import com.example.raitha_varta.ui.stories.StoriesApp
import com.example.raitha_varta.ui.theme.Raitha_VartaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkModePref by settingsViewModel.isDarkMode.collectAsState()
            val darkTheme = when (isDarkModePref) {
                true -> true
                false -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            Raitha_VartaTheme(darkTheme = darkTheme) {
                MainScreen(settingsViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()

    val languageCode by settingsViewModel.preferredLanguage.collectAsState()
    val appText = remember(languageCode) { appStrings(languageCode) }
    val onToggleLanguage = {
        settingsViewModel.updateLanguage(if (languageCode == "kn") "en" else "kn")
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showBottomBar = currentDestination?.route != "welcome" && !isImeVisible

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    val items = listOf(
                        NavigationItem("crop_selection", appText.navCrops, Icons.Default.Agriculture),
                        NavigationItem("tips", appText.navTips, Icons.Default.Lightbulb),
                        NavigationItem("market", appText.navMarket, Icons.Default.ShoppingCart),
                        NavigationItem("expert", appText.navExpert, Icons.Default.Psychology),
                        NavigationItem("community", if (languageCode == "kn") "ಸಮುದಾಯ" else "Community", Icons.Default.Groups),
                        NavigationItem("stories", if (languageCode == "kn") "ಯಶಸ್ಸಿನ ಕಥೆಗಳು" else "Success Stories", Icons.Default.AutoAwesome)
                    )

                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route?.startsWith(item.route) == true } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "welcome",
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            composable("welcome") {
                WelcomeScreen(
                    languageCode = languageCode,
                    onLanguageChange = { settingsViewModel.updateLanguage(it) },
                    onGetStarted = { navController.navigate("crop_selection") },
                    onCreateAccount = { /* TODO */ }
                )
            }

            composable("crop_selection") {
                val cropViewModel = hiltViewModel<com.example.raitha_varta.viewmodel.CropViewModel>()
                CropSelectionScreen(
                    viewModel = cropViewModel,
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage,
                    onBackToWelcome = {
                        if (!navController.popBackStack("welcome", inclusive = false)) {
                            navController.navigate("welcome") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNavigateToTips = { cropId ->
                        if (cropId != null) {
                            navController.navigate("tips/$cropId")
                        } else {
                            navController.navigate("tips")
                        }
                    },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable(
                route = "tips/{crop}",
                arguments = listOf(navArgument("crop") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                val crop = backStackEntry.arguments?.getString("crop")
                val tipViewModel = hiltViewModel<TipViewModel>()
                HomeScreen(
                    viewModel = tipViewModel,
                    cropFilter = if (crop == "null") null else crop,
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            
            composable("tips") {
                val tipViewModel = hiltViewModel<TipViewModel>()
                HomeScreen(
                    viewModel = tipViewModel,
                    cropFilter = null,
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("market") {
                val marketViewModel = hiltViewModel<MarketViewModel>()
                MarketScreen(
                    viewModel = marketViewModel,
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage
                )
            }
            composable("expert") {
                ExpertAskScreen(
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage,
                    onNavigateToWeather = { navController.navigate("weather") },
                    onNavigateToExpert = { /* Already here */ },
                    onNavigateToCrops = {
                        navController.navigate("crop_selection") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToRecommend = { navController.navigate("crop_recommend") },
                    onNavigateToPrediction = { navController.navigate("prediction") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("stories") {
                StoriesApp(
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage,
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("weather") {
                val weatherViewModel = hiltViewModel<WeatherViewModel>()
                WeatherScreen(
                    viewModel = weatherViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onToggleLanguage = onToggleLanguage,
                    onNavigateToWeather = { navController.navigate("weather") },
                    onNavigateToExpert = {
                        navController.navigate("expert") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToCrops = {
                        navController.navigate("crop_selection") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    onNavigateToDisease = { navController.navigate("expert") },
                    onNavigateToRecommend = { navController.navigate("crop_recommend") },
                    onNavigateToPrediction = { navController.navigate("prediction") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("crop_recommend") {
                CropRecommendationScreen(
                    languageCode = languageCode,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("prediction") {
                val predictionViewModel = hiltViewModel<AiPredictionViewModel>()
                AiPredictionScreen(
                    viewModel = predictionViewModel,
                    languageCode = languageCode,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("community") {
                FarmerCommunityScreen(
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

data class NavigationItem(val route: String, val label: String, val icon: ImageVector)
