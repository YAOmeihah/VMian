package com.java.vmian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.java.vmian.presentation.ui.MainScreen
import com.java.vmian.presentation.ui.PermissionScreen
import com.java.vmian.ui.theme.VMianTheme

/**
 * 主活动
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VMianTheme {
                val navController = rememberNavController()

                // 防抖动状态管理
                var lastNavigationTime by remember { mutableLongStateOf(0L) }

                // 防抖动导航函数
                fun navigateWithDebounce(route: String) {
                    val currentTime = System.currentTimeMillis()
                    val currentRoute = navController.currentDestination?.route

                    // 双重检查：时间间隔 + 路由状态
                    if (currentTime - lastNavigationTime > 500L && currentRoute != route) {
                        lastNavigationTime = currentTime
                        navController.navigate(route)
                    }
                }

                // 使用NavHost实现真正的预测性返回，优化动画避免白屏
                // 自定义动画容器，解决白色边框问题
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                    navController = navController,
                    startDestination = "main",
                    // 进入动画：滑动 + 缩放 + 淡入，实现真正的预测性返回
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + scaleIn(
                            initialScale = 0.85f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                    },
                    // 退出动画：滑动 + 缩放 + 淡出，保持在背景可见
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        ) + scaleOut(
                            targetScale = 0.90f,
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        )
                    },
                    // 预测性返回进入动画：从左侧滑入，目标页面恢复显示
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        ) + scaleIn(
                            initialScale = 0.90f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    },
                    // 预测性返回退出动画：向右滑出，跟随手势缩放
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        ) + scaleOut(
                            targetScale = 0.85f,
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = 250,
                                easing = FastOutLinearInEasing
                            )
                        )
                    }
                ) {
                    composable("main") {
                        MainScreen(
                            onNavigateToPermissions = {
                                navigateWithDebounce("permissions")
                            }
                        )
                    }
                    composable("permissions") {
                        PermissionScreen(
                            onNavigateBack = {
                                // 检查当前是否在权限页面，避免重复返回
                                if (navController.currentDestination?.route == "permissions") {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
                }
            }
        }
    }
}