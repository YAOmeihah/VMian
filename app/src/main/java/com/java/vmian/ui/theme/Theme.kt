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
    // 主要颜色 - 深海蓝系
    primary = PrimaryBlueLight,
    onPrimary = Color(0xFF001A33),
    primaryContainer = PrimaryBlueDarkContainer,
    onPrimaryContainer = Color(0xFFD1E4FF),

    // 次要颜色 - 蓝灰系
    secondary = SecondaryBlueGrayLight,
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = SecondaryBlueGrayDarkContainer,
    onSecondaryContainer = Color(0xFFCFD8DC),

    // 第三颜色 - 琥珀系
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = Color(0xFFBF360C),
    onTertiaryContainer = Color(0xFFFFE0B2),

    // 错误颜色
    error = Color(0xFFEF5350),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFDAD6),

    // 背景和表面
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = NeutralGray800,
    onSurfaceVariant = NeutralGray300,

    // 轮廓
    outline = NeutralGray600,
    outlineVariant = NeutralGray700,

    // 表面容器层次
    surfaceContainer = Color(0xFF1F252A),
    surfaceContainerHigh = Color(0xFF252B30),
    surfaceContainerHighest = Color(0xFF2B3136),
    surfaceContainerLow = Color(0xFF191E23),
    surfaceContainerLowest = Color(0xFF0A0F14)
)


// 浅色模式配色 - 现代蓝绿主题
private val LightColorScheme = lightColorScheme(
    // 主要颜色 - 深海蓝系
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = Color(0xFF001A33),

    // 次要颜色 - 蓝灰系
    secondary = SecondaryBlueGray,
    onSecondary = Color.White,
    secondaryContainer = SecondaryBlueGrayContainer,
    onSecondaryContainer = Color(0xFF1A1A1A),

    // 第三颜色 - 琥珀系
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF3E2723),

    // 错误颜色
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // 背景和表面
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = NeutralGray100,
    onSurfaceVariant = NeutralGray700,

    // 轮廓
    outline = NeutralGray400,
    outlineVariant = NeutralGray200,

    // 表面容器层次
    surfaceContainer = NeutralGray50,
    surfaceContainerHigh = Color(0xFFECEFF1),
    surfaceContainerHighest = Color(0xFFE0E3E6),
    surfaceContainerLow = Color(0xFFF5F7FA),
    surfaceContainerLowest = Color.White
)


@Composable
fun VMianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // 默认启用动态颜色以支持Material You，在不支持的设备上回退到品牌色
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

    // 设置沉浸式状态栏和导航栏
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // 启用边到边显示
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // 设置状态栏和导航栏图标颜色
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
