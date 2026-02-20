/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components.battery

import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.ui.overall.OverallBatteryInfo
import kotlin.math.abs

@Composable
fun BatteryWeatherCard(
    info: OverallBatteryInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val engine = remember { BatteryWeatherEngine() }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    
    // Gunakan rememberUpdatedState untuk data real-time
    val currentInfoState by rememberUpdatedState(info)

    val timePhase = remember { BatteryTimeSystem.getCurrentPhase() }
    val theme = remember(timePhase) { BatteryTimeSystem.getTheme(timePhase) }

    val uiIsCharging = info.status.contains("Charging", ignoreCase = true) && 
                       !info.status.contains("Discharging", ignoreCase = true) &&
                       !info.status.contains("Not", ignoreCase = true)

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                val safeW = if (canvasSize.width > 0) canvasSize.width else 1000f
                val safeH = if (canvasSize.height > 0) canvasSize.height else 1000f
                
                val latestInfo = currentInfoState
                val isChargingNow = latestInfo.status.contains("Charging", ignoreCase = true) && 
                                   !latestInfo.status.contains("Discharging", ignoreCase = true) &&
                                   !latestInfo.status.contains("Not", ignoreCase = true)

                engine.update(
                    isCharging = isChargingNow,
                    currentNow = latestInfo.currentNow,
                    width = safeW,
                    height = safeH
                )
            }
        }
    }

    Card(
        modifier = modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(28.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) 
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width != canvasSize.width || size.height != canvasSize.height) {
                    canvasSize = size
                }

                val width = size.width
                val height = size.height
                if (width <= 1f || height <= 1f) return@Canvas

                val isRenderingCharging = uiIsCharging

                // A. LANGIT
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.skyTop, theme.skyBottom),
                        startY = 0f,
                        endY = height * 0.8f
                    )
                )

                // B. BENDA LANGIT
                if (theme.starAlpha > 0f && !isRenderingCharging) {
                    engine.stars.forEach { star ->
                        BatteryArtAssets.run {
                            drawStar(star.x * width, star.y * height, star.currentAlpha * theme.starAlpha)
                        }
                    }
                }
                
                drawCircle(
                    color = theme.celestialColor,
                    radius = width * 0.12f,
                    center = Offset(width * 0.8f, height * 0.15f),
                    alpha = if(isRenderingCharging) 0.5f else 0.9f
                )

                // C. OBJEK UDARA
                if (isRenderingCharging) {
                    BatteryArtAssets.run {
                        drawLightningBolt(engine.lightningX, height * 0.1f, height * 0.6f, Color(0xFFFFF59D), engine.lightningAlpha)
                    }
                    BatteryArtAssets.run {
                        drawCloud(width * 0.2f, height * 0.12f, width * 0.3f, Color.DarkGray.copy(alpha=0.7f))
                        drawCloud(width * 0.6f, height * 0.08f, width * 0.35f, Color.Gray.copy(alpha=0.8f))
                        drawCloud(width * 0.85f, height * 0.15f, width * 0.28f, Color.DarkGray.copy(alpha=0.6f))
                    }
                } else {
                    BatteryArtAssets.run {
                        drawCloud(width * 0.15f, height * 0.15f, width * 0.2f, theme.cloudColor)
                        drawCloud(width * 0.6f, height * 0.1f, width * 0.25f, theme.cloudColor)
                    }
                    engine.birds.forEach { bird ->
                        BatteryArtAssets.run {
                            drawBird(bird.x, bird.y, 15f, theme.celestialColor.copy(alpha = 0.8f), bird.wingPhase)
                        }
                    }
                }

                // D. LAUTAN
                val levelInt = info.level.replace("%", "").toIntOrNull() ?: 50
                val visualLevel = (levelInt / 100f).coerceIn(0.2f, 0.85f)
                val baseWaterHeight = height * (1 - visualLevel)
                val baseAmplitude = 10.dp.toPx() + (if(isRenderingCharging) 15.dp.toPx() else 0f) 

                val backWavePath = Path().apply {
                    moveTo(0f, height)
                    var x = 0f; val step = 10f
                    while (x <= width + step) {
                        val y = engine.calculateWaterLevel((x/width) + 0.3f, baseWaterHeight + 15f, baseAmplitude * 0.7f)
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(width, height); lineTo(0f, height); close()
                }
                drawPath(backWavePath, theme.waterColor.copy(alpha = 0.4f))

                val frontWavePath = Path().apply {
                    moveTo(0f, height)
                    var x = 0f; val step = 5f
                    while (x <= width + step) {
                        val y = engine.calculateWaterLevel(x/width, baseWaterHeight, baseAmplitude)
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(width, height); lineTo(0f, height); close()
                }
                
                drawPath(
                    path = frontWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(theme.waterColor.copy(alpha=0.85f), theme.waterColor),
                        startY = baseWaterHeight - baseAmplitude,
                        endY = height
                    )
                )

                // E. KAPAL (DINAMIS DENGAN AI)
                // Hitung posisi X berdasarkan logic AI di engine
                val boatX = engine.boatXPercent * width 
                
                // Kalkulasi Y dan Rotasi berdasarkan ombak di titik X tersebut
                val physics = engine.getBoatPhysics(
                    boatXNormalized = engine.boatXPercent, // Pakai persen dari engine
                    baseHeight = baseWaterHeight, 
                    baseAmplitude = baseAmplitude,
                    viewWidth = width 
                )
                
                val boatY = physics.first + 5.dp.toPx() 
                
                BatteryArtAssets.run {
                    drawBoatReflection(boatX, boatY, physics.second, Color.Black, 0.85f)
                    drawBoat(
                        x = boatX,
                        y = boatY,
                        rotation = physics.second,
                        color = Color.White,
                        scale = 0.85f
                    )
                }

                // F. HUJAN
                if (isRenderingCharging) {
                    engine.rainDrops.forEach { drop ->
                        val start = Offset(drop.x, drop.y)
                        val end = Offset(drop.x - 10f, drop.y + drop.length)
                        drawLine(color = Color.White.copy(alpha = 0.5f), start = start, end = end, strokeWidth = 2f)
                    }
                    val stormIntensity = (abs(info.currentNow) / 3000f).coerceIn(0.2f, 0.6f)
                    drawRect(Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = stormIntensity)),
                        center = Offset(width/2, height/2),
                        radius = width
                    ))
                }
            }
            
            // G. TEXT INFO
            val isDay = timePhase == TimePhase.DAY
            val forceDarkText = uiIsCharging || !isDay
            val textColor = if (forceDarkText) Color.White else Color.Black.copy(alpha = 0.85f)
            val subTextColor = if (forceDarkText) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f)

            Column(
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Battery", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = subTextColor)
                Spacer(modifier = Modifier.weight(1f))
                Text(info.level, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(info.status, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = subTextColor)
            }
        }
    }
}
