/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components.battery

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ... (Data Class RainDrop, Bird, Star TETAP SAMA seperti sebelumnya) ...
data class RainDrop(var x: Float, var y: Float, var speed: Float, var length: Float)
data class Bird(var x: Float, var y: Float, var speed: Float, var wingPhase: Float)
data class Star(var x: Float, var y: Float, var size: Float, var blinkSpeed: Float, var currentAlpha: Float)

class BatteryWeatherEngine {

    // --- STATE ANIMASI ---
    var mainWavePhase by mutableFloatStateOf(0f)
    var secondaryWavePhase by mutableFloatStateOf(0f)
    var breathingPhase by mutableFloatStateOf(0f)

    var lightningAlpha by mutableFloatStateOf(0f)
    var lightningX by mutableFloatStateOf(0f)

    // --- [NEW] STATE PERAHU (AI NAVIGASI) ---
    var boatXPercent by mutableFloatStateOf(0.5f) // Posisi perahu (0.0 - 1.0)
    private var boatTargetX by mutableFloatStateOf(0.5f) // Tujuan perahu berikutnya
    private var boatSpeed by mutableFloatStateOf(0f) // Kecepatan saat ini
    private var boatRockingOffset by mutableFloatStateOf(0f) // Goyangan tambahan

    val rainDrops = mutableListOf<RainDrop>()
    val birds = mutableListOf<Bird>()
    val stars = mutableListOf<Star>()

    private val BASE_SPEED_MAIN = 0.0015f
    private val BASE_SPEED_SECONDARY = 0.003f
    private val BREATHING_SPEED = 0.0008f
    private val MAX_CURRENT_FOR_STORM = 2500f 

    init {
        repeat(80) { rainDrops.add(createRandomRain()) }
        repeat(5) { birds.add(createRandomBird()) }
        repeat(25) { stars.add(createRandomStar()) }
    }

    fun update(
        isCharging: Boolean,
        currentNow: Int,
        width: Float,
        height: Float
    ) {
        val currentAbs = abs(currentNow).toFloat()
        val intensity = if (isCharging) {
            (currentAbs / MAX_CURRENT_FOR_STORM).coerceIn(0.4f, 1f)
        } else {
            0f
        }

        // 1. Update Ombak (Sama seperti sebelumnya)
        val speedMultiplier = 1f + (intensity * 2.0f)
        mainWavePhase += BASE_SPEED_MAIN * speedMultiplier
        secondaryWavePhase -= BASE_SPEED_SECONDARY * speedMultiplier
        breathingPhase += BREATHING_SPEED * (1f + intensity)

        // --- [NEW] LOGIKA PERAHU BERGERAK ---
        updateBoatAI(width, isCharging, intensity)

        // 3. Logika Charging (Hujan/Petir)
        if (isCharging) {
            rainDrops.forEach { drop ->
                drop.y += drop.speed * (1f + intensity)
                drop.x -= 2.0f * (1f + intensity)
                if (drop.y > height) {
                    drop.y = -drop.length - Random.nextFloat() * 100f
                    drop.x = Random.nextFloat() * width * 1.5f 
                }
            }
            if (lightningAlpha > 0f) {
                lightningAlpha -= 0.05f 
            } else {
                if (Random.nextFloat() < (0.02f + 0.05f * intensity)) {
                    lightningAlpha = 1.0f
                    lightningX = width * 0.1f + Random.nextFloat() * (width * 0.8f)
                }
            }
        } else {
            birds.forEach { bird ->
                bird.x += bird.speed
                bird.wingPhase += 0.15f
                if (bird.x > width + 50f) {
                    bird.x = -50f
                    bird.y = 50f + Random.nextFloat() * (height * 0.4f)
                }
            }
            if (lightningAlpha > 0f) lightningAlpha = 0f
        }
        
        stars.forEach { star ->
            star.currentAlpha += star.blinkSpeed
            if (star.currentAlpha > 1f || star.currentAlpha < 0.2f) star.blinkSpeed *= -1
        }
    }

    // --- [NEW] FUNGSI OTAK PERAHU ---
    private fun updateBoatAI(width: Float, isCharging: Boolean, intensity: Float) {
        // 1. Tentukan Tujuan Baru (Random Waypoint)
        // Kalau sudah dekat target (< 0.05), cari target baru
        if (abs(boatXPercent - boatTargetX) < 0.05f) {
            // Batasi target biar perahu gak terlalu ke pinggir layar (0.2 s/d 0.8)
            boatTargetX = 0.2f + Random.nextFloat() * 0.6f
        }

        // 2. Hitung Kecepatan
        // Kalau badai (charging), perahu bergerak lebih liar/cepat
        val responsiveness = if (isCharging) 0.005f + (intensity * 0.01f) else 0.001f
        
        // Lerp position: Bergerak pelan menuju target
        val diff = boatTargetX - boatXPercent
        boatXPercent += diff * responsiveness

        // 3. Tambahkan Efek "Terombang-ambing" (Noise/Sinus)
        // Biar jalannya gak lurus kaku kayak robot
        boatRockingOffset += 0.02f
        val subtleDrift = sin(boatRockingOffset) * (if (isCharging) 0.002f else 0.0005f)
        boatXPercent = (boatXPercent + subtleDrift).coerceIn(0.1f, 0.9f)
    }

    // --- LOGIKA FISIKA ---
    fun calculateWaterLevel(
        xNormalized: Float, 
        baseHeight: Float,  
        baseAmplitude: Float 
    ): Float {
        val wave1 = sin((2 * PI * 1.0f * xNormalized) + (2 * PI * mainWavePhase)) * baseAmplitude * 0.6f
        val wave2 = sin((2 * PI * 2.0f * xNormalized) + (2 * PI * secondaryWavePhase)) * baseAmplitude * 0.25f
        val breathing = sin(2 * PI * breathingPhase) * (baseAmplitude * 0.15f)
        return baseHeight + wave1.toFloat() + wave2.toFloat() + breathing.toFloat()
    }

    fun getBoatPhysics(
        boatXNormalized: Float,
        baseHeight: Float,
        baseAmplitude: Float,
        viewWidth: Float
    ): Pair<Float, Float> {
        val yCenter = calculateWaterLevel(boatXNormalized, baseHeight, baseAmplitude)
        
        val deltaNorm = 0.02f
        val xFront = boatXNormalized + deltaNorm
        val yFront = calculateWaterLevel(xFront, baseHeight, baseAmplitude)
        
        val deltaY = yFront - yCenter
        val deltaX = deltaNorm * viewWidth
        
        val angleRad = atan2(deltaY, deltaX)
        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        val safeRotation = angleDeg.coerceIn(-35f, 35f)
        
        // Return Y position dan Rotasi
        return Pair(yCenter, safeRotation)
    }

    // Helper Creates (Sama)
    private fun createRandomRain() = RainDrop(
        x = Random.nextFloat() * 1000f,
        y = Random.nextFloat() * -1000f,
        speed = 15f + Random.nextFloat() * 10f, 
        length = 30f + Random.nextFloat() * 20f
    )
    private fun createRandomBird() = Bird(
        x = Random.nextFloat() * -500f,
        y = 100f + Random.nextFloat() * 200f,
        speed = 0.5f + Random.nextFloat() * 0.5f,
        wingPhase = Random.nextFloat() * 10f
    )
    private fun createRandomStar() = Star(
        x = Random.nextFloat(),
        y = Random.nextFloat() * 0.5f,
        size = 1.5f + Random.nextFloat() * 3f,
        blinkSpeed = 0.005f + Random.nextFloat() * 0.01f,
        currentAlpha = Random.nextFloat()
    )
}
