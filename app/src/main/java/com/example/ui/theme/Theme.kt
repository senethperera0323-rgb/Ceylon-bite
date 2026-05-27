package com.example.ui.theme

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
    primary = BoldDarkPrimary,
    secondary = BoldDarkPrimary,
    tertiary = BoldDarkPrimary,
    background = BoldDarkBg,
    surface = BoldDarkContainer,
    onPrimary = Color.Black,
    onSecondary = BoldDarkTextPrimary,
    onBackground = BoldDarkTextPrimary,
    onSurface = BoldDarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BoldPrimary,
    secondary = BoldSecondary,
    tertiary = BoldPrimary,
    background = BoldBg,
    surface = BoldContainer,
    onPrimary = Color.White,
    onSecondary = BoldTextPrimary,
    onBackground = BoldTextPrimary,
    onSurface = BoldTextPrimary,
    primaryContainer = BoldSecondary,
    secondaryContainer = BoldContainer,
    onPrimaryContainer = BoldTextPrimary,
    onSecondaryContainer = BoldTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to preserve the exact brand identity requested
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
