/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // <--- PENTING: Import ini
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.R // <--- Pastikan import R sesuai package kamu
import com.zuan.kernelmanager.ui.settings.SettingsPreference
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

// --- COLORS ---
val CropvestDark = Color(0xFF0F172A)
val CropvestGray = Color(0xFF64748B)

val ThemeBlue = Color(0xFF0EA5E9)
val ThemeRed = Color(0xFFEF4444)
val ThemeGreen = Color(0xFF10B981)

// --- DATA KONTEN (UPDATED: Menggunakan @StringRes Int) ---
sealed class IntroPage(
    @StringRes val titleRes: Int,     // Ubah String jadi Int (Resource ID)
    @StringRes val descriptionRes: Int, // Ubah String jadi Int
    val icon: ImageVector,
    val themeColor: Color,
    val isRiskPage: Boolean = false
) {
    // 1. PERKENALAN
    object Intro : IntroPage(
        titleRes = R.string.intro_p1_title,
        descriptionRes = R.string.intro_p1_desc,
        icon = Icons.Rounded.Smartphone,
        themeColor = ThemeBlue
    )
    // 2. RISIKO
    object Risk : IntroPage(
        titleRes = R.string.intro_p2_title,
        descriptionRes = R.string.intro_p2_desc,
        icon = Icons.Rounded.WarningAmber,
        themeColor = ThemeRed,
        isRiskPage = true
    )
    // 3. PRIVASI & PERSETUJUAN
    object Privacy : IntroPage(
        titleRes = R.string.intro_p3_title,
        descriptionRes = R.string.intro_p3_desc,
        icon = Icons.Rounded.Shield,
        themeColor = ThemeGreen
    )
}

@Composable
fun IntroScreen(
    onIntroCompleted: () -> Unit,
    onDeclined: () -> Unit = { exitProcess(0) }
) {
    val context = LocalContext.current
    val prefs = remember { SettingsPreference.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    val pages = listOf(IntroPage.Intro, IntroPage.Risk, IntroPage.Privacy)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold(
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. BACKGROUND CAIRAN
            LiquidFluidBackground(currentPage = pagerState.currentPage, pages = pages)
            
            // 2. KONTEN UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f)) 
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Kecil
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(CropvestDark, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.intro_app_label), // UPDATED
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CropvestDark,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // KONTEN PAGER
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = true
                ) { index ->
                    CropvestContent(page = pages[index])
                }

                Spacer(modifier = Modifier.height(24.dp))

                // BAGIAN BAWAH
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indikator
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val activeColor = pages[pagerState.currentPage].themeColor
                            
                            val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "width")
                            val color by animateColorAsState(if (isSelected) activeColor else CropvestGray.copy(alpha = 0.3f), label = "color")
                            
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // TOMBOL
                    if (pagerState.currentPage < pages.size - 1) {
                        Button(
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CropvestDark),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(stringResource(R.string.intro_btn_next), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // UPDATED
                        }
                    } else {
                        // Halaman Terakhir
                        Button(
                            onClick = {
                                prefs.setHasCompletedIntro(true)
                                prefs.setAcceptedTerms(true)
                                onIntroCompleted()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CropvestDark),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(stringResource(R.string.intro_btn_agree), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // UPDATED
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { onDeclined() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ThemeRed),
                            shape = RoundedCornerShape(50),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(stringResource(R.string.intro_btn_decline), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) // UPDATED
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.intro_footer_legal), // UPDATED
                            style = MaterialTheme.typography.bodySmall,
                            color = CropvestGray.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center, fontSize = 11.sp, lineHeight = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- BACKGROUND CAIRAN (Tidak ada Text disini, aman) ---
@Composable
fun LiquidFluidBackground(currentPage: Int, pages: List<IntroPage>) {
    val targetColor = pages[currentPage].themeColor
    val animatedThemeColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "fluidColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "liquidMovement")
    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse), label = "o1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse), label = "o2"
    )
    
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize().blur(150.dp).scale(1.2f)
        ) {
            val width = size.width
            val height = size.height
            
            drawCircle(
                color = animatedThemeColor.copy(alpha = 0.4f),
                radius = width * 0.6f,
                center = Offset(
                    x = width * 0.2f + (offset1 * width * 0.2f),
                    y = height * 0.3f + (cos(offset1 * 3) * height * 0.1f)
                )
            )
            drawCircle(
                color = animatedThemeColor.copy(alpha = 0.35f),
                radius = width * 0.7f,
                center = Offset(
                    x = width * 0.8f - (offset2 * width * 0.3f),
                    y = height * 0.7f - (sin(offset2 * 3) * height * 0.1f)
                )
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = width * 0.4f,
                center = Offset(width * 0.5f, height * 0.5f)
            )
        }
        
        Box(
            modifier = Modifier.fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.2f)),
                        radius = 1000f
                    )
                )
        )
    }
}

@Composable
fun CropvestContent(page: IntroPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // JUDUL (UPDATED: stringResource)
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = page.themeColor, 
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // DESKRIPSI (UPDATED: stringResource)
        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = CropvestGray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(240.dp, 120.dp)
                    .offset(y = 50.dp)
                    .graphicsLayer { rotationX = 60f }
                    .background(
                        color = page.themeColor.copy(alpha = 0.1f), 
                        shape = RoundedCornerShape(30.dp)
                    )
            )
            
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(160.dp).offset(y = (-20).dp),
                tint = page.themeColor
            )
            
            if (page.isRiskPage) {
                Icon(
                    Icons.Rounded.PriorityHigh, null,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp).size(40.dp),
                    tint = ThemeRed
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}
