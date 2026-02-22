/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.settings.WeatherEffect
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

val ExpressiveCornerRadius = 32.dp
val IconContainerRadius = 16.dp
val CardPadding = 16.dp

@Composable
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun AboutScreen(
    navController: NavController,
    hazeState: HazeState,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // [FIX] Ambil versi aplikasi langsung dari PackageManager
    val appVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val isCustomBg = bgType == BgType.GALLERY
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()

    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColorName.primary
        }
    }

    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val (tintedBackground, themeCardColor) = generateThemedBackgroundColor(finalPrimary, useDarkTheme)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )

    val isCollapsed by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.7f }
    }

    val accentColor = finalPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val isGlassActive = isHazeEnabled && isCustomBg
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else tintedBackground
    
    val targetCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
        else -> themeCardColor
    }

    val appBarHazeStyle = HazeStyle(
    backgroundColor = tintedBackground.copy(alpha = 0.5f),
    blurRadius = 24.dp,
    noiseFactor = 0.1f,
    tints = listOf(HazeTint(tintedBackground.copy(alpha = 0.4f)))
)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
        ) {
            // BACKGROUND LAYER (zIndex 0)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 0f)
            ) {
                if (isCustomBg && bgUriString != null) {
                    val blurModifier = if (isBgBlur && blurStrength > 0f) {
                        Modifier.blur(blurStrength.dp)
                    } else if (isBgBlur) {
                        Modifier.blur(20.dp)
                    } else {
                        Modifier
                    }
                    
                    val saturationMatrix = ColorMatrix().apply {
                        setToSaturation(bgSaturation)
                    }
                    val colorFilter = ColorFilter.colorMatrix(saturationMatrix)
                    
                    val uri = android.net.Uri.parse(bgUriString)

                    if (isVideo) {
                        VideoWallpaperPlayer(
                            uri = uri,
                            modifier = Modifier.fillMaxSize().then(blurModifier)
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(uri).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().then(blurModifier),
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter
                        )
                    }

                    if (bgContrast > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = bgContrast))
                        )
                    }
                }

                WeatherEffectOverlay(
                    effect = weatherEffect,
                    intensity = weatherIntensity,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // CONTENT LAYER
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 1f)
                    .verticalScroll(scrollState)
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(200.dp))

                // DEVELOPER SECTION
                ThemedSectionTitle(stringResource(R.string.about_section_developer), accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = "https://avatars.githubusercontent.com/u/207377902?v=4",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zuanvfx01",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = stringResource(R.string.about_maintainer_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { openUrl(context, "https://zuanvfx01.github.io/ZKM/") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Language,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.about_link_website),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "zuanvfx01.github.io/ZKM",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // COLLABORATOR SECTION
                ThemedSectionTitle(stringResource(R.string.about_section_collaborator), accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Person,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rve",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Collaborator",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // APPLICATION SECTION - Changelog & Open Source
                ThemedSectionTitle("Application", accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Changelog Card
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { navController.navigate(NavigationRoute.Changelogs.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.History,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Changelog",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Version history and updates",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Open Source Card dengan Avatar Stack
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { navController.navigate(NavigationRoute.OpenSource.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Code,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Open Source",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Contributors and licenses",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        // Avatar Stack
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-8).dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            listOf(
                                "https://avatars.githubusercontent.com/Rve27",
                                "https://avatars.githubusercontent.com/libxzr",
                                "https://avatars.githubusercontent.com/Kyant0"
                            ).forEach { avatarUrl ->
                                Surface(
                                    shape = CircleShape,
                                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                            }
                            
                            // +5 indicator
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+5",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // APP INFO SECTION
                ThemedSectionTitle(stringResource(R.string.about_section_app_info), accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { navController.navigate(NavigationRoute.General.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.GridView,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sect_general),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "App settings and configuration",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { navController.navigate(NavigationRoute.Settings.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Palette,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sect_appearance),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Theme, colors and personalization",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // COMMUNITY SECTION
                ThemedSectionTitle(stringResource(R.string.about_section_community), accentColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { openUrl(context, "https://t.me/ZuanvfxProject3") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Campaign,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.about_link_channel),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "@ZuanvfxProject3",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { openUrl(context, "https://t.me/zuanvfx01enterprise/2") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Group,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.about_link_discussion),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "@zuanvfx01enterprise",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemedExpressiveCard(
                    containerColor = targetCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    onClick = { openUrl(context, "https://t.me/Zuanvfx01") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(CardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedIconContainer(
                            icon = Icons.Outlined.Translate,
                            primaryColor = accentColor
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.about_link_translate),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                ),
                                color = onSurfaceColor
                            )
                            Text(
                                text = "@Zuanvfx01",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceVariantColor
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = onSurfaceVariantColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(120.dp))
            }

            // APPBAR
            LargeTopAppBar(
                title = {
                    AppBarContent(
                        isCollapsed = isCollapsed,
                        appName = stringResource(R.string.app_name),
                        version = appVersion, // [FIX] Sekarang menggunakan appVersion dari PackageManager
                        textColor = onSurfaceColor,
                        subTextColor = onSurfaceVariantColor,
                        accentColor = accentColor
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    actionIconContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .hazeEffect(
                        state = hazeState,
                        style = appBarHazeStyle
                    )
            )
        }
    }
}

@Composable
private fun AppBarContent(
    isCollapsed: Boolean,
    appName: String,
    version: String,
    textColor: Color,
    subTextColor: Color,
    accentColor: Color
) {
    AnimatedContent(
        targetState = isCollapsed,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInVertically { it / 2 })
                .togetherWith(
                    fadeOut(animationSpec = tween(200)) +
                            slideOutVertically { -it / 2 }
                )
        },
        label = "AppBarTransition"
    ) { collapsed ->
        if (collapsed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = accentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textColor
                )
            }
        } else {
            Column {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = subTextColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Version $version",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun ThemedSectionTitle(text: String, accentColor: Color) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontSize = 13.sp
        ),
        color = accentColor.copy(alpha = 0.9f)
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ThemedExpressiveCard(
    containerColor: Color,
    isGlassActive: Boolean = false,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(ExpressiveCornerRadius)
    
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    } else {
        Modifier.fillMaxWidth()
    }
    
    val glassModifier = if (isGlassActive && hazeState != null) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(glassModifier),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else containerColor,
        tonalElevation = if (onClick != null && !isGlassActive) 1.dp else 0.dp
    ) {
        content()
    }
}

@Composable
fun ThemedIconContainer(
    icon: ImageVector,
    primaryColor: Color
) {
    val softBackground = primaryColor.copy(alpha = 0.12f)
    
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(IconContainerRadius))
            .background(softBackground),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
