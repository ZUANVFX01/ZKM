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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
fun generateThermalBackgroundColors(
    isDark: Boolean, 
    primaryColor: Color
): Pair<Color, Color> {
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
fun ThermalDevicesScreen(
    navController: NavController,
    viewModel: ThermalDevicesViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Collect all settings states
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
    
    // Theme states
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // FIX: Ambil theme mode dari settings untuk menghitung dark theme yang benar
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
        generateThermalBackgroundColors(isDarkTheme, effectivePrimary)
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
    
    val activeZones by viewModel.filteredZones.collectAsStateWithLifecycle()
    val disabledZones by viewModel.disabledZones.collectAsStateWithLifecycle()
    val coolingDevices by viewModel.coolingDevices.collectAsStateWithLifecycle()
    val stats by viewModel.thermalStats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val isAutoRefresh by viewModel.isAutoRefresh.collectAsStateWithLifecycle()
    val thermalPolicy by viewModel.thermalPolicy.collectAsStateWithLifecycle()
    val showTripPoints by viewModel.showTripPoints.collectAsStateWithLifecycle()

    var showPolicyDialog by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val hazeState = rememberHazeState()

    LaunchedEffect(Unit) {
        viewModel.loadThermalData()
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
                GlassThermalHeader(
                    navController = navController,
                    stats = stats,
                    isAutoRefresh = isAutoRefresh,
                    onAutoRefreshToggle = { viewModel.toggleAutoRefresh(it) },
                    thermalPolicy = thermalPolicy,
                    onPolicyClick = { showPolicyDialog = true },
                    isCustomBg = isCustomBg,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                GlassThermalTabRow(
                    selectedPage = pagerState.currentPage,
                    onPageSelected = { index ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary
                )

                HorizontalDivider(color = dividerColor)

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
                            0 -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    GlassFilterChips(
                                        filters = viewModel.availableFilters,
                                        selectedFilter = selectedFilter,
                                        onFilterSelected = { viewModel.onFilterSelected(it) },
                                        isGlassActive = isGlassActive,
                                        primaryColor = effectivePrimary,
                                        textColor = textColor
                                    )
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(activeZones, key = { it.id }) { zone ->
                                            ThermalZoneCardGlass(
                                                zone = zone,
                                                tripPoints = showTripPoints[zone.id],
                                                onToggle = { viewModel.toggleThermalZone(zone) },
                                                onShowTrips = { viewModel.loadTripPoints(zone) },
                                                onHideTrips = { viewModel.clearTripPoints(zone.id) },
                                                isGlassActive = isGlassActive,
                                                hazeState = hazeState,
                                                cardColor = finalCardColor,
                                                textColor = textColor,
                                                subTextColor = subTextColor,
                                                primaryColor = effectivePrimary
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 72.dp), 
                                                color = dividerColor.copy(alpha = 0.5f)
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(100.dp)) }
                                    }
                                }
                            }
                            1 -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item { Spacer(modifier = Modifier.height(8.dp)) }
                                    items(coolingDevices, key = { it.id }) { device ->
                                        CoolingDeviceCardGlass(
                                            device = device,
                                            onStateChange = { viewModel.setCoolingState(device, it) },
                                            isGlassActive = isGlassActive,
                                            hazeState = hazeState,
                                            cardColor = finalCardColor,
                                            textColor = textColor,
                                            subTextColor = subTextColor,
                                            primaryColor = effectivePrimary
                                        )
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 72.dp), 
                                            color = dividerColor.copy(alpha = 0.5f)
                                        )
                                    }
                                    item { Spacer(modifier = Modifier.height(100.dp)) }
                                }
                            }
                            2 -> {
                                if (disabledZones.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(stringResource(R.string.thermal_no_disabled), color = subTextColor)
                                    }
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        item { Spacer(modifier = Modifier.height(8.dp)) }
                                        items(disabledZones, key = { "disabled_${it.id}" }) { zone ->
                                            ThermalZoneCardGlass(
                                                zone = zone,
                                                tripPoints = showTripPoints[zone.id],
                                                onToggle = { viewModel.toggleThermalZone(zone) },
                                                onShowTrips = { viewModel.loadTripPoints(zone) },
                                                onHideTrips = { viewModel.clearTripPoints(zone.id) },
                                                isGlassActive = isGlassActive,
                                                hazeState = hazeState,
                                                cardColor = finalCardColor,
                                                textColor = textColor,
                                                subTextColor = subTextColor,
                                                primaryColor = effectivePrimary
                                            )
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 72.dp), 
                                                color = dividerColor.copy(alpha = 0.5f)
                                            )
                                        }
                                        item { Spacer(modifier = Modifier.height(100.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPolicyDialog) {
            ThermalPolicyDialog(
                currentPolicy = thermalPolicy,
                onDismiss = { showPolicyDialog = false },
                onSelect = {
                    viewModel.setThermalPolicy(it)
                    showPolicyDialog = false
                },
                isGlassActive = isGlassActive,
                textColor = textColor,
                subTextColor = subTextColor,
                primaryColor = effectivePrimary
            )
        }
    }
}

