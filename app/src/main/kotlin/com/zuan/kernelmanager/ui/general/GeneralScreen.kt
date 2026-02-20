/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.general

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.NavLabelState
import com.zuan.kernelmanager.ui.settings.NavStyle
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

/**
 * Generate SOFT TINTED background berdasarkan primary color tema (SAMA PERSIS AboutScreen)
 */
@Composable
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
        val darkTintedBg = primaryColor.copy(
            red = primaryColor.red * 0.08f,
            green = primaryColor.green * 0.08f,
            blue = primaryColor.blue * 0.08f,
            alpha = 1f
        ).compositeOver(Color(0xFF0A0A0A))
        
        val cardColor = primaryColor.copy(
            red = primaryColor.red * 0.15f,
            green = primaryColor.green * 0.15f,
            blue = primaryColor.blue * 0.15f,
            alpha = 1f
        ).compositeOver(Color(0xFF141414))
        
        Pair(darkTintedBg, cardColor)
    } else {
        // Light Mode: Background putih dengan "tint" dari primary
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun GeneralScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val navStyle by viewModel.navStyle.collectAsState()
    val navLabelState by viewModel.navLabelState.collectAsState()
    
    // Theme state collecting (sama seperti AboutScreen)
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamic by viewModel.isDynamicColor.collectAsState()
    val themeColorName by viewModel.currentThemeColor.collectAsState()
    val isCustomColor by viewModel.isCustomColor.collectAsState()
    val customPrimary by viewModel.customPrimaryColor.collectAsState()

    // Gunakan themeMode dari aplikasi (sama persis AboutScreen)
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // Determine primary color (sama logic dengan AboutScreen)
    val effectivePrimary = when {
        isCustomColor -> Color(customPrimary)
        isDynamic -> MaterialTheme.colorScheme.primary
        else -> themeColorName.primary
    }

    // Generate TINTED background berdasarkan primary color
    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(effectivePrimary, useDarkTheme)
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // Warna text yang konsisten dengan theme
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "General",
                        color = onSurfaceColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = onSurfaceColor
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                )
            )
        },
        containerColor = tintedBackground // <-- SAMA PERSIS AboutScreen
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            
            // Section Title dengan accent color (sama seperti AboutScreen)
            Text(
                text = "Navigation Mode",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    fontSize = 13.sp
                ),
                color = effectivePrimary.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )

            // --- PILIHAN NAVIGASI (GRID 2 KOLOM) ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // BARIS 1: Capsule & Bottom Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option 1: Liquid Capsule
                    NavigationPreviewCard(
                        modifier = Modifier.weight(1f),
                        title = "Capsule iOS",
                        imageRes = R.drawable.navigation_capsule,
                        isSelected = navStyle == NavStyle.LIQUID_CAPSULE,
                        cardColor = cardContainerColor,
                        primaryColor = effectivePrimary,
                        onClick = { viewModel.setNavStyle(NavStyle.LIQUID_CAPSULE) }
                    )

                    // Option 2: Classic Bottom Bar
                    NavigationPreviewCard(
                        modifier = Modifier.weight(1f),
                        title = "Bottom Bar",
                        imageRes = R.drawable.navigation_bottom_bar,
                        isSelected = navStyle == NavStyle.CLASSIC_MATERIAL,
                        cardColor = cardContainerColor,
                        primaryColor = effectivePrimary,
                        onClick = { viewModel.setNavStyle(NavStyle.CLASSIC_MATERIAL) }
                    )
                }

                // BARIS 2: Hybrid Tabs (Pilihan Baru)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Option 3: Modern Tabs
                    NavigationPreviewCard(
                        modifier = Modifier.weight(1f), 
                        title = "Hybrid Tabs",
                        imageRes = R.drawable.navigation_hybird_tabs,
                        isSelected = navStyle == NavStyle.MODERN_TABS,
                        badge = "BETA",
                        cardColor = cardContainerColor,
                        primaryColor = effectivePrimary,
                        onClick = { viewModel.setNavStyle(NavStyle.MODERN_TABS) }
                    )
                    
                    // Spacer agar card tidak melebar sendirian
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- LABEL BEHAVIOR SETTINGS (Kondisional) ---
            if (navStyle != NavStyle.LIQUID_CAPSULE) {
                // Section Title dengan accent color
                Text(
                    text = "Label Behavior",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        fontSize = 13.sp
                    ),
                    color = effectivePrimary.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                    shape = RoundedCornerShape(28.dp) // <-- Sama seperti AboutScreen
                ) {
                    Column {
                        LabelOptionItem(
                            text = "Always Show", 
                            isSelected = navLabelState == NavLabelState.ALWAYS_SHOW,
                            primaryColor = effectivePrimary,
                            onClick = { viewModel.setNavLabelState(NavLabelState.ALWAYS_SHOW) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        LabelOptionItem(
                            text = "Only Selected", 
                            isSelected = navLabelState == NavLabelState.ONLY_SELECTED,
                            primaryColor = effectivePrimary,
                            onClick = { viewModel.setNavLabelState(NavLabelState.ONLY_SELECTED) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        LabelOptionItem(
                            text = "Always Hide", 
                            isSelected = navLabelState == NavLabelState.ALWAYS_HIDE,
                            primaryColor = effectivePrimary,
                            onClick = { viewModel.setNavLabelState(NavLabelState.ALWAYS_HIDE) }
                        )
                    }
                }
            } else {
                Text(
                    text = "Labels are hidden in Capsule Mode to maintain the minimal aesthetic.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariantColor
                )
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun NavigationPreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    isSelected: Boolean,
    cardColor: Color,
    primaryColor: Color,
    badge: String? = null,
    onClick: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .aspectRatio(0.6f) 
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(cardColor) // <-- Pakai cardColor dari parameter
                .clickable(onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, 
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier.fillMaxSize(), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(32.dp)
                            .background(primaryColor, CircleShape) // <-- Pakai primaryColor
                            .padding(6.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 17.sp // <-- Sama seperti AboutScreen
            ),
            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun LabelOptionItem(
    text: String, 
    isSelected: Boolean, 
    primaryColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp // <-- Sama seperti AboutScreen
            ), 
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check, 
                null, 
                tint = primaryColor
            )
        }
    }
}
