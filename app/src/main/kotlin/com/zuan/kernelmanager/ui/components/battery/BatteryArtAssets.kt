/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.components.battery

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform

object BatteryArtAssets {

    // Gambar Kapal
    fun DrawScope.drawBoat(
        x: Float,
        y: Float,
        rotation: Float,
        color: Color,
        scale: Float = 1.0f
    ) {
        withTransform({
            translate(x, y)
            rotate(rotation)
            scale(scale, scale)
        }) {
            val boatPath = Path().apply {
                moveTo(-25f, 0f)
                quadraticBezierTo(0f, 15f, 25f, 0f)
                close()
                moveTo(0f, 0f)
                lineTo(0f, -35f)
                moveTo(0f, -30f)
                lineTo(-15f, -10f)
                lineTo(0f, -5f)
                moveTo(0f, -32f)
                lineTo(20f, -10f)
                lineTo(0f, -5f)
            }
            drawPath(boatPath, color.copy(alpha = 0.9f))
            drawPath(boatPath, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }

    // [NEW] Gambar Refleksi/Bayangan Kapal di Air
    fun DrawScope.drawBoatReflection(
        x: Float,
        y: Float,
        rotation: Float,
        color: Color,
        scale: Float = 1.0f
    ) {
        withTransform({
            translate(x, y + 10f) // Geser ke bawah sedikit dari kapal asli
            rotate(rotation) // Ikuti kemiringan kapal
            scale(scale, -scale * 0.5f) // Flip vertikal (gepeng) & mirror
        }) {
            // Gambar bentuk simpel untuk bayangan
            val shadowPath = Path().apply {
                moveTo(-25f, 0f)
                quadraticBezierTo(0f, 15f, 25f, 0f)
                close()
            }
            drawPath(shadowPath, color.copy(alpha = 0.2f)) // Transparan
        }
    }

    // Gambar Burung
    fun DrawScope.drawBird(
        x: Float,
        y: Float,
        wingSpan: Float,
        color: Color,
        flapPhase: Float
    ) {
        val wingY = kotlin.math.sin(flapPhase).toFloat() * 2f
        val path = Path().apply {
            moveTo(x - wingSpan, y - wingY)
            quadraticBezierTo(x, y + 2f, x + wingSpan, y - wingY)
        }
        drawPath(path, color, style = Stroke(width = 3f, cap = StrokeCap.Round))
    }

    // Gambar Awan
    fun DrawScope.drawCloud(
        centerX: Float,
        centerY: Float,
        size: Float,
        color: Color
    ) {
        drawCircle(color, radius = size * 0.6f, center = Offset(centerX, centerY))
        drawCircle(color, radius = size * 0.4f, center = Offset(centerX - size * 0.5f, centerY + size * 0.1f))
        drawCircle(color, radius = size * 0.5f, center = Offset(centerX + size * 0.5f, centerY + size * 0.15f))
    }

    // Gambar Petir
    fun DrawScope.drawLightningBolt(
        startX: Float,
        startY: Float,
        height: Float,
        color: Color,
        alpha: Float
    ) {
        if (alpha <= 0.05f) return
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(startX - 15f, startY + height * 0.3f)
            lineTo(startX + 10f, startY + height * 0.35f)
            lineTo(startX - 20f, startY + height * 0.6f)
            lineTo(startX + 5f, startY + height * 0.65f)
            lineTo(startX - 10f, startY + height)
        }
        // Glow effect
        drawPath(path, color.copy(alpha = alpha * 0.4f), style = Stroke(width = 15f, cap = StrokeCap.Round))
        // Core effect
        drawPath(path, Color.White.copy(alpha = alpha), style = Stroke(width = 4f, cap = StrokeCap.Round))
    }

    fun DrawScope.drawStar(x: Float, y: Float, alpha: Float) {
        if (alpha <= 0f) return
        drawCircle(color = Color.White.copy(alpha = alpha), radius = 3f, center = Offset(x, y))
    }
}