@Composable
fun GlassThermalHeader(
    navController: NavController,
    stats: ThermalStats,
    isAutoRefresh: Boolean,
    onAutoRefreshToggle: (Boolean) -> Unit,
    thermalPolicy: String,
    onPolicyClick: () -> Unit,
    isCustomBg: Boolean,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
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
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.thermal_back), tint = textColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.thermal_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val maxTempColor = when {
                stats.maxTemp > 70 -> if (isCustomBg) Color(0xFFCF6679) else MaterialTheme.colorScheme.error
                stats.maxTemp > 50 -> if (isCustomBg) Color(0xFFFFB74D) else MaterialTheme.colorScheme.tertiary
                else -> primaryColor.copy(alpha = 0.8f)
            }
            val maxTempOnColor = if (isCustomBg) Color.White else when {
                stats.maxTemp > 70 -> MaterialTheme.colorScheme.onError
                stats.maxTemp > 50 -> MaterialTheme.colorScheme.onTertiary
                else -> MaterialTheme.colorScheme.onPrimary
            }
            
            GlassStatCardThermal(
                label = stringResource(R.string.thermal_max_temp),
                value = "${stats.maxTemp}°C",
                icon = Icons.Rounded.Whatshot,
                color = maxTempColor,
                onColor = maxTempOnColor,
                modifier = Modifier.weight(1f),
                isGlassActive = isGlassActive,
                hazeState = hazeState
            )
            GlassStatCardThermal(
                label = stringResource(R.string.thermal_avg_temp),
                value = "${stats.avgTemp.toInt()}°C",
                icon = Icons.Rounded.Speed,
                color = if (isCustomBg) Color.White.copy(0.15f) else MaterialTheme.colorScheme.secondaryContainer,
                onColor = textColor,
                modifier = Modifier.weight(1f),
                isGlassActive = isGlassActive,
                hazeState = hazeState
            )
            GlassStatCardThermal(
                label = stringResource(R.string.thermal_active_zones),
                value = "${stats.activeZones}/${stats.totalZones}",
                icon = Icons.Rounded.Analytics,
                color = if (isCustomBg) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                onColor = subTextColor,
                modifier = Modifier.weight(1f),
                isGlassActive = isGlassActive,
                hazeState = hazeState
            )
        }

        GlassThermalSettingCard(
            isGlassActive = isGlassActive,
            hazeState = hazeState,
            cardColor = if (isCustomBg) Color.Black.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onPolicyClick() }.padding(end = 12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(32.dp)
                            .background(if (isCustomBg) primaryColor.copy(0.3f) else MaterialTheme.colorScheme.primaryContainer, CircleShape), 
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Settings, 
                            contentDescription = null, 
                            tint = primaryColor, 
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(stringResource(R.string.thermal_policy), style = MaterialTheme.typography.labelSmall, color = subTextColor)
                        Text(
                            thermalPolicy.replaceFirstChar { it.uppercase() }, 
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.thermal_live), style = MaterialTheme.typography.labelMedium, color = subTextColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isAutoRefresh, 
                        onCheckedChange = onAutoRefreshToggle, 
                        modifier = Modifier.scale(0.8f),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = if (isCustomBg) Color.White else Color.Gray,
                            uncheckedTrackColor = if (isCustomBg) Color.White.copy(0.3f) else Color.Gray.copy(0.3f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassStatCardThermal(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onColor: Color,
    modifier: Modifier = Modifier,
    isGlassActive: Boolean,
    hazeState: HazeState
) {
    val shape = RoundedCornerShape(24.dp)
    
    val cardModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = color,
                    blurRadius = 20.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
    } else {
        Modifier.clip(shape)
    }

    Surface(
        modifier = modifier.then(cardModifier),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else color
    ) {
        Column(
            modifier = Modifier.padding(14.dp).height(100.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onColor.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = onColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = onColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun GlassThermalSettingCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else cardColor
        )
    ) {
        content()
    }
}

@Composable
fun GlassThermalTabRow(
    selectedPage: Int,
    onPageSelected: (Int) -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val tabs = listOf(
        stringResource(R.string.thermal_tab_zones), 
        stringResource(R.string.thermal_tab_cooling), 
        stringResource(R.string.thermal_tab_disabled)
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
            val selected = selectedPage == index
            val bgColor by animateColorAsState(
                if (selected) {
                    if (isGlassActive) primaryColor.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primaryContainer
                } else Color.Transparent, label = ""
            )
            val txtColor by animateColorAsState(
                if (selected) Color.White else if (isGlassActive) textColor else subTextColor, label = ""
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(bgColor)
                    .clickable { onPageSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    ), 
                    color = txtColor
                )
            }
        }
    }
}

