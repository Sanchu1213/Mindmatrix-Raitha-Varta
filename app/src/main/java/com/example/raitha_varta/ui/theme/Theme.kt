package com.example.raitha_varta.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AgriPrimaryContainer,
    onPrimary = AgriOnPrimaryContainer,
    secondary = AgriSecondaryContainer,
    onSecondary = AgriOnSecondaryContainer,
    tertiary = AgriTertiaryContainer,
    onTertiary = AgriOnTertiaryContainer,
    background = Color(0xFF1E1B17),
    onBackground = Color(0xFFECE2D5),
    surface = Color(0xFF25211C),
    onSurface = Color(0xFFECE2D5),
    surfaceVariant = Color(0xFF3A332A),
    onSurfaceVariant = Color(0xFFD4C5B3),
    outline = Color(0xFF8D7F6F)
)

private val LightColorScheme = lightColorScheme(
    primary = AgriPrimary,
    onPrimary = AgriOnPrimary,
    primaryContainer = AgriPrimaryContainer,
    onPrimaryContainer = AgriOnPrimaryContainer,
    secondary = AgriSecondary,
    onSecondary = AgriOnSecondary,
    secondaryContainer = AgriSecondaryContainer,
    onSecondaryContainer = AgriOnSecondaryContainer,
    tertiary = AgriTertiary,
    onTertiary = AgriOnTertiary,
    tertiaryContainer = AgriTertiaryContainer,
    onTertiaryContainer = AgriOnTertiaryContainer,
    background = AgriBackground,
    onBackground = AgriOnBackground,
    surface = AgriSurface,
    onSurface = AgriOnSurface,
    surfaceVariant = AgriSurfaceVariant,
    onSurfaceVariant = AgriOnSurfaceVariant,
    outline = AgriOutline
)

@Composable
fun Raitha_VartaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}