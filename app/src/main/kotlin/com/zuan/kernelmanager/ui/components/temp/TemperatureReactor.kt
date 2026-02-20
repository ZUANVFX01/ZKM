/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components.temp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.ui.overall.OverallBatteryInfo

@Composable
fun TemperatureReactorCard(
    info: OverallBatteryInfo,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    // 1. Ambil data suhu, bersihkan string "36.5 °C" jadi Float 36.5
    val tempValue = info.temp.replace(" °C", "").toFloatOrNull() ?: 30f

    // 2. Tentukan Warna & State berdasarkan suhu (Logic yang kamu minta tadi)
    val (targetColor, statusText) = when {
        tempValue < 30f -> Color(0xFF00E5FF) to "Freezing"     // Cyan (Dingin banget)
        tempValue < 36f -> Color(0xFF00C853) to "Cool"         // Hijau (Normal)
        tempValue < 41f -> Color(0xFFFFC107) to "Warm"         // Kuning (Mulai anget)
        tempValue < 45f -> Color(0xFFFF5722) to "Hot"          // Orange (Panas)
        else -> Color(0xFFD50000) to "Overheat"                // Merah (Bahaya)
    }

    // Animasi perubahan warna biar halus (gak kaget pas suhu naik 1 derajat)
    val animatedColor by animateColorAsState(
        targetValue = targetColor, 
        animationSpec = tween(1000), 
        label = "ColorAnim"
    )

    // Animasi "Denyut" (Breathing Effect)
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    // Kecepatan denyut makin ngebut kalau makin panas
    val pulseDuration = when {
        tempValue > 43f -> 600   // Cepat banget (Panic mode)
        tempValue > 38f -> 1000  // Sedang
        else -> 2000             // Santai
    }

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDuration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing), // Rotasi pelan biar aesthetic
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bagian Kiri: Teks Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Temperature",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tempValue}°C",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                // Status Badge Kecil
                Surface(
                    color = animatedColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = statusText,
                        color = animatedColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bagian Kanan: Visual Reactor (Canvas)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp) // Ukuran Visual
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = size.width / 2

                    // 1. Lingkaran Dasar (Track) - Gelap
                    drawCircle(
                        color = textColor.copy(alpha = 0.1f),
                        radius = radius,
                        style = Stroke(width = 8.dp.toPx())
                    )

                    // 2. Glow Effect (Pulsing) di tengah
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * pulseScale // Ikut animasi denyut
                        ),
                        radius = radius * pulseScale
                    )

                    // 3. Cincin Indikator (Mutar pelan)
                    // Menggambar arc putus-putus biar kayak sci-fi UI
                    rotate(rotationAnim) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(Color.Transparent, animatedColor, Color.Transparent)
                            ),
                            startAngle = -90f,
                            sweepAngle = 200f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    // 4. Lingkaran Solid Kecil di tengah (Core)
                    drawCircle(
                        color = animatedColor,
                        radius = 6.dp.toPx()
                    )
                }
            }
        }
    }
}