@Composable
fun GlassFilterChips(
    filters: List<String>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    isGlassActive: Boolean,
    primaryColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val selected = filter == selectedFilter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter, color = if (selected) Color.White else textColor) },
                leadingIcon = if (selected) {
                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = Color.White,
                    containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun ThermalZoneCardGlass(
    zone: ThermalZone,
    tripPoints: List<TripPoint>?,
    onToggle: () -> Unit,
    onShowTrips: () -> Unit,
    onHideTrips: () -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
    val tempColor = when {
        !zone.isEnabled -> subTextColor
        zone.temperature > 70 -> if (cardColor == Color.Transparent) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.error
        zone.temperature > 50 -> if (cardColor == Color.Transparent) Color(0xFFFFB74D) else MaterialTheme.colorScheme.tertiary
        else -> primaryColor
    }

    val shape = RoundedCornerShape(16.dp)
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tempColor.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (zone.isEnabled) Icons.Rounded.Thermostat else Icons.Rounded.AcUnit,
                        contentDescription = null,
                        tint = tempColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if(zone.isEnabled) textColor else subTextColor
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${zone.type} • Zone ${zone.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }

            Text(
                text = "${zone.temperature}°C",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = tempColor
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = subTextColor
            )
        }

        AnimatedVisibility(visible = expanded) {
            val cardModifier = if (isGlassActive) {
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .clip(shape)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = cardColor.copy(alpha = 0.3f),
                            blurRadius = 20.dp,
                            noiseFactor = 0.05f,
                            tints = emptyList()
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            }

            Card(
                modifier = cardModifier,
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggle,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (zone.isEnabled) 
                                    (if (isGlassActive) Color(0xFFCF6679) else MaterialTheme.colorScheme.errorContainer)
                                else 
                                    primaryColor.copy(alpha = 0.8f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (zone.isEnabled) Icons.Rounded.Block else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (zone.isEnabled) stringResource(R.string.thermal_disable) else stringResource(R.string.thermal_enable))
                        }

                        OutlinedButton(
                            onClick = { if (tripPoints != null) onHideTrips() else onShowTrips() },
                            modifier = Modifier.weight(1f),
                            border = if (isGlassActive) BorderStroke(1.dp, Color.White.copy(0.5f)) else null,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (tripPoints != null) stringResource(R.string.thermal_hide_trips) else stringResource(R.string.thermal_trip_points))
                        }
                    }

                    if (tripPoints != null && tripPoints.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isGlassActive) Color.White.copy(0.05f) else MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.thermal_trip_config),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                tripPoints.forEach { trip ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = trip.type.replaceFirstChar { it.uppercase() }, 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor
                                        )
                                        Text(
                                            text = "${trip.temperature}°C", 
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoolingDeviceCardGlass(
    device: ThermalCoolingDevice,
    onStateChange: (Int) -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    var showSlider by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(device.curState.toFloat()) }

    val shape = RoundedCornerShape(16.dp)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSlider = !showSlider }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGlassActive) primaryColor.copy(0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ModeFanOff,
                        contentDescription = null,
                        tint = if (isGlassActive) primaryColor else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${device.type} • ID: ${device.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isGlassActive) primaryColor.copy(0.3f) else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${device.curState}/${device.maxState}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        AnimatedVisibility(visible = showSlider) {
            val sliderCardModifier = if (isGlassActive) {
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .clip(shape)
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = cardColor.copy(alpha = 0.3f),
                            blurRadius = 20.dp,
                            noiseFactor = 0.05f,
                            tints = emptyList()
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
                    .padding(16.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                    .padding(16.dp)
            }

            Column(modifier = sliderCardModifier) {
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onStateChange(sliderValue.toInt()) },
                    valueRange = 0f..device.maxState.toFloat(),
                    steps = if (device.maxState > 1) device.maxState - 1 else 0,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = if (isGlassActive) Color.White.copy(0.3f) else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    Text(
                        "${stringResource(R.string.thermal_current_state)}: ${sliderValue.toInt()}", 
                        style = MaterialTheme.typography.labelMedium.copy(color = primaryColor, fontWeight = FontWeight.Bold)
                    )
                    Text("${device.maxState}", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                }
            }
        }
    }
}

@Composable
fun ThermalPolicyDialog(
    currentPolicy: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    isGlassActive: Boolean,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val policies = listOf("default", "performance", "balanced", "powersave", "user_space")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isGlassActive) Color.Black.copy(0.8f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.thermal_policy_dialog_title),
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column {
                policies.forEach { policy ->
                    ListItem(
                        headlineContent = { 
                            Text(
                                policy.replaceFirstChar { it.uppercase() },
                                fontWeight = if (policy == currentPolicy) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        },
                        leadingContent = {
                            if (policy == currentPolicy) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = primaryColor)
                            } else {
                                Icon(Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = subTextColor)
                            }
                        },
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onSelect(policy) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = primaryColor)
            }
        }
    )
}
