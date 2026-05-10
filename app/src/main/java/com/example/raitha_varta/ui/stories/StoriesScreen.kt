package com.example.raitha_varta.ui.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.raitha_varta.R
import com.example.raitha_varta.ui.appStrings
import kotlinx.coroutines.launch

data class SuccessStory(
    val id: String,
    val farmerName: String,
    val location: String,
    val crop: String,
    val title: String,
    val summary: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val yieldBadge: String,
    val yieldBadgeKn: String = "",
    val quoteEn: String,
    val quoteKn: String,
    val achievement: String,
    val achievementKn: String = "",
    val problems: String,
    val problemsKn: String = "",
    val solutions: String,
    val solutionsKn: String = "",
    val steps: String = "",
    val stepsKn: String = "",
)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StoriesApp(
    languageCode: String = "en",
    onToggleLanguage: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val navController = rememberNavController()
    val stories = remember { getMockStories() }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            StoriesListScreen(
                stories = stories,
                languageCode = languageCode,
                onToggleLanguage = onToggleLanguage,
                onNavigateToSettings = onNavigateToSettings
            ) { storyId -> navController.navigate("detail/$storyId") }
        }
        composable(
            "detail/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storyId = backStackEntry.arguments?.getString("storyId")
            val story = stories.find { it.id == storyId }
            if (story != null) {
                StoryDetailScreen(
                    story = story,
                    languageCode = languageCode,
                    onToggleLanguage = onToggleLanguage
                ) { navController.popBackStack() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun StoriesListScreen(
    stories: List<SuccessStory>,
    languageCode: String,
    onToggleLanguage: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onStoryClick: (String) -> Unit
) {
    val appText = remember(languageCode) { appStrings(languageCode) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollUp by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showScrollDown by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible < layoutInfo.totalItemsCount - 1
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text(appText.storiesTitle, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF2E7D32))
                    }
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF7F9F7)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(appText.storiesSubtitle, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
            }
            items(stories) { story ->
                StoryCard(
                    story = story,
                    languageCode = languageCode,
                    badgeText = appText.storiesNewBadge,
                    onClick = { onStoryClick(story.id) }
                )
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
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top",
                        tint = Color(0xFF2E7D32),
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
                    .background(Color(0xFFDDDDDD))
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
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
    } // end Box
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun StoryCard(story: SuccessStory, languageCode: String, badgeText: String, onClick: () -> Unit) {
    val isKn = languageCode == "kn"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                if (story.imageRes != null) {
                    Image(
                        painter = painterResource(id = story.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!story.imageUrl.isNullOrBlank()) {
                    GlideImage(
                        model = story.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isKn && story.yieldBadgeKn.isNotBlank()) story.yieldBadgeKn else story.yieldBadge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = story.farmerName,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF10233F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${story.location.uppercase()} • ${localizedCropLabel(story.crop, languageCode).uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFF6E6E6E)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "\"${if (isKn) story.quoteKn else story.quoteEn}\"",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A2D4A)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(color = Color(0xFFFFEB3B), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun StoryDetailScreen(
    story: SuccessStory,
    languageCode: String,
    onToggleLanguage: () -> Unit,
    onBack: () -> Unit
) {
    val appText = remember(languageCode) { appStrings(languageCode) }
    val isKn = languageCode == "kn"
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appText.storiesDetailsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onToggleLanguage) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                if (story.imageRes != null) {
                    Image(
                        painter = painterResource(id = story.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!story.imageUrl.isNullOrBlank()) {
                    GlideImage(
                        model = story.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                Text(
                    text = if (isKn) localizedTitleKn(story.title) else story.title,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = story.farmerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = story.location, color = Color.Gray, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                DetailSection(
                    appText.storiesWhatDone,
                    if (isKn && story.achievementKn.isNotBlank()) story.achievementKn else story.achievement,
                    Icons.Default.Star
                )
                DetailSection(
                    appText.storiesProblems,
                    if (isKn && story.problemsKn.isNotBlank()) story.problemsKn else story.problems,
                    Icons.Default.LocationOn
                )
                DetailSection(
                    appText.storiesHowOvercame,
                    if (isKn && story.solutionsKn.isNotBlank()) story.solutionsKn else story.solutions,
                    Icons.Default.Star
                )
                DetailSection(
                    appText.storiesSteps,
                    if (isKn && story.stepsKn.isNotBlank()) story.stepsKn else story.steps,
                    Icons.Default.Star
                )
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = content, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
    }
}

fun localizedCropLabel(crop: String, languageCode: String): String {
    if (languageCode != "kn") return crop
    return when (crop.lowercase()) {
        "paddy" -> "ಭತ್ತ"
        "sugarcane" -> "ಕಬ್ಬು"
        "pepper" -> "ಮೆಣಸು"
        "red chilli" -> "ಕೆಂಪು ಮೆಣಸಿನಕಾಯಿ"
        "jowar" -> "ಜೋಳ"
        else -> crop
    }
}

fun localizedTitleKn(title: String): String {
    return when (title) {
        "Water-Smart Paddy Management" -> "ನೀರಿನ ಉಳಿತಾಯ ಭತ್ತ ನಿರ್ವಹಣೆ"
        "Smart Sugarcane Water-Nutrient Plan" -> "ಕಬ್ಬಿಗೆ ನೀರು-ಪೋಷಕ ಸ್ಮಾರ್ಟ್ ಯೋಜನೆ"
        "Sirsi Pepper Block Revival" -> "ಸಿರ್ಸಿ ಮೆಣಸು ತೋಟ ಪುನರುಜ್ಜೀವನ"
        "Dharwad Chilli Quality Boost" -> "ಧಾರವಾಡ ಮೆಣಸಿನ ಗುಣಮಟ್ಟ ಹೆಚ್ಚಳ"
        "Belagavi Jowar Resilience Model" -> "ಬೆಳಗಾವಿ ಜೋಳ ಸಹನಶೀಲತಾ ಮಾದರಿ"
        "Mandya Paddy Quality Story" -> "ಮಂಡ್ಯ ಭತ್ತ ಗುಣಮಟ್ಟ ಯಶೋಗಾಥೆ"
        else -> title
    }
}

fun getMockStories(): List<SuccessStory> {
    return listOf(
        SuccessStory(
            id = "1",
            farmerName = "Basavaraj M.",
            location = "Mandya, Karnataka",
            crop = "Paddy",
            title = "Water-Smart Paddy Management",
            summary = "Daily advisories helped reduce water use while improving grain quality.",
            imageRes = R.drawable.rice,
            yieldBadge = "40% Yield Increase",
            yieldBadgeKn = "40% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "Using the daily tips for water management helped me save 30% water and increased my paddy quality significantly.",
            quoteKn = "ನೀರಿನ ನಿರ್ವಹಣೆಯ ಕುರಿತ ದಿನನಿತ್ಯದ ಸಲಹೆಗಳು ನನಗೆ 30% ನೀರು ಉಳಿಸಲು ಮತ್ತು ಭತ್ತದ ಗುಣಮಟ್ಟವನ್ನು ಹೆಚ್ಚಿಸಲು ಸಹಾಯ ಮಾಡಿವೆ.",
            achievement = "Adopted alternate wetting-drying and stage-wise nutrient scheduling across 4.5 acres.",
            achievementKn = "4.5 ಎಕರೆಯಲ್ಲಿ ಪರ್ಯಾಯ ನೆನೆಸಿ-ಒಣಗಿಸುವ ನೀರಾವರಿ ಮತ್ತು ಹಂತವಾರು ಪೋಷಕ ನಿರ್ವಹಣೆಯನ್ನು ಅನುಸರಿಸಲಾಯಿತು.",
            problems = "Frequent over-irrigation and patchy tiller growth were reducing grain fill and input efficiency.",
            problemsKn = "ಅತಿಯಾದ ನೀರಾವರಿ ಮತ್ತು ಅಸಮ ಟಿಲ್ಲರ್ ಬೆಳವಣಿಗೆ ಕಾಳು ತುಂಬುವಿಕೆ ಹಾಗೂ ಇನ್‌ಪುಟ್ ಕಾರ್ಯಕ್ಷಮತೆಯನ್ನು ಕಡಿಮೆ ಮಾಡುತ್ತಿತ್ತು.",
            solutions = "Moved from fixed irrigation calendar to moisture-based irrigation and weekly field scouting.",
            solutionsKn = "ನಿಶ್ಚಿತ ದಿನಗಳ ನೀರಾವರಿಯಿಂದ ತೇವಾಂಶ ಆಧಾರಿತ ನೀರಾವರಿ ಮತ್ತು ವಾರದ ಹೊಲ ನಿಗಾಕ್ಕೆ ಬದಲಾಯಿಸಲಾಯಿತು.",
            steps = "1. Set field channels for controlled irrigation.\n2. Started weekly pest and tiller count log.\n3. Applied split fertilizer during active tillering and panicle initiation.",
            stepsKn = "1. ನಿಯಂತ್ರಿತ ನೀರಾವರಿಗಾಗಿ ಹೊಲ ಕಾಲುವೆಗಳನ್ನು ಸಜ್ಜುಗೊಳಿಸಲಾಯಿತು.\n2. ವಾರದ ಕೀಟ ಹಾಗೂ ಟಿಲ್ಲರ್ ಎಣಿಕೆ ದಾಖಲೆ ಆರಂಭಿಸಲಾಯಿತು.\n3. ಸಕ್ರಿಯ ಟಿಲ್ಲರಿಂಗ್ ಮತ್ತು ಪ್ಯಾನಿಕಲ್ ಹಂತದಲ್ಲಿ ವಿಭಜಿತ ಗೊಬ್ಬರ ನೀಡಲಾಯಿತು."
        ),
        SuccessStory(
            id = "2",
            farmerName = "Ravi K.",
            location = "Belagavi, Karnataka",
            crop = "Sugarcane",
            title = "Smart Sugarcane Water-Nutrient Plan",
            summary = "Belagavi sugarcane field improved cane thickness and recovery.",
            imageRes = R.drawable.sugarcane,
            yieldBadge = "31% Yield Increase",
            yieldBadgeKn = "31% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "Drip fertigation and weekly field observations helped me reduce cost and improve cane recovery.",
            quoteKn = "ಡ್ರಿಪ್ ಫರ್ಟಿಗೇಶನ್ ಮತ್ತು ವಾರದ ಹೊಲ ನಿಗಾದಿಂದ ವೆಚ್ಚ ಕಡಿಮೆಯಾಗಿ ಕಬ್ಬಿನ ರಿಕವರಿ ಉತ್ತಮವಾಗಿದೆ.",
            achievement = "Improved cane uniformity and sugar recovery through stage-wise water and nutrient scheduling.",
            achievementKn = "ಹಂತವಾರು ನೀರು ಮತ್ತು ಪೋಷಕ ಯೋಜನೆಯಿಂದ ಕಬ್ಬಿನ ಸಮತೋಲನ ಬೆಳವಣಿಗೆ ಮತ್ತು ಸಕ್ಕರೆ ರಿಕವರಿ ಹೆಚ್ಚಿತು.",
            problems = "Flood irrigation and bulk fertilizer application were causing uneven cane growth.",
            problemsKn = "ಮಳೆಗಾಲದ ನೀರಾವರಿ ಮತ್ತು ಒಮ್ಮೆಗೆ ಗೊಬ್ಬರ ನೀಡಿಕೆ ಕಬ್ಬಿನ ಅಸಮ ಬೆಳವಣಿಗೆಗೆ ಕಾರಣವಾಗುತ್ತಿತ್ತು.",
            solutions = "Adopted split N-K fertigation and need-based irrigation intervals.",
            solutionsKn = "ವಿಭಜಿತ N-K ಫರ್ಟಿಗೇಶನ್ ಮತ್ತು ಅಗತ್ಯಾಧಾರಿತ ನೀರಾವರಿ ಅಂತರಗಳನ್ನು ಅನುಸರಿಸಲಾಯಿತು.",
            steps = "1. Installed venturi fertigation setup.\n2. Shifted to split nutrient doses.\n3. Logged cane girth every two weeks.",
            stepsKn = "1. ವೆಂಚುರಿ ಫರ್ಟಿಗೇಶನ್ ವ್ಯವಸ್ಥೆ ಅಳವಡಿಸಲಾಯಿತು.\n2. ಪೋಷಕ ಡೋಸ್‌ಗಳನ್ನು ಹಂತ ಹಂತವಾಗಿ ನೀಡಲಾಯಿತು.\n3. ಪ್ರತಿ ಎರಡು ವಾರಕ್ಕೊಮ್ಮೆ ಕಬ್ಬಿನ ದಪ್ಪ ದಾಖಲಿಸಲಾಯಿತು."
        ),
        SuccessStory(
            id = "3",
            farmerName = "Mallappa Hegde",
            location = "Sirsi, Karnataka",
            crop = "Pepper",
            title = "Sirsi Pepper Block Revival",
            summary = "Pepper vine management reduced spike drop and improved harvest quality.",
            imageRes = R.drawable.pepper,
            yieldBadge = "27% Yield Increase",
            yieldBadgeKn = "27% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "Timely vine nutrition and disease alerts helped me keep pepper spikes healthy till harvest.",
            quoteKn = "ಸಮಯಕ್ಕೆ ನೀಡಿದ ಬೆಳೆ ಪೋಷಣೆ ಮತ್ತು ರೋಗ ನಿಗಾದಿಂದ ಮೆಣಸು ಸ್ಪೈಕ್‌ಗಳು ಕೊಯ್ಲುವರೆಗೂ ಉತ್ತಮವಾಗಿದ್ದವು.",
            achievement = "Stabilized pepper output by improving vine health and preventive disease control.",
            achievementKn = "ವೈನ್ ಆರೋಗ್ಯ ಸುಧಾರಣೆ ಮತ್ತು ತಡೆಗಟ್ಟುವ ರೋಗ ನಿಯಂತ್ರಣದಿಂದ ಮೆಣಸು ಉತ್ಪಾದನೆ ಸ್ಥಿರವಾಯಿತು.",
            problems = "Excess shade and delayed disease response caused spike drop in previous season.",
            problemsKn = "ಹೆಚ್ಚಿನ ನೆರಳು ಮತ್ತು ತಡವಾದ ರೋಗ ಪ್ರತಿಕ್ರಿಯೆಯಿಂದ ಹಿಂದಿನ ಋತುವಿನಲ್ಲಿ ಸ್ಪೈಕ್ ಉದುರುವಿಕೆ ಹೆಚ್ಚಿತ್ತು.",
            solutions = "Balanced shade pruning, micronutrient spray, and rain-period preventive schedule.",
            solutionsKn = "ಸಮತೋಲನ ನೆರಳು ಕತ್ತರಿಕೆ, ಸೂಕ್ಷ್ಮಪೋಷಕ ಸಿಂಪಡಣೆ ಮತ್ತು ಮಳೆ ಅವಧಿಯ ತಡೆಗಟ್ಟುವ ವೇಳಾಪಟ್ಟಿ ಅನುಸರಿಸಲಾಯಿತು.",
            steps = "1. Pruned dense shade before monsoon.\n2. Started fortnightly vine scouting.\n3. Followed preventive spray window during humid weeks.",
            stepsKn = "1. ಮಳೆಗಾಲದ ಮೊದಲು ಗಿಡದ ಗಟ್ಟಿಯಾದ ನೆರಳನ್ನು ಕಡಿತಗೊಳಿಸಲಾಯಿತು.\n2. ಪಕ್ವಾವಧಿ ವೈನ್ ಪರಿಶೀಲನೆ ಆರಂಭಿಸಲಾಯಿತು.\n3. ತೇವ ಹೆಚ್ಚಿರುವ ವಾರಗಳಲ್ಲಿ ತಡೆಗಟ್ಟುವ ಸಿಂಪಡಣೆ ಅನುಸರಿಸಲಾಯಿತು."
        ),
        SuccessStory(
            id = "4",
            farmerName = "Savitri D.",
            location = "Dharwad, Karnataka",
            crop = "Red Chilli",
            title = "Dharwad Chilli Quality Boost",
            summary = "Thrips control and spray timing improved chilli quality and grade.",
            imageRes = R.drawable.red_chilli,
            yieldBadge = "29% Yield Increase",
            yieldBadgeKn = "29% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "By following weekly thrips alerts and early sprays, my chilli curl damage came down a lot.",
            quoteKn = "ವಾರದ ತ್ರಿಪ್ಸ್ ಎಚ್ಚರಿಕೆ ಮತ್ತು ಬೇಗ ಸಿಂಪಡಣೆ ಅನುಸರಿಸಿದ ಕಾರಣ ಮೆಣಸಿನಕಾಯಿ ಎಲೆ ಮುಡುವಿಕೆ ಹಾನಿ ಬಹಳ ಕಡಿಮೆಯಾಯಿತು.",
            achievement = "Improved marketable chilli pickings with pest hotspot mapping and timely intervention.",
            achievementKn = "ಕೀಟ ಹಾಟ್‌ಸ್ಪಾಟ್ ನಕ್ಷೆ ಹಾಗೂ ಸಕಾಲಿಕ ಕ್ರಮದಿಂದ ಮಾರುಕಟ್ಟೆ ಗುಣಮಟ್ಟದ ಮೆಣಸಿನ ಕೊಯ್ಲು ಹೆಚ್ಚಿತು.",
            problems = "Dry wind periods triggered thrips outbreaks and uneven fruit quality.",
            problemsKn = "ಒಣಗಾಳಿ ಅವಧಿಯಲ್ಲಿ ತ್ರಿಪ್ಸ್ ದಾಳಿ ಹೆಚ್ಚಾಗಿ ಹಣ್ಣಿನ ಗುಣಮಟ್ಟ ಅಸಮವಾಗುತ್ತಿತ್ತು.",
            solutions = "Started hotspot-first sprays, neem support sprays, and field sanitation.",
            solutionsKn = "ಮೊದಲು ಹಾಟ್‌ಸ್ಪಾಟ್ ಭಾಗ ಸಿಂಪಡಣೆ, ಬೇವು ಸಹಾಯಕ ಸಿಂಪಡಣೆ ಮತ್ತು ಹೊಲ ಸ್ವಚ್ಛತೆ ಅನುಸರಿಸಲಾಯಿತು.",
            steps = "1. Identified hotspot rows twice weekly.\n2. Applied recommended dose in evening hours.\n3. Maintained 5-7 day pest log.",
            stepsKn = "1. ವಾರಕ್ಕೆ ಎರಡು ಬಾರಿ ಹಾಟ್‌ಸ್ಪಾಟ್ ಸಾಲುಗಳನ್ನು ಗುರುತಿಸಲಾಯಿತು.\n2. ಸಂಜೆ ಸಮಯದಲ್ಲಿ ಶಿಫಾರಸಾದ ಡೋಸ್ ಸಿಂಪಡಿಸಲಾಯಿತು.\n3. 5-7 ದಿನಗಳ ಕೀಟ ನಿಗಾ ದಾಖಲೆ ಇಡಲಾಯಿತು."
        ),
        SuccessStory(
            id = "5",
            farmerName = "Imran P.",
            location = "Belagavi, Karnataka",
            crop = "Jowar",
            title = "Belagavi Jowar Resilience Model",
            summary = "Moisture-focused jowar practices improved grain fill under dry spells.",
            imageRes = R.drawable.jowar,
            yieldBadge = "24% Yield Increase",
            yieldBadgeKn = "24% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "Line sowing and stage-wise nutrition helped my jowar hold better even during moisture stress weeks.",
            quoteKn = "ಸಾಲು ಬಿತ್ತನೆ ಮತ್ತು ಹಂತವಾರು ಪೋಷಕಾಂಶ ನಿರ್ವಹಣೆಯಿಂದ ಒಣ ವಾರಗಳಲ್ಲೂ ನನ್ನ ಜೋಳದಲ್ಲಿ ಉತ್ತಮ ಕಾಳು ತುಂಬುವಿಕೆ ಕಂಡುಬಂತು.",
            achievement = "Raised jowar grain weight by improving spacing, soil moisture retention, and nutrient timing.",
            achievementKn = "ಸರಿಯಾದ ಅಂತರ, ಮಣ್ಣಿನ ತೇವ ಸಂರಕ್ಷಣೆ ಮತ್ತು ಪೋಷಕ ಸಮಯದಿಂದ ಜೋಳದ ಕಾಳಿನ ತೂಕ ಹೆಚ್ಚಿತು.",
            problems = "Erratic rainfall led to weak tillers and poor grain set in previous season.",
            problemsKn = "ಅನಿಯಮಿತ ಮಳೆಯಿಂದ ಹಿಂದಿನ ಋತುವಿನಲ್ಲಿ ಟಿಲ್ಲರ್ ದುರ್ಬಲತೆ ಮತ್ತು ಕಾಳು ಸೆಟ್ ಕಡಿಮೆ ಇತ್ತು.",
            solutions = "Adopted ridge-furrow layout, split top dressing, and early stress monitoring.",
            solutionsKn = "ರಿಡ್ಜ್-ಫರೋ ವಿನ್ಯಾಸ, ವಿಭಜಿತ ಮೇಲ್ಚೇರಿಕೆ ಮತ್ತು ಆರಂಭಿಕ ಒತ್ತಡ ನಿಗಾವಹಣೆ ಅನುಸರಿಸಲಾಯಿತು.",
            steps = "1. Corrected seed spacing and depth.\n2. Applied split nutrients at key growth stages.\n3. Used light irrigation at flowering where needed.",
            stepsKn = "1. ಬೀಜ ಅಂತರ ಮತ್ತು ಆಳವನ್ನು ಸರಿಪಡಿಸಲಾಯಿತು.\n2. ಪ್ರಮುಖ ಬೆಳವಣಿಗೆ ಹಂತಗಳಲ್ಲಿ ವಿಭಜಿತ ಪೋಷಕ ನೀಡಲಾಯಿತು.\n3. ಅಗತ್ಯವಿರುವಲ್ಲಿ ಹೂ ಹಂತದಲ್ಲಿ ಹಗುರ ನೀರಾವರಿ ಮಾಡಲಾಯಿತು."
        ),
        SuccessStory(
            id = "6",
            farmerName = "Manjunath B.",
            location = "Mandya, Karnataka",
            crop = "Paddy",
            title = "Mandya Paddy Quality Story",
            summary = "Field-level moisture control improved both quantity and grain quality.",
            imageRes = R.drawable.rice,
            yieldBadge = "21% Yield Increase",
            yieldBadgeKn = "21% ಇಳುವರಿ ಹೆಚ್ಚಳ",
            quoteEn = "Using advisory-based irrigation intervals helped me reduce wastage and improve paddy grain quality.",
            quoteKn = "ಸಲಹೆ ಆಧಾರಿತ ನೀರಾವರಿ ಅಂತರ ಪಾಲಿಸಿದ ಪರಿಣಾಮ ನೀರಿನ ವ್ಯರ್ಥ ಕಡಿಮೆಯಾಗಿ ಭತ್ತದ ಕಾಳಿನ ಗುಣಮಟ್ಟ ಹೆಚ್ಚಾಯಿತು.",
            achievement = "Improved paddy quality grade and reduced unnecessary irrigation cycles.",
            achievementKn = "ಭತ್ತದ ಗುಣಮಟ್ಟ ಶ್ರೇಣಿ ಸುಧಾರಿಸಿ ಅನಾವಶ್ಯಕ ನೀರಾವರಿ ಚಕ್ರಗಳನ್ನು ಕಡಿತಗೊಳಿಸಲಾಯಿತು.",
            problems = "Earlier, fixed-day irrigation caused overwatering and patchy crop vigor.",
            problemsKn = "ಹಿಂದೆ ನಿಶ್ಚಿತ ದಿನದ ನೀರಾವರಿಯಿಂದ ಅತಿಯಾದ ನೀರು ಹಾಗೂ ಅಸಮ ಗಿಡ ಬೆಳವಣಿಗೆ ಆಗುತ್ತಿತ್ತು.",
            solutions = "Used moisture checks, balanced fertilizer timing, and regular pest scouting.",
            solutionsKn = "ತೇವಾಂಶ ಪರಿಶೀಲನೆ, ಸಮತೋಲನ ಗೊಬ್ಬರ ಸಮಯ ಮತ್ತು ನಿಯಮಿತ ಕೀಟ ನಿಗಾ ಅನುಸರಿಸಲಾಯಿತು.",
            steps = "1. Shifted to moisture-based irrigation decision.\n2. Balanced top dressing by stage.\n3. Maintained weekly field advisory checklist.",
            stepsKn = "1. ತೇವಾಂಶ ಆಧಾರಿತ ನೀರಾವರಿ ನಿರ್ಧಾರಕ್ಕೆ ಬದಲಾಯಿಸಲಾಯಿತು.\n2. ಬೆಳೆ ಹಂತಕ್ಕೆ ಅನುಗುಣವಾಗಿ ಟಾಪ್ ಡ್ರೆಸಿಂಗ್ ಸಮತೋಲನ ಮಾಡಲಾಯಿತು.\n3. ವಾರದ ಹೊಲ ಸಲಹೆ ಪರಿಶೀಲನಾ ಪಟ್ಟಿಯನ್ನು ಕಾಪಾಡಲಾಯಿತು."
        ),
    )
}
