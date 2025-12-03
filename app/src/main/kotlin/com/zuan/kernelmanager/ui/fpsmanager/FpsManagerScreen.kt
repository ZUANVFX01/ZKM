/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.fpsmanager

import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zuan.kernelmanager.utils.ShellExecutor
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

enum class FpsManagerTab(
    val title: String,
    val icon: ImageVector
) {
    Home("Home", Icons.Default.Home),
    Stats("Stats", Icons.Default.Analytics),
    Games("Games", Icons.Default.Gamepad),
    Overlay("Overlay", Icons.Default.Layers)
}

@Composable
fun FpsManagerScreen(
    navController: NavController
) {
    val tabs = FpsManagerTab.values()
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    
    // LOGIKA BARU: Handle Shizuku Permission dengan lebih rapi
    val shizukuListener = remember {
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            // Listener kosong, hanya untuk menangkap callback agar tidak error
        }
    }

    DisposableEffect(Unit) {
        // 1. Daftarkan listener
        Shizuku.addRequestPermissionResultListener(shizukuListener)
        
        // 2. Cek apakah Shizuku tersedia
        if (ShellExecutor.isShizukuAvailable()) {
            try {
                // 3. LOGIKA FIX: Cek dulu apakah izin sudah ada?
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    // Kalau BELUM ada izin, baru minta
                    ShellExecutor.requestShizukuPermission()
                }
                // Kalau SUDAH ada izin, diam saja (jangan jalankan requestPermission)
            } catch (e: Exception) {
                // Handle jika terjadi error saat cek permission
            }
        }

        onDispose {
            Shizuku.removeRequestPermissionResultListener(shizukuListener)
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = statusBarPadding.calculateTopPadding())
    ) {
        // --- Header ---
        Text(
            text = "ZKM FPS Manager",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        // --- Tabs ---
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Pager Content ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (tabs[page]) {
                FpsManagerTab.Home -> FpsManagerHomeContent()
                FpsManagerTab.Stats -> FpsManagerStatsContent()
                FpsManagerTab.Games -> FpsManagerGamesContent()
                FpsManagerTab.Overlay -> FpsManagerOverlayContent()
            }
        }
    }
}