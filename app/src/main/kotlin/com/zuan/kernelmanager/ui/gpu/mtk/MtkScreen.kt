/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.gpu.mtk

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.zuan.kernelmanager.ui.gpu.mtk.tabs.*
import com.zuan.kernelmanager.ui.gpu.mtk.viewmodel.MtkViewModel
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
import kotlinx.coroutines.launch

data class MtkTabItem(
    val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MtkScreen(
    navController: NavController,
    viewModel: MtkViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val hazeState = remember { HazeState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    val pagerState = rememberPagerState(pageCount = { 6 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // THEME STATES
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

    // Generate themed colors
    val (tintedBackground, themeCardColor) = generateMtkThemedBackground(finalPrimary, useDarkTheme)

    val isGlassActive = isHazeEnabled && isCustomBg
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else tintedBackground
    
    val targetCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
        else -> themeCardColor
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val appBarHazeStyle = HazeMaterials.regular()

    val tabs = listOf(
        MtkTabItem(R.string.mtk_tab_freq, Icons.Filled.Speed),
        MtkTabItem(R.string.mtk_tab_dram, Icons.Filled.Memory),
        MtkTabItem(R.string.mtk_tab_boost, Icons.Filled.RocketLaunch),
        MtkTabItem(R.string.mtk_tab_ppm, Icons.Filled.SettingsSuggest),
        MtkTabItem(R.string.mtk_tab_cpu, Icons.Filled.DeveloperMode),
        MtkTabItem(R.string.mtk_tab_thermal, Icons.Filled.Thermostat)
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text(
                            stringResource(R.string.mtk_gpu_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.mtk_gpu_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceVariantColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAllData() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    actionIconContentColor = onSurfaceColor,
                    navigationIconContentColor = onSurfaceColor
                ),
                modifier = Modifier.hazeEffect(
                    state = hazeState,
                    style = appBarHazeStyle
                )
            )
        }
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
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                // Expressive Tab Row
                Surface(
                    color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(64.dp)
                        .then(
                            if (isGlassActive) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style = HazeStyle(
                                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                        blurRadius = 20.dp,
                                        noiseFactor = 0.05f,
                                        tints = emptyList()
                                    )
                                )
                            } else Modifier
                        )
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            if (pagerState.currentPage < tabPositions.size) {
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(finalPrimary.copy(alpha = 0.25f))
                                )
                            }
                        },
                        divider = {},
                        modifier = Modifier.fillMaxSize()
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                modifier = Modifier.height(56.dp),
                                text = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                                        modifier = Modifier.fillMaxHeight()
                                    ) {
                                        Icon(
                                            tab.icon, 
                                            contentDescription = null,
                                            tint = if (selected) finalPrimary 
                                                   else onSurfaceVariantColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            stringResource(tab.titleRes),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) finalPrimary 
                                                   else onSurfaceVariantColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 2
                ) { page ->
                    when (page) {
                        0 -> MtkFreqTab(
                            state = uiState.freqState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onSetMin = { viewModel.setMinFreq(it) },
                            onSetMax = { viewModel.setMaxFreq(it) },
                            onToggleDvfs = { viewModel.setDvfsEnabled(it) },
                            onLockFreq = { viewModel.lockGpuFreq(it) },
                            onUnlockFreq = { viewModel.unlockGpuFreq() }
                        )
                        1 -> MtkDramTab(
                            state = uiState.dramState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onSetDramFreq = { viewModel.setDramFreq(it) }
                        )
                        2 -> MtkBoostTab(
                            state = uiState.boostState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onToggleFeature = { feature, enabled -> viewModel.toggleFeature(feature, enabled) }
                        )
                        3 -> MtkPpmTab(
                            state = uiState.ppmState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onTogglePolicy = { idx, enabled -> viewModel.togglePpmPolicy(idx, enabled) },
                            onTogglePpm = { viewModel.setPpmEnabled(it) }
                        )
                        4 -> MtkCpuTab(
                            state = uiState.cpuMiscState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onSetCciMode = { viewModel.setCciMode(it) },
                            onSetPowerMode = { viewModel.setPowerMode(it) },
                            onSetEemOffset = { detName, offset -> viewModel.setEemOffset(detName, offset) }
                        )
                        5 -> MtkThermalTab(
                            state = uiState.thermalState,
                            hazeState = hazeState,
                            cardColor = targetCardColor,
                            isGlassActive = isGlassActive,
                            accentColor = finalPrimary,
                            onToggleEara = { viewModel.toggleFeature("eara", it) },
                            onToggleEaraFake = { viewModel.toggleFeature("eara_fake", it) }
                        )
                    }
                }
            }
        }
    }
}

// Helper function
@Composable
private fun generateMtkThemedBackground(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
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
