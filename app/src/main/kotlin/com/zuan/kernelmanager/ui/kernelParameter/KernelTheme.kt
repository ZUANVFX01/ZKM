package com.zuan.kernelmanager.ui.kernelParameter

import androidx.compose.ui.graphics.Color

data class ZkmColors(
    val background: Color,
    val cardBg: Color,
    val cardActive: Color,
    val textMain: Color,
    val textSub: Color,
    val pillBg: Color,
    val border: Color,
    val tabAccent: Color
)

val DarkPalette = ZkmColors(
    background = Color(0xFF0F1014),
    cardBg = Color(0xFF1C1C1E),
    cardActive = Color(0xFF2C2C2E),
    textMain = Color(0xFFFFFFFF),
    textSub = Color(0xFF9CA3AF),
    pillBg = Color(0xFF000000),
    border = Color.White.copy(alpha = 0.08f),
    tabAccent = Color(0xFFFACC15)
)

val LightPalette = ZkmColors(
    background = Color(0xFFF3F4F6),
    cardBg = Color(0xFFFFFFFF),
    cardActive = Color(0xFFEEF2FF),
    textMain = Color(0xFF1F2937),
    textSub = Color(0xFF6B7280),
    pillBg = Color(0xFFE5E7EB),
    border = Color.Transparent,
    tabAccent = Color(0xFF6366F1)
)
