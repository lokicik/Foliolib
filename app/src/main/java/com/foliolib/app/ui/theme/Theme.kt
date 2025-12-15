package com.foliolib.app.ui.theme

import android.app.Activity
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
    primary = Indigo400,
    onPrimary = Color(0xFF000000),
    primaryContainer = Indigo700,
    onPrimaryContainer = Indigo300,

    secondary = Violet400,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Violet600,
    onSecondaryContainer = Violet300,

    tertiary = Pink400,
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Pink600,
    onTertiaryContainer = Pink400,

    background = DarkBackground,
    onBackground = Color(0xFFE8E4E1),
    surface = DarkSurface,
    onSurface = Color(0xFFE8E4E1),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo300,
    onPrimaryContainer = Indigo700,

    secondary = Violet500,
    onSecondary = Color.White,
    secondaryContainer = Violet300,
    onSecondaryContainer = Violet600,

    tertiary = Pink500,
    onTertiary = Color.White,
    tertiaryContainer = Pink400,
    onTertiaryContainer = Pink600,

    background = CreamLight,
    onBackground = Color(0xFF1C1B1F),
    surface = WarmWhite,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = CreamSurface,
    onSurfaceVariant = Color(0xFF49454F),

    error = Error,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun FoliolibTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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
        shapes = Shapes,
        content = content
    )
}