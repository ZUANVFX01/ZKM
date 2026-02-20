/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(
    ExperimentalMaterial3Api::class, 
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.zuan.kernelmanager.ui.fpsmanager

import android.net.Uri
import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

enum class FpsManagerTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home(R.string.fps_tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    Stats(R.string.fps_tab_stats, Icons.Filled.Analytics, Icons.Outlined.Analytics),
    Games(R.string.fps_tab_games, Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    Overlay(R.string.fps_tab_overlay, Icons.Filled.Layers, Icons.Outlined.Layers)
}

@Composable
fun generateFpsBackgroundColors(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
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

@Composable
fun FpsManagerScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val tabs = FpsManagerTab.values()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    val localHazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )
    val isCollapsed by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.6f } }

    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    val isAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    
    // --- THEME & BACKGROUND STATES ---
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val isCustomBg = bgType == BgType.GALLERY
    
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColor by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }

    val effectivePrimary = remember(isDynamic, themeColor, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColor.primary
        }
    }

    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val (tintedBackground, cardContainerColor) = generateFpsBackgroundColors(finalPrimary, useDarkTheme)

    val isGlassActive = isHazeEnabled && isCustomBg
    
    val actualCardContainerColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }

    val cardShape = RoundedCornerShape(24.dp)
    
    val glassCardModifier = if (isGlassActive) {
        Modifier
            .clip(cardShape)
            .hazeEffect(
                state = localHazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = listOf(HazeTint(Color.White.copy(alpha = 0.1f)))
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
                shape = cardShape
            )
    } else Modifier

    val cardElevation by animateDpAsState(
        targetValue = if (isCustomBg) 0.dp else 2.dp,
        label = "elevation_anim"
    )

    val contentColor = when {
        isCustomBg -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val subContentColor = when {
        isCustomBg -> Color.White.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val screenBackground = when {
        isCustomBg -> Color.Transparent
        else -> tintedBackground
    }

    val appBarHazeStyle = if (isGlassActive) {
        HazeStyle(
            backgroundColor = Color.Black.copy(alpha = 0.3f),
            blurRadius = 30.dp,
            noiseFactor = 0.1f,
            tints = listOf(HazeTint(Color.Black.copy(alpha = 0.1f)))
        )
    } else {
        HazeMaterials.regular()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { paddingValues ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackground)
        ) {
            // LAYER 0: GLOBAL CUSTOM BACKGROUND (FOTO/VIDEO & WEATHER)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = localHazeState, zIndex = 0f)
            ) {
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

                WeatherEffectOverlay(
                    effect = weatherEffect,
                    intensity = weatherIntensity,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // LAYER 1: PAGER CONTENT
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = localHazeState, zIndex = 1f)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = headerHeightDp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                ) { page ->
                    when (tabs[page]) {
                        FpsManagerTab.Home -> FpsManagerHomeContent(
                            cardBg = actualCardContainerColor, 
                            contentColor = contentColor, 
                            subContentColor = subContentColor, 
                            activeColor = finalPrimary, 
                            elevation = cardElevation,
                            isGlassActive = isGlassActive,
                            hazeState = localHazeState,
                            glassModifier = glassCardModifier
                        )
                        FpsManagerTab.Stats -> FpsManagerStatsContent(
                            contentColor = contentColor, 
                            subContentColor = subContentColor,
                            isGlassActive = isGlassActive,
                            hazeState = localHazeState,
                            glassModifier = glassCardModifier
                        )
                        FpsManagerTab.Games -> FpsManagerGamesContent(
                            contentColor = contentColor, 
                            subContentColor = subContentColor,
                            isGlassActive = isGlassActive,
                            hazeState = localHazeState
                        )
                        FpsManagerTab.Overlay -> FpsManagerOverlayContent(
                            cardBg = actualCardContainerColor, 
                            contentColor = contentColor, 
                            subContentColor = subContentColor, 
                            activeColor = finalPrimary, 
                            elevation = cardElevation,
                            isGlassActive = isGlassActive,
                            hazeState = localHazeState,
                            glassModifier = glassCardModifier
                        )
                    }
                }
            }

            // LAYER 2: HEADER APPBAR & TABS
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .onGloballyPositioned { headerHeightPx = it.size.height }
                    .hazeEffect(
                        state = localHazeState,
                        style = appBarHazeStyle
                    )
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                
                LargeTopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = isCollapsed,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 })
                                    .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 })
                            },
                            label = "AppBarTitle"
                        ) { collapsed ->
                            if (collapsed) {
                                Text(stringResource(R.string.fps_manager_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                            } else {
                                Column {
                                    Text(stringResource(R.string.fps_display_header), style = MaterialTheme.typography.titleLarge, color = subContentColor)
                                    Text(stringResource(R.string.fps_manager_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = contentColor)
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_cancel), tint = contentColor)
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = contentColor,
                        titleContentColor = contentColor
                    ),
                    scrollBehavior = scrollBehavior
                )

                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = finalPrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(pagerState.currentPage)
                                .width(32.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                            color = finalPrimary,
                            width = 32.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isSelected = pagerState.currentPage == index
                        Tab(
                            selected = isSelected,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { 
                                Text(
                                    text = stringResource(tab.titleRes), 
                                    style = MaterialTheme.typography.labelMedium, 
                                    maxLines = 1,
                                    color = if (isSelected) finalPrimary else subContentColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            icon = { 
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon, 
                                    contentDescription = null,
                                    tint = if (isSelected) finalPrimary else subContentColor
                                ) 
                            },
                            selectedContentColor = finalPrimary,
                            unselectedContentColor = subContentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
