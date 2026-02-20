/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.navigation.NavController
import com.zuan.kernelmanager.ui.about.AboutScreen
import com.zuan.kernelmanager.ui.home.HomeScreen
import com.zuan.kernelmanager.ui.overall.OverallScreen
import com.zuan.kernelmanager.ui.soc.SoCScreen
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

@Composable
fun MainSwipeScreen(
    navController: NavController,
    hazeState: HazeState,
    onOpenDialog: (ActiveDialog) -> Unit
) {
    // 4 Halaman Utama
    val tabs = listOf(
        NavigationRoute.Overall,
        NavigationRoute.Dashboard,
        NavigationRoute.SoC,
        NavigationRoute.About
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Logic Warna
    val isAppDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val containerColor = Color.Transparent

    Scaffold(
        containerColor = containerColor,
        topBar = {
            ModernSwipeableNavBar(
                tabs = tabs,
                pagerState = pagerState,
                hazeState = hazeState,
                onTabSelected = { index ->
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                navController = navController,
                onOpenDialog = onOpenDialog
            )
        }
    ) { innerPadding ->
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            beyondViewportPageCount = 1, 
            userScrollEnabled = true
        ) { page ->
            
            when (tabs[page]) {
                NavigationRoute.Overall -> OverallScreen(navController = navController, hazeState = hazeState)
                
                NavigationRoute.Dashboard -> HomeScreen(navController = navController, hazeState = hazeState)
                
                // [UPDATE] Tangkap Sinyal dari SoCScreen
                NavigationRoute.SoC -> SoCScreen(
                    navController = navController, 
                    hazeState = hazeState,
                    onParentSignal = { direction ->
                        scope.launch {
                            // Hitung halaman target berdasarkan sinyal
                            // direction -1 = Mundur (ke halaman 1 / Dashboard)
                            // direction 1 = Maju (ke halaman 3 / About)
                            val targetPage = pagerState.currentPage + direction
                            if (targetPage in 0 until tabs.size) {
                                pagerState.animateScrollToPage(targetPage)
                            }
                        }
                    }
                )
                
                NavigationRoute.About -> AboutScreen(navController = navController, hazeState = hazeState)
                
                else -> {}
            }
        }
    }
}
