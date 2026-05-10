package com.example.raitha_varta.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

private const val LANG_EN = "en"
private const val LANG_KN = "kn"

@Composable
fun WelcomeScreen(
    languageCode: String,
    onLanguageChange: (String) -> Unit,
    onGetStarted: () -> Unit,
    onCreateAccount: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    val isKn = languageCode == LANG_KN

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0E1A12))
        )

        // ── FULL-SCREEN FARM IMAGE ──────────────────────────────────
        AsyncImage(
            model = "https://images.unsplash.com/photo-1625246333195-78d9c38ad449?q=80&w=1200&auto=format&fit=crop",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ── TOP SCRIM ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.30f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.60f), Color.Transparent)
                    )
                )
        )

        // ── BOTTOM SCRIM (covers bottom 65% for readability) ────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.70f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // ── TOP BAR ─────────────────────────────────────────────────
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AsyncImage(
                            model = com.example.raitha_varta.R.drawable.logo,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                        Text("Raitha Varta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Language pill
                Surface(
                    onClick = { onLanguageChange(if (isKn) LANG_EN else LANG_KN) },
                    shape = RoundedCornerShape(999.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Default.Language, null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Text(
                            text = if (isKn) "English" else "ಕನ್ನಡ",
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White
                        )
                    }
                }
            }
        }

        // ── BOTTOM CONTENT COLUMN (everything in one flow) ──────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ① Feature pills
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 100)) + slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(600, delayMillis = 100)
                )
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeaturePill(Icons.Default.WbSunny,    Color(0xFFFFC107), if (isKn) "ಹವಾಮಾನ ಎಚ್ಚರಿಕೆ"  else "Live Weather Alerts")
                    FeaturePill(Icons.Default.Lightbulb,  Color(0xFF8BC34A), if (isKn) "AI ಕೃಷಿ ಸಲಹೆ"      else "AI Farming Tips")
                    FeaturePill(Icons.Default.ShoppingCart, Color(0xFF03A9F4), if (isKn) "ಮಾರುಕಟ್ಟೆ ಬೆಲೆ"  else "Market Prices")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ② Stats row
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 250))
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatItem("100%", if (isKn) "ಉಚಿತ" else "Free")
                    StatItem("26+", if (isKn) "ಬೆಳೆಗಳು" else "Crops")
                    StatItem("2", if (isKn) "ಭಾಷೆಗಳು" else "Languages")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ③ Bold headline
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 150)) + slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = tween(700, delayMillis = 150)
                )
            ) {
                Column {
                    Text(
                        text = if (isKn) "ನಿಮ್ಮ ಜಮೀನು." else "YOUR FARM.",
                        fontSize = 42.sp, fontWeight = FontWeight.Black,
                        color = Color.White, lineHeight = 48.sp, letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (isKn) "ಸ್ಮಾರ್ಟ್." else "SMARTER.",
                        fontSize = 42.sp, fontWeight = FontWeight.Black,
                        color = Color(0xFF8BC34A), lineHeight = 48.sp, letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (isKn) "ಬೆಳವಣಿಗೆ." else "GROWTH.",
                        fontSize = 42.sp, fontWeight = FontWeight.Black,
                        color = Color.White, lineHeight = 48.sp, letterSpacing = (-1).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ④ Get started pill
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 350)) + slideInVertically(
                    initialOffsetY = { it / 5 },
                    animationSpec = tween(800, delayMillis = 350)
                )
            ) {
                val density = LocalDensity.current
                var trackWidthPx by remember { mutableStateOf(1f) }
                val thumbSizeDp = 48.dp
                val thumbSizePx = with(density) { thumbSizeDp.toPx() }
                val maxOffset = (trackWidthPx - thumbSizePx).coerceAtLeast(0f)
                var thumbOffset by remember { mutableStateOf(0f) }
                var triggered by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFF111111).copy(alpha = 0.82f))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    thumbOffset = (thumbOffset + dragAmount).coerceIn(0f, maxOffset)
                                },
                                onDragEnd = {
                                    if (!triggered && maxOffset > 0f && thumbOffset >= maxOffset * 0.82f) {
                                        triggered = true
                                        onGetStarted()
                                    }
                                    thumbOffset = 0f
                                }
                            )
                        }
                        .onSizeChanged { trackWidthPx = it.width.toFloat() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(thumbOffset.roundToInt(), 0) }
                                .size(48.dp)
                                .background(Color(0xFF4CAF50), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                "Get started",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isKn) "ಬಲಕ್ಕೆ ಸ್ವೈಪ್ ಮಾಡಿ" else "Swipe right to start",
                                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                            Text(
                                text = if (isKn) "ನಿಮ್ಮ ಕೃಷಿ ಪ್ರಯಾಣ" else "Your farming journey",
                                fontSize = 11.sp, color = Color.White.copy(alpha = 0.50f)
                            )
                        }
                        Row {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                                tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(20.dp))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                                tint = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(20.dp).offset(x = (-7).dp))
                        }
                    }
                }
            }


        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

@Composable
private fun FeaturePill(icon: ImageVector, iconTint: Color, label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(number, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF8BC34A))
        Text(label,  fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.65f))
    }
}
