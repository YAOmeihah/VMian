package com.java.vmian.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 状态栏占位组件
 * 用于在沉浸式状态栏模式下为内容添加适当的上边距
 */
@Composable
fun StatusBarSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.systemBars)
    )
}

/**
 * 底部导航栏占位组件
 * 用于在沉浸式导航栏模式下为内容添加适当的下边距
 */
@Composable
fun NavigationBarSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.systemBars)
    )
}

/**
 * 带状态栏适配的内容容器
 * 自动为内容添加状态栏高度的上边距
 */
@Composable
fun StatusBarAwareContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
    ) {
        content()
    }
}

/**
 * 完整的边到边适配内容容器
 * 实现真正的沉浸式体验，内容延伸到系统UI区域
 */
@Composable
fun EdgeToEdgeContent(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.systemBars.only(
                    WindowInsetsSides.Horizontal
                )
            )
    ) {
        content()
    }
}

/**
 * 沉浸式内容容器，支持自定义状态栏和导航栏处理
 */
@Composable
fun ImmersiveContent(
    modifier: Modifier = Modifier,
    includeStatusBar: Boolean = true,
    includeNavigationBar: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val insets = if (includeStatusBar && includeNavigationBar) {
        WindowInsets.systemBars
    } else if (includeStatusBar) {
        WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    } else if (includeNavigationBar) {
        WindowInsets.systemBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
    } else {
        WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(insets)
    ) {
        content()
    }
}
