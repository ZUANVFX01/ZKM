/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi // Penting untuk API baru
import androidx.compose.material3.LinearWavyProgressIndicator // Komponen Expressive
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Zuan3DCustomSplashScreen(onSplashFinished: () -> Unit) {
    // --- Konfigurasi Waktu ---
    val splashHoldTime = 1500L // Sedikit diperlama untuk menikmati animasinya

    // --- State Animasi (Physics Based) ---
    // 1. Logo Scale: Efek "Pop" memantul
    val logoScale = remember { Animatable(0.5f) }
    
    // 2. Text Offset: Muncul dari Kanan ke Kiri (Slide In)
    val textTranslationX = remember { Animatable(100f) }
    val textAlpha = remember { Animatable(0f) }

    // 3. Wavy Loader: Muncul terakhir
    val loaderAlpha = remember { Animatable(0f) }

    // --- Definisi Fisika (Sesuai Dokumen M3 Expressive) ---
    // Token: Expressive Spatial (Playful Bounce)
    // Damping: 0.38f, Stiffness: 700f
    val expressiveSpringSpec = spring<Float>(
        dampingRatio = 0.38f,
        stiffness = 700f
    )

    val gentleSpringSpec = spring<Float>(
        dampingRatio = 0.6f, // Sedikit lebih tenang untuk teks
        stiffness = 500f
    )

    // --- Eksekusi Orkestrasi Animasi ---
    LaunchedEffect(Unit) {
        // Langkah 1: Logo "Pop" dengan pantulan
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = expressiveSpringSpec
            )
        }

        // Langkah 2: Teks masuk dari kanan (sedikit delay agar staggered/bertahap)
        delay(150) 
        launch {
            textTranslationX.animateTo(
                targetValue = 0f,
                animationSpec = gentleSpringSpec
            )
        }
        launch {
            textAlpha.animateTo(1f, tween(400))
        }

        // Langkah 3: Loader muncul perlahan
        delay(300)
        launch {
            loaderAlpha.animateTo(1f, tween(500))
        }

        // Selesai
        delay(splashHoldTime)
        onSplashFinished()
    }

    // --- UI Layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Bisa diganti MaterialTheme.colorScheme.surface
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Menggunakan ROW untuk Logo & Teks
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                // --- BAGIAN KIRI: ICON (Animated Scale) ---
                Image(
                    painter = painterResource(id = R.drawable.ic_app),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(72.dp) // Sedikit diperbesar
                        .scale(logoScale.value) // Animasi Scale diterapkan di sini
                )

                Spacer(modifier = Modifier.width(24.dp))

                // --- BAGIAN KANAN: KOLOM TEKS (Animated Slide & Alpha) ---
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .graphicsLayer {
                            translationX = textTranslationX.value // Animasi geser
                            alpha = textAlpha.value // Animasi transparan
                        }
                ) {
                    // Judul Utama
                    Text(
                        text = "Zuan Manager",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold, // Lebih tebal (Emphasized)
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = Color(0xFF37474F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sub-judul
                    Text(
                        text = "Elegant System Root Tool",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Color(0xFF78909C)
                    )
                }
            }
        }

        // --- EXPRESSIVE COMPONENT: Wavy Progress Indicator ---
        // Posisikan di bagian bawah layar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp) // Jarak dari bawah
                .graphicsLayer { alpha = loaderAlpha.value } // Animasi muncul
        ) {
            // Ini adalah komponen khas M3 Expressive
            // Memberikan kesan "hidup" dibanding progress bar lurus biasa
            LinearWavyProgressIndicator(
                modifier = Modifier.width(150.dp), // Jangan terlalu lebar agar estetik
                color = Color(0xFF37474F),
                trackColor = Color(0xFFECEFF1),
                amplitude = 0.6f, // Tinggi gelombang (0.0 - 1.0)
                wavelength = 20.dp // Jarak antar puncak gelombang
            )
        }
    }
}
