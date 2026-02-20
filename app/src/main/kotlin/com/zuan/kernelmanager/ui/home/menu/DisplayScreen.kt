/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.home.menu

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

/**
 * Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan generateThemedBackgroundColor di AboutScreen
 * Hijau → #F8FBF2 (hijau sangat soft)
 * Ungu → #FFF7FC (ungu sangat soft) 
 * Pink → #FFF5F8 (pink sangat soft)
 */
@Composable
fun generateDisplayBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
        // Primary dikurangi intensitasnya 92%, lalu di-blend dengan dark base
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
        // Formula: 97% White + 3% Primary = Warna soft yang tetap berhue sama
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        // Buat warna soft: hampir putih tapi ada sedikit nuansa primary
        // Contoh: Hijau #D4E8CF → #F8FBF2 (248, 251, 242) sangat soft
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),   // 97% white + 3% primary
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        // Card color: Putih bersih untuk card (Material 3 standard)
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun DisplayScreen(
    navController: NavController,
    viewModel: DisplayViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // FIX: Ambil theme mode dari settings
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    val effectivePrimary = when {
        isCustomColor -> Color(customPrimary)
        isDynamic -> MaterialTheme.colorScheme.primary
        else -> themeColorName.primary
    }

    // FIX: Hitung dark theme berdasarkan setting aplikasi, bukan sistem HP
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    // SESUAI ABOUTSCREEN: Generate tinted background untuk semua kasus SYSTEM
    val (themeBgColor, themeCardColor) = if (bgType == BgType.SYSTEM) {
        generateDisplayBackgroundColors(isDarkTheme, effectivePrimary)
    } else {
        Pair(Color.Transparent, Color(0xFFFFFFFF))
    }
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
    }
    
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else themeBgColor
    
    val textColor = when {
        isGlassActive || isCustomBg -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val subTextColor = when {
        isGlassActive || isCustomBg -> Color.White.copy(0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val dividerColor = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val hazeState = rememberHazeState()

    LaunchedEffect(Unit) {
        viewModel.loadDisplayData(context)
    }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) {
            viewModel.onTabSelected(pagerState.currentPage)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        if (isCustomBg && bgUriString != null) {
            val blurModifier = if (isBgBlur && blurStrength > 0f) {
                Modifier.blur(blurStrength.dp)
            } else if (isBgBlur) {
                Modifier.blur(20.dp)
            } else {
                Modifier
            }
            
            val saturationMatrix = ColorMatrix().apply { setToSaturation(bgSaturation) }
            val colorFilter = ColorFilter.colorMatrix(saturationMatrix)
            val uri = Uri.parse(bgUriString)

            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f)) {
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
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgContrast)))
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeBgColor)
                    .hazeSource(state = hazeState, zIndex = 0f)
            )
        }
        
        WeatherEffectOverlay(
            effect = weatherEffect, 
            intensity = weatherIntensity, 
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassDisplayHeader(
                    navController = navController,
                    isCustomBg = isCustomBg,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                GlassTabRow(
                    selectedTab = selectedTab,
                    onTabSelected = { index -> 
                        viewModel.onTabSelected(index)
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = effectivePrimary)
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top
                    ) { page ->
                        when (page) {
                            0 -> DashboardTabGlass(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, dividerColor, effectivePrimary)
                            1 -> ResolutionDpiTabGlass(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, dividerColor, effectivePrimary)
                            2 -> RefreshColorTabGlass(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, effectivePrimary)
                            3 -> AppearanceTabGlass(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, effectivePrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassDisplayHeader(
    navController: NavController,
    isCustomBg: Boolean,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color
) {
    val shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = 0.3f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
    } else {
        Modifier
    }
    
    val bgColor = if (isCustomBg) Color.Transparent else MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(glassModifier)
            .background(bgColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Rounded.ArrowBack, 
                    contentDescription = stringResource(R.string.display_back), 
                    tint = textColor, 
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = stringResource(R.string.display_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun GlassTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val tabs = listOf(
        stringResource(R.string.display_tab_info),
        stringResource(R.string.display_tab_res_dpi),
        stringResource(R.string.display_tab_color),
        stringResource(R.string.display_tab_extra)
    )
    val shape = RoundedCornerShape(32.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            .padding(4.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .padding(4.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        tabs.forEachIndexed { index, title ->
            GlassDisplayTabItem(
                selected = selectedTab == index,
                text = title,
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f),
                isGlassActive = isGlassActive,
                textColor = textColor,
                subTextColor = subTextColor,
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
fun GlassDisplayTabItem(
    selected: Boolean, 
    text: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    isGlassActive: Boolean,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val bgColor by animateColorAsState(
        if (selected) {
            if (isGlassActive) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primaryContainer
        } else Color.Transparent, label = ""
    )
    val txtColor by animateColorAsState(
        if (selected) Color.White else if (isGlassActive) textColor else subTextColor, label = ""
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
            color = txtColor, 
            maxLines = 1
        )
    }
}

@Composable
fun DashboardTabGlass(
    viewModel: DisplayViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    dividerColor: Color,
    primaryColor: Color
) {
    val info by viewModel.dashboardInfo.collectAsStateWithLifecycle()

    if (info == null) return

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            DisplaySectionTitleGlass(stringResource(R.string.display_device_info), primaryColor)
            GlassDisplayCard(isGlassActive, hazeState, cardColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassDisplayDetailRow(stringResource(R.string.display_resolution), info!!.resolution, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_density_dpi), info!!.density, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_exact_dpi), info!!.xdpiYdpi, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_technology), info!!.technology, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_diagonal_size), info!!.diagonalSize, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_physical_size), info!!.physicalSize, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_refresh_rate), info!!.refreshRate, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_orientation), info!!.orientation, textColor, subTextColor)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_graphics_hdr), primaryColor)
            GlassDisplayCard(isGlassActive, hazeState, cardColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GlassDisplayDetailRow(stringResource(R.string.display_gpu_renderer), info!!.gpuRenderer, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_opengl_version), info!!.openGlVersion, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(stringResource(R.string.display_hdr_support), info!!.hdrCapabilities, textColor, subTextColor)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = dividerColor)
                    GlassDisplayDetailRow(
                        stringResource(R.string.display_wide_color_gamut), 
                        if(info!!.isWideColorGamut) stringResource(R.string.display_supported) else stringResource(R.string.display_not_supported), 
                        textColor, 
                        subTextColor
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun GlassDisplayCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        content()
    }
}

@Composable
fun GlassDisplayDetailRow(label: String, value: String, textColor: Color, subTextColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = subTextColor)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = textColor)
    }
}

