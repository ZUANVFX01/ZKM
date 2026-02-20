/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components.battery

import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class TimePhase {
    DAWN,   // Subuh (05-07) - Berkabut
    DAY,    // Siang (07-16) - Cerah
    DUSK,   // Sore (16-18) - Sunset
    NIGHT   // Malam (18-05) - Gelap/Bulan
}

data class WeatherTheme(
    val skyTop: Color,
    val skyBottom: Color,
    val waterColor: Color,
    val cloudColor: Color,
    val celestialColor: Color, // Matahari/Bulan
    val starAlpha: Float // 0f (Siang) - 1f (Malam)
)

object BatteryTimeSystem {

    fun getCurrentPhase(): TimePhase {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..6 -> TimePhase.DAWN
            in 7..16 -> TimePhase.DAY
            in 17..18 -> TimePhase.DUSK
            else -> TimePhase.NIGHT
        }
    }

    fun getTheme(phase: TimePhase): WeatherTheme {
        return when (phase) {
            TimePhase.DAWN -> WeatherTheme(
                skyTop = Color(0xFF5D4037),       // Coklat gelap pudar
                skyBottom = Color(0xFFFFCCBC),    // Oranye kabut
                waterColor = Color(0xFF795548),   // Air keruh berkabut
                cloudColor = Color(0xFFFFAB91).copy(alpha = 0.5f),
                celestialColor = Color(0xFFFFE0B2), // Matahari terbit pucat
                starAlpha = 0.0f
            )
            TimePhase.DAY -> WeatherTheme(
                skyTop = Color(0xFF29B6F6),       // Biru Langit
                skyBottom = Color(0xFFB3E5FC),    // Putih awan
                waterColor = Color(0xFF0288D1),   // Biru laut segar
                cloudColor = Color.White.copy(alpha = 0.9f),
                celestialColor = Color(0xFFFFEB3B), // Matahari kuning cerah
                starAlpha = 0.0f
            )
            TimePhase.DUSK -> WeatherTheme(
                skyTop = Color(0xFF4A148C),       // Ungu gelap
                skyBottom = Color(0xFFFF5722),    // Merah oranye sunset
                waterColor = Color(0xFF311B92),   // Laut ungu
                cloudColor = Color(0xFFFF8A65).copy(alpha = 0.7f),
                celestialColor = Color(0xFFFF9800), // Matahari oranye
                starAlpha = 0.3f
            )
            TimePhase.NIGHT -> WeatherTheme(
                skyTop = Color(0xFF000000),       // Hitam pekat
                skyBottom = Color(0xFF1A237E),    // Biru midnight
                waterColor = Color(0xFF0D47A1),   // Laut malam
                cloudColor = Color(0xFF90A4AE).copy(alpha = 0.2f),
                celestialColor = Color(0xFFFAFAFA), // Bulan putih bersinar
                starAlpha = 1.0f
            )
        }
    }
}
