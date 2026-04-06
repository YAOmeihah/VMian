package com.java.vmian.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
// 深色模式配色 - 现代蓝绿主题
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNavyLight,
    onPrimary = Color(0xFF062033),
    primaryContainer = PrimaryNavyDarkContainer,
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = SecondarySlateLight,
    onSecondary = Color(0xFF0D1C2B),
    secondaryContainer = SecondarySlateDarkContainer,
    onSecondaryContainer = Color(0xFFD8E3EE),

    tertiary = AccentBlue,
    onTertiary = Color(0xFFE8FBFF),
    tertiaryContainer = Color(0xFF124556),
    onTertiaryContainer = Color(0xFFD8F1F7),

    error = Color(0xFFEF5350),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFDAD6),

    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = NeutralGray800,
    onSurfaceVariant = NeutralGray300,

    outline = NeutralGray600,
    outlineVariant = NeutralGray700,

    surfaceContainer = Color(0xFF1B2632),
    surfaceContainerHigh = Color(0xFF223140),
    surfaceContainerHighest = Color(0xFF2A3A4A),
    surfaceContainerLow = Color(0xFF15202B),
    surfaceContainerLowest = Color(0xFF0B121A)
)


// 浅色模式配色 - 现代蓝绿主题
private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = PrimaryNavyContainer,
    onPrimaryContainer = Color(0xFF0A2235),

    secondary = SecondarySlate,
    onSecondary = Color.White,
    secondaryContainer = SecondarySlateContainer,
    onSecondaryContainer = Color(0xFF18293A),

    tertiary = AccentBlue,
    onTertiary = Color.White,
    tertiaryContainer = AccentBlueContainer,
    onTertiaryContainer = Color(0xFF103746),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray600,

    outline = NeutralGray400,
    outlineVariant = NeutralGray200,

    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFE9EEF5),
    surfaceContainerHighest = Color(0xFFDDE5EE),
    surfaceContainerLow = Color(0xFFF7FAFD),
    surfaceContainerLowest = Color.White
)


@Composable
fun VMianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
