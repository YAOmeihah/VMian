package com.java.vmian.ui.theme

import androidx.compose.ui.graphics.Color

// === V免签现代配色方案 ===
// 设计理念：现代蓝色系，专业可信且简洁优雅

// 主色系 - 深海蓝主题
val PrimaryBlue = Color(0xFF1565C0)        // 深海蓝 - 主要操作按钮
val PrimaryBlueLight = Color(0xFF64B5F6)   // 浅海蓝 - 深色模式主色
val PrimaryBlueContainer = Color(0xFFE3F2FD) // 主色容器 - 浅色模式
val PrimaryBlueDarkContainer = Color(0xFF1976D2) // 主色容器 - 深色模式

// 辅助色系 - 蓝灰色
val SecondaryBlueGray = Color(0xFF455A64)  // 深蓝灰 - 辅助操作
val SecondaryBlueGrayLight = Color(0xFF90A4AE) // 浅蓝灰 - 深色模式辅助色
val SecondaryBlueGrayContainer = Color(0xFFECEFF1) // 辅助色容器 - 浅色模式
val SecondaryBlueGrayDarkContainer = Color(0xFF546E7A) // 辅助色容器 - 深色模式

// 背景色系 - 现代中性色
val BackgroundLight = Color(0xFFFAFAFA)    // 浅色背景 - 温暖白
val BackgroundDark = Color(0xFF0F1419)     // 深色背景 - 深蓝灰
val SurfaceLight = Color(0xFFFFFFFF)       // 浅色表面 - 纯白
val SurfaceDark = Color(0xFF1A1F24)        // 深色表面 - 蓝灰

// 文字色系 - 高对比度
val OnBackgroundLight = Color(0xFF1A1C1E)  // 浅色背景文字 - 深灰
val OnBackgroundDark = Color(0xFFE1E3E6)   // 深色背景文字 - 浅灰
val OnSurfaceLight = Color(0xFF1A1C1E)     // 浅色表面文字 - 深灰
val OnSurfaceDark = Color(0xFFE1E3E6)      // 深色表面文字 - 浅灰

// 状态指示色 - 语义化颜色
val SuccessGreen = Color(0xFF2E7D32)       // 成功绿 - 深绿色
val WarningAmber = Color(0xFFEF6C00)       // 警告琥珀 - 深橙色
val InfoCyan = Color(0xFF0277BD)           // 信息青 - 深青色
val ErrorRed = Color(0xFFD32F2F)           // 错误红 - 深红色

// 中性色系 - 灰度层次
val NeutralGray50 = Color(0xFFF8F9FA)      // 极浅灰
val NeutralGray100 = Color(0xFFF1F3F4)     // 浅灰
val NeutralGray200 = Color(0xFFE8EAED)     // 中浅灰
val NeutralGray300 = Color(0xFFDADCE0)     // 中灰
val NeutralGray400 = Color(0xFFBDC1C6)     // 深中灰
val NeutralGray500 = Color(0xFF9AA0A6)     // 深灰
val NeutralGray600 = Color(0xFF80868B)     // 更深灰
val NeutralGray700 = Color(0xFF5F6368)     // 深色灰
val NeutralGray800 = Color(0xFF3C4043)     // 极深灰
val NeutralGray900 = Color(0xFF202124)     // 最深灰

// === 兼容性别名 - 保持现有代码正常工作 ===
val WarningOrange = WarningAmber           // 兼容旧的警告橙色名称
val InfoBlue = InfoCyan                    // 兼容旧的信息蓝色名称
