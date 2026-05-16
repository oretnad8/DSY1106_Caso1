package com.smartlogix.app.ui.theme

import androidx.compose.ui.graphics.Color

import android.app.Activity
import android.os.Build
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VibrantBlue,
    onPrimary = Color.White,
    secondary = VibrantBlueBright,
    onSecondary = Color.White,
    tertiary = Navylighter,
    background = NavyDark,
    surface = NavySurface,
    onBackground = Slate50,
    onSurface = VibrantBlue, // TITULOS EN AZUL VIBRANTE
    surfaceVariant = NavySurface,
    onSurfaceVariant = Slate300, // TEXTO SECUNDARIO EN GRIS CLARO
    primaryContainer = Navylighter,
    onPrimaryContainer = VibrantBlue // ICONOS EN AZUL VIBRANTE
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantBlueDark,
    onPrimary = Color.White,
    secondary = VibrantBlue,
    onSecondary = Color.White,
    tertiary = VibrantBlueBright,
    background = Color.White,
    surface = Color(0xFFF8FAFC),
    onBackground = NavyDark,
    onSurface = NavyDark,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun TestTheme(
    darkTheme: Boolean = false,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Status Bar
            insetsController.isAppearanceLightStatusBars = !darkTheme
            
            // Navigation Bar
            window.navigationBarColor = NavyDark.toArgb()
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

