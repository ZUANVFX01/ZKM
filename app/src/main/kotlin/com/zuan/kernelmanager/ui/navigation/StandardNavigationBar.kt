/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.NavLabelState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun StandardNavigationBar(
    navController: NavController,
    hazeState: HazeState,
    labelState: NavLabelState
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationRoute.Overall,
        NavigationRoute.Dashboard,
        NavigationRoute.SoC,
        NavigationRoute.About
    )

    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier.hazeEffect(state = hazeState, style = HazeMaterials.thin())
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.route == item.route
            
            val showLabel = when (labelState) {
                NavLabelState.ALWAYS_SHOW -> true
                NavLabelState.ALWAYS_HIDE -> false
                NavLabelState.ONLY_SELECTED -> isSelected
            }

            val labelText = when(item) {
                NavigationRoute.Overall -> stringResource(R.string.nav_home)
                NavigationRoute.Dashboard -> stringResource(R.string.nav_home)
                NavigationRoute.SoC -> stringResource(R.string.nav_soc)
                NavigationRoute.About -> stringResource(R.string.nav_about)
                else -> ""
            }

            NavigationBarItem(
                icon = { 
                    Icon(
                        if (isSelected) item.selectedIcon else item.unselectedIcon, 
                        contentDescription = labelText
                    ) 
                },
                label = if (showLabel) { { Text(text = labelText) } } else null,
                selected = isSelected,
                onClick = {
                    if (currentDestination?.route != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = showLabel
            )
        }
    }
}