@Composable
fun DisplaySectionTitleGlass(title: String, primaryColor: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = primaryColor,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun ResolutionDpiTabGlass(
    viewModel: DisplayViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    dividerColor: Color,
    primaryColor: Color
) {
    val currentResolution by viewModel.currentResolution.collectAsStateWithLifecycle()
    val currentDpi by viewModel.currentDpi.collectAsStateWithLifecycle()
    
    var showManualResDialog by remember { mutableStateOf(false) }
    var showManualDpiDialog by remember { mutableStateOf(false) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            DisplaySectionTitleGlass(stringResource(R.string.display_resolution_section), primaryColor)
            GlassSettingCard(
                title = stringResource(R.string.display_current_resolution, currentResolution),
                subtitle = stringResource(R.string.display_tap_presets),
                icon = Icons.Rounded.AspectRatio,
                onClick = { },
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                primaryColor = primaryColor
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showManualResDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGlassActive) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.display_manual_input))
                }
                OutlinedButton(
                    onClick = { viewModel.resetResolution() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isGlassActive) BorderStroke(1.dp, Color.White.copy(0.5f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(18.dp), tint = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.display_reset), color = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.display_presets), style = MaterialTheme.typography.labelMedium, color = primaryColor)
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.availableResolutions.take(6).forEach { res ->
                    FilterChip(
                        selected = res == currentResolution,
                        onClick = { viewModel.setResolution(res.width, res.height) },
                        label = { Text("${res.width}x${res.height}", color = textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_density_section), primaryColor)
            GlassSettingCard(
                title = stringResource(R.string.display_current_dpi, currentDpi),
                subtitle = stringResource(R.string.display_density_desc),
                icon = Icons.Rounded.ZoomIn,
                onClick = { },
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardColor = cardColor,
                textColor = textColor,
                subTextColor = subTextColor,
                primaryColor = primaryColor
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showManualDpiDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGlassActive) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.display_manual_input))
                }
                OutlinedButton(
                    onClick = { viewModel.resetDpi() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isGlassActive) BorderStroke(1.dp, Color.White.copy(0.5f)) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Rounded.RestartAlt, null, modifier = Modifier.size(18.dp), tint = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.display_reset), color = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    if (showManualResDialog) {
        ManualResolutionDialog(
            onDismiss = { showManualResDialog = false },
            onApply = { w, h -> viewModel.setResolution(w, h); showManualResDialog = false }
        )
    }
    if (showManualDpiDialog) {
        ManualDpiDialog(
            currentDpi = currentDpi,
            onDismiss = { showManualDpiDialog = false },
            onApply = { dpi -> viewModel.setDpi(dpi); showManualDpiDialog = false }
        )
    }
}

@Composable
fun GlassSettingCard(
    title: String, 
    subtitle: String, 
    icon: ImageVector, 
    onClick: () -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val shape = RoundedCornerShape(20.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subTextColor)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = subTextColor)
        }
    }
}

