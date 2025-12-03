/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun BottomNavigationBar(
    navController: NavController,
    hazeState: HazeState? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationRoute.Home,
        NavigationRoute.SoC,
        NavigationRoute.Battery,
        NavigationRoute.KernelParameter,
    )

    val glassModifier = if (hazeState != null) {
        Modifier
            .fillMaxWidth()
            .hazeChild(
                state = hazeState,
                style = HazeMaterials.regular()
            )
    } else {
        Modifier.fillMaxWidth()
    }

    val containerColor = if (hazeState != null) Color.Transparent else NavigationBarDefaults.containerColor

    NavigationBar(
        modifier = glassModifier,
        containerColor = containerColor,
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.route == item.route
            
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                    )
                },
                // PERBAIKAN: AnimatedVisibility dihapus.
                // Text hanya dirender biasa. Karena 'alwaysShowLabel = false',
                // dia otomatis hanya muncul (visible) kalau 'selected = true'.
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                alwaysShowLabel = false 
            )
        }
    }
}
