/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.soc

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.socmenu.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.launch

@Composable
fun generateSoCBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
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

enum class SoCTab(val icon: ImageVector) {
    CpuGpu(Icons.Default.Memory),
    Sched(Icons.Default.Schedule),
    Memory(Icons.Default.SdStorage),
    Network(Icons.Default.Wifi)
}

@Composable
fun SoCTab.getTitle(): String = when(this) {
    SoCTab.CpuGpu -> stringResource(R.string.soc_tab_processor)
    SoCTab.Sched -> stringResource(R.string.soc_tab_scheduler)
    SoCTab.Memory -> stringResource(R.string.soc_tab_memory)
    SoCTab.Network -> stringResource(R.string.soc_tab_network)
}

@Composable
fun SoCScreen(
    navController: NavController,
    hazeState: HazeState,
    settingsViewModel: SettingsViewModel = viewModel(),
    onParentSignal: (Int) -> Unit = {} 
) {
    val schedulerVM: SchedulerViewModel = viewModel()
    val memoryVM: MemoryViewModel = viewModel()
    val networkVM: NetworkViewModel = viewModel()
    val density = LocalDensity.current

    val themeMode by settingsViewModel.themeMode.collectAsState()
    val isCustomBg by settingsViewModel.isCustomBackground.collectAsState()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsState()
    val isDynamic by settingsViewModel.isDynamicColor.collectAsState()
    val themeColor by settingsViewModel.currentThemeColor.collectAsState()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsState()
    
    val isCustomColor by settingsViewModel.isCustomColor.collectAsState()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsState()

    val isAppDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
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

    val (themeBgColor, themeCardColor) = generateSoCBackgroundColors(isAppDarkTheme, finalPrimary)
    val targetBgColor = themeBgColor
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else targetBgColor
    val cardBg = when {
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
    }

    val isGlassActive = isCustomBg && isHazeEnabled
    val contentColor = if (isAppDarkTheme) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subContentColor = if (isAppDarkTheme) Color(0xFFAAAAAA) else Color(0xFF666666)
    val activeColor = finalPrimary

    val appBarHazeStyle = HazeStyle(
        backgroundColor = themeBgColor.copy(alpha = 0.5f), 
        blurRadius = 24.dp,
        noiseFactor = 0.1f, 
        tints = listOf(HazeTint(themeBgColor.copy(alpha = 0.4f))) 
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val tabs = SoCTab.values()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    val isCollapsed by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.6f } }
    
    // [FIX] State untuk menyimpan height AppBar yang terukur
    var appBarHeightPx by remember { mutableIntStateOf(0) }
    // [FIX] Convert ke Dp, dengan fallback jika belum terukur (200dp = estimate LargeTopAppBar expanded)
    val appBarHeightDp = if (appBarHeightPx > 0) {
        with(density) { appBarHeightPx.toDp() }
    } else {
        200.dp // Fallback agar content tidak tertutup saat initial load
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0) // Handle insets manual
    ) { _ -> 
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
        ) {
            
            Column(modifier = Modifier.fillMaxSize()) {
                // [FIX] Spacer dengan height AppBar yang dinamis
                Spacer(modifier = Modifier.height(appBarHeightDp))
                
                // [FIX] Content pager tanpa padding top tambahan (sudah dihandle Spacer)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    pageSpacing = 0.dp,
                    beyondViewportPageCount = 1,
                    userScrollEnabled = true
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        when (tabs[page]) {
                            SoCTab.CpuGpu -> CpuGpuScreen(navController, hazeState)
                            
                            SoCTab.Sched -> SchedulerScreen(
                                viewModel = schedulerVM, 
                                hazeState = hazeState,
                                cardBg = cardBg, 
                                contentColor = contentColor, 
                                subContentColor = subContentColor, 
                                activeColor = activeColor
                            )
                            SoCTab.Memory -> MemoryScreen(
                                viewModel = memoryVM, 
                                hazeState = hazeState,
                                cardBg = cardBg, 
                                contentColor = contentColor, 
                                subContentColor = subContentColor, 
                                activeColor = activeColor
                            )
                            SoCTab.Network -> NetworkScreen(
                                viewModel = networkVM, 
                                hazeState = hazeState,
                                cardBg = cardBg, 
                                contentColor = contentColor, 
                                subContentColor = subContentColor, 
                                activeColor = activeColor
                            )
                        }
                    }
                }
            }

            // [FIX] AppBar dengan onGloballyPositioned untuk mengukur height
            LargeTopAppBar(
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isCollapsed) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        AnimatedContent(
                            targetState = isCollapsed,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 })
                                    .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 })
                            },
                            label = "AppBarTitleAnim"
                        ) { collapsed ->
                            if (!collapsed) {
                                Text(
                                    stringResource(R.string.soc_system_monitor),
                                    style = MaterialTheme.typography.titleLarge, 
                                    color = subContentColor, 
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(0.dp))
                            }
                        }
                        
                        ExpressiveTabRow(
                            tabs = tabs,
                            selectedIndex = pagerState.currentPage,
                            onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                            activeColor = activeColor,
                            contentColor = contentColor,
                            subContentColor = subContentColor,
                            hazeState = hazeState,
                            isGlassActive = isGlassActive,
                            modifier = if (isCollapsed) Modifier.padding(top = 4.dp) else Modifier
                        )
                    }
                },
                navigationIcon = {
                     IconButton(onClick = { }) {
                         Icon(Icons.Rounded.Bolt, null, tint = activeColor)
                     }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent, 
                    actionIconContentColor = activeColor,
                    navigationIconContentColor = activeColor
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    // [PENTING] Update height state saat layout berubah
                    .onGloballyPositioned { coordinates ->
                        if (coordinates.size.height > 0 && appBarHeightPx != coordinates.size.height) {
                            appBarHeightPx = coordinates.size.height
                        }
                    }
                    .hazeEffect(
                        state = hazeState, 
                        style = appBarHazeStyle
                    )
            )
        }
    }
}

@Composable
fun ExpressiveTabRow(
    tabs: Array<SoCTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    activeColor: Color,
    contentColor: Color,
    subContentColor: Color,
    hazeState: HazeState? = null,
    isGlassActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) { listState.animateScrollToItem(selectedIndex, -100) }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(tabs) { index, tab ->
            val isSelected = selectedIndex == index
            
            var tabModifier = Modifier
                .height(36.dp)
                .clip(CircleShape)
                .clickable { onTabSelected(index) }
            
            val iconColor: Color
            val textColor: Color

            if (isSelected && isGlassActive && hazeState != null) {
                tabModifier = tabModifier
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = activeColor.copy(alpha = 0.3f),
                            blurRadius = 16.dp,
                            noiseFactor = 0.05f,
                            tints = emptyList()
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha=0.15f), CircleShape)
                
                iconColor = activeColor
                textColor = activeColor
            } else {
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent, 
                    label = "tabBg"
                )
                
                tabModifier = tabModifier.background(containerColor)
                
                val animIconColor by animateColorAsState(if (isSelected) activeColor else subContentColor, label = "icon")
                val animTextColor by animateColorAsState(if (isSelected) activeColor else subContentColor, label = "text")
                iconColor = animIconColor
                textColor = animTextColor
            }

            Row(
                modifier = tabModifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(tab.icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    tab.getTitle(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, 
                    color = textColor
                )
            }
        }
    }
}