@Composable
fun RefreshColorTabGlass(
    viewModel: DisplayViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val currentRefreshRate by viewModel.currentRefreshRate.collectAsStateWithLifecycle()
    val screenTimeout by viewModel.screenTimeout.collectAsStateWithLifecycle()
    val saturation by viewModel.saturation.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            DisplaySectionTitleGlass(stringResource(R.string.display_saturation_title), primaryColor)
            GlassDisplayCard(isGlassActive, hazeState, cardColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Palette, null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.display_saturation_level), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = textColor))
                        Spacer(modifier = Modifier.weight(1f))
                        Text(String.format("%.1f", saturation), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = primaryColor))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = saturation,
                        onValueChange = { viewModel.setSaturation(it) },
                        valueRange = 0.0f..2.0f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = if (isGlassActive) Color.White.copy(0.3f) else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.display_bw), style = MaterialTheme.typography.labelSmall, color = subTextColor)
                        Text(stringResource(R.string.display_default), style = MaterialTheme.typography.labelSmall, color = subTextColor)
                        Text(stringResource(R.string.display_vivid), style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_refresh_rate), primaryColor)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.availableRefreshRates.forEach { rate ->
                    FilterChip(
                        selected = rate == currentRefreshRate,
                        onClick = { viewModel.setRefreshRate(rate) },
                        label = { Text("${rate}Hz", color = if (rate == currentRefreshRate) Color.White else textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_screen_timeout), primaryColor)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.timeoutOptions.forEach { timeout ->
                    val label = when {
                        timeout < 60 -> "${timeout}s"
                        else -> "${timeout/60}m"
                    }
                    FilterChip(
                        selected = timeout == screenTimeout,
                        onClick = { viewModel.setScreenTimeout(timeout) },
                        label = { Text(label, color = if (timeout == screenTimeout) Color.White else textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun AppearanceTabGlass(
    viewModel: DisplayViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val animationScale by viewModel.animationScale.collectAsStateWithLifecycle()
    val nightMode by viewModel.nightMode.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            DisplaySectionTitleGlass(stringResource(R.string.display_font_scale), primaryColor)
            GlassDisplayCard(isGlassActive, hazeState, cardColor) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.setFontScale(it) },
                        valueRange = 0.8f..1.3f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = if (isGlassActive) Color.White.copy(0.3f) else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )
                    Text(stringResource(R.string.display_current_scale, (fontScale * 100).toInt()), style = MaterialTheme.typography.bodySmall, color = subTextColor)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_animation_speed), primaryColor)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.animationScaleOptions.forEach { scale ->
                    FilterChip(
                        selected = scale == animationScale,
                        onClick = { viewModel.setAnimationScale(scale) },
                        label = { Text(if(scale == 0f) stringResource(R.string.display_off) else "${scale}x", color = if (scale == animationScale) Color.White else textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            DisplaySectionTitleGlass(stringResource(R.string.display_night_light), primaryColor)
            GlassSwitchControl(
                title = stringResource(R.string.display_night_mode), 
                checked = nightMode, 
                onCheckedChange = { viewModel.setNightMode(it) }, 
                icon = Icons.Rounded.NightsStay,
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardColor = cardColor,
                textColor = textColor,
                primaryColor = primaryColor
            )
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun GlassSwitchControl(
    title: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit, 
    icon: ImageVector,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    primaryColor: Color
) {
    val shape = RoundedCornerShape(20.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            .clickable { onCheckedChange(!checked) }
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onCheckedChange(!checked) }
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = textColor)
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = if (isGlassActive) Color.White else Color.Gray,
                    uncheckedTrackColor = if (isGlassActive) Color.White.copy(0.3f) else Color.Gray.copy(0.3f)
                )
            )
        }
    }
}

@Composable
fun ManualResolutionDialog(onDismiss: () -> Unit, onApply: (Int, Int) -> Unit) {
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.display_custom_resolution)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = width, onValueChange = { if(it.all { c -> c.isDigit() }) width = it },
                    label = { Text(stringResource(R.string.display_width_px)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = height, onValueChange = { if(it.all { c -> c.isDigit() }) height = it },
                    label = { Text(stringResource(R.string.display_height_px)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if(width.isNotEmpty() && height.isNotEmpty()) 
                        onApply(width.toInt(), height.toInt()) 
                }
            ) { Text(stringResource(R.string.btn_apply)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } }
    )
}

@Composable
fun ManualDpiDialog(currentDpi: Int, onDismiss: () -> Unit, onApply: (Int) -> Unit) {
    var dpi by remember { mutableStateOf(currentDpi.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.display_custom_dpi)) },
        text = {
            OutlinedTextField(
                value = dpi, onValueChange = { if(it.all { c -> c.isDigit() }) dpi = it },
                label = { Text(stringResource(R.string.display_density_dpi_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if(dpi.isNotEmpty()) onApply(dpi.toInt()) }) { Text(stringResource(R.string.btn_apply)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel)) } }
    )
}
