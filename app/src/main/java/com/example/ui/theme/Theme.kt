package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SolarGold,
    onPrimary = Color(0xFF3E2723),
    primaryContainer = Color(0xFFFFE082),
    onPrimaryContainer = Color(0xFF5D4037),
    secondary = SolarOrange,
    onSecondary = Color.White,
    tertiary = SolarTeal,
    onTertiary = Color(0xFF00332D),
    background = SolarDarkMidnight,
    onBackground = Color(0xFFECEFF1),
    surface = SolarDarkSurface,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = SolarDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCFD8DC),
    outline = Color(0xFFFFD54F).copy(alpha = 0.35f),
    outlineVariant = Color(0xFF37474F)
)

private val LightColorScheme = lightColorScheme(
    primary = SolarLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF5D4037),
    secondary = SolarLightSecondary,
    onSecondary = Color.White,
    tertiary = SolarTeal,
    onTertiary = Color.White,
    background = SolarLightClean,
    onBackground = Color(0xFF263238),
    surface = SolarLightSurface,
    onSurface = Color(0xFF263238),
    surfaceVariant = SolarLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF455A64),
    outline = SolarLightOutline,
    outlineVariant = Color(0xFFCFD8DC)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
