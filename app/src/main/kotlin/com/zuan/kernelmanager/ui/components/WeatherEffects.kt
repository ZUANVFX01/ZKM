/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.ui.settings.WeatherEffect
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

/**
 * Weather Effects Overlay Component
 * Renders various weather effects: Fog, Rain, Snow, Sun Rays
 */
@Composable
fun WeatherEffectOverlay(
    effect: WeatherEffect,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    when (effect) {
        WeatherEffect.NONE -> {}
        WeatherEffect.FOG -> FogEffect(intensity, modifier)
        WeatherEffect.RAIN -> RainEffect(intensity, modifier)
        WeatherEffect.SNOW -> SnowEffect(intensity, modifier)
        WeatherEffect.SUN_RAYS -> SunRaysEffect(intensity, modifier)
    }
}

// ==================== FOG EFFECT ====================
@Composable
fun FogEffect(intensity: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog")
    
    // Multiple fog layers with different animation speeds
    val fogLayers = remember { List(5) { FogLayerData.random() } }
    
    Box(modifier = modifier.fillMaxSize()) {
        fogLayers.forEachIndexed { index, layer ->
            val offsetX by infiniteTransition.animateFloat(
                initialValue = -0.3f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (20000 + index * 5000),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "fog_$index"
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val layerIntensity = intensity * layer.opacity
                val fogColor = Color.White.copy(alpha = layerIntensity * 0.4f)
                
                withTransform({
                    translate(left = size.width * (offsetX + layer.offsetX))
                }) {
                    drawCircle(
                        color = fogColor,
                        radius = size.width * layer.radius,
                        center = Offset(
                            x = size.width * 0.5f,
                            y = size.height * layer.yPosition
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }
        }
    }
}

private data class FogLayerData(
    val radius: Float,
    val opacity: Float,
    val yPosition: Float,
    val offsetX: Float
) {
    companion object {
        fun random(): FogLayerData = FogLayerData(
            radius = 0.4f + Random.nextFloat() * 0.4f,
            opacity = 0.3f + Random.nextFloat() * 0.4f,
            yPosition = 0.2f + Random.nextFloat() * 0.6f,
            offsetX = Random.nextFloat() * 0.2f
        )
    }
}

// ==================== RAIN EFFECT ====================
@Composable
fun RainEffect(intensity: Float, modifier: Modifier = Modifier) {
    val dropCount = (50 + (intensity * 150)).toInt()
    val drops = remember { List(dropCount) { RainDropData.random() } }
    
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val globalTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drops.forEach { drop ->
            val progress = ((globalTime + drop.delay) % 1f)
            val y = size.height * progress
            val x = drop.x * size.width + (progress * drop.sway * 100)
            
            val alpha = if (progress < 0.1f) progress * 10f 
                       else if (progress > 0.9f) (1f - progress) * 10f 
                       else 1f
            
            val dropColor = Color(0xFF87CEEB).copy(alpha = alpha * intensity * 0.6f)
            
            drawLine(
                color = dropColor,
                start = Offset(x, y - drop.length),
                end = Offset(x, y),
                strokeWidth = drop.width
            )
        }
    }
}

private data class RainDropData(
    val x: Float,
    val length: Float,
    val width: Float,
    val delay: Float,
    val sway: Float
) {
    companion object {
        fun random(): RainDropData = RainDropData(
            x = Random.nextFloat(),
            length = 20f + Random.nextFloat() * 40f,
            width = 1f + Random.nextFloat() * 2f,
            delay = Random.nextFloat(),
            sway = (Random.nextFloat() - 0.5f) * 0.3f
        )
    }
}

// ==================== SNOW EFFECT ====================
@Composable
fun SnowEffect(intensity: Float, modifier: Modifier = Modifier) {
    val flakeCount = (30 + (intensity * 100)).toInt()
    val flakes = remember { List(flakeCount) { SnowFlakeData.random() } }
    
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val globalTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow_time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        flakes.forEach { flake ->
            val progress = ((globalTime * flake.speed + flake.delay) % 1f)
            val baseY = size.height * progress
            val sway = sin(progress * flake.swayFrequency * 2 * PI).toFloat() * flake.swayAmplitude
            val x = flake.x * size.width + sway * 100
            val y = baseY
            
            val alpha = if (progress < 0.05f) progress * 20f 
                       else if (progress > 0.95f) (1f - progress) * 20f 
                       else 1f
            
            val flakeColor = Color.White.copy(alpha = alpha * intensity * 0.9f)
            
            drawCircle(
                color = flakeColor,
                radius = flake.size,
                center = Offset(x, y)
            )
        }
    }
}

private data class SnowFlakeData(
    val x: Float,
    val size: Float,
    val speed: Float,
    val delay: Float,
    val swayAmplitude: Float,
    val swayFrequency: Float
) {
    companion object {
        fun random(): SnowFlakeData = SnowFlakeData(
            x = Random.nextFloat(),
            size = 2f + Random.nextFloat() * 4f,
            speed = 0.5f + Random.nextFloat() * 0.5f,
            delay = Random.nextFloat(),
            swayAmplitude = 0.5f + Random.nextFloat() * 1f,
            swayFrequency = 1f + Random.nextFloat() * 2f
        )
    }
}

// ==================== SUN RAYS EFFECT ====================
@Composable
fun SunRaysEffect(intensity: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunrays")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width * 0.8f
        val centerY = size.height * 0.1f
        val rayCount = 12
        
        rotate(rotation, Offset(centerX, centerY)) {
            for (i in 0 until rayCount) {
                val angle = (i * 360f / rayCount) * (PI / 180f)
                val rayLength = size.height * 0.8f * pulse
                
                val startX = centerX + cos(angle).toFloat() * size.width * 0.05f
                val startY = centerY + sin(angle).toFloat() * size.width * 0.05f
                val endX = centerX + cos(angle).toFloat() * rayLength
                val endY = centerY + sin(angle).toFloat() * rayLength
                
                val rayAlpha = 0.1f + (intensity * 0.2f)
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = rayAlpha),
                            Color(0xFFFFA500).copy(alpha = 0f)
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 20f + (intensity * 30f),
                    blendMode = BlendMode.Screen
                )
            }
        }
        
        // Sun glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = intensity * 0.3f),
                    Color(0xFFFFA500).copy(alpha = intensity * 0.1f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = size.width * 0.3f * pulse
            ),
            radius = size.width * 0.3f * pulse,
            center = Offset(centerX, centerY),
            blendMode = BlendMode.Screen
        )
    }
}

// ==================== SATURATION ADJUSTMENT ====================
/**
 * Modifier to apply saturation adjustment to content
 */
@Composable
fun SaturationModifier(saturation: Float): Modifier {
    return if (saturation != 1f) {
        Modifier.graphicsLayer {
            // Use color matrix to adjust saturation
            val matrix = ColorMatrix().apply {
                setToSaturation(saturation)
            }
            this.colorFilter = ColorMatrixColorFilter(matrix)
        }
    } else {
        Modifier
    }
}

// ==================== BLUR WITH STRENGTH ====================
/**
 * Applies blur with adjustable strength
 */
@Composable
fun BlurWithStrength(blurStrength: Float, content: @Composable () -> Unit) {
    if (blurStrength > 0f) {
        Box(
            modifier = Modifier.blur((blurStrength).dp)
        ) {
            content()
        }
    } else {
        content()
    }
}
