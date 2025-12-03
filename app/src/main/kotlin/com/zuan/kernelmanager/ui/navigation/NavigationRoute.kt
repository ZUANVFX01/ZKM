/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Battery0Bar
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zuan.kernelmanager.ui.battery.BatteryScreen
import com.zuan.kernelmanager.ui.setedit.SetEditScreen
import com.zuan.kernelmanager.ui.home.HomeScreen
import com.zuan.kernelmanager.ui.kernelParameter.KernelParameterScreen
import com.zuan.kernelmanager.ui.fpsmanager.FpsManagerScreen
import com.zuan.kernelmanager.ui.soc.SoCScreen
import com.zuan.kernelmanager.ui.fastshell.FastShellScreen
import com.zuan.kernelmanager.ui.gpu.AdrenoScreen
import com.zuan.kernelmanager.ui.gpu.MtkScreen
import com.zuan.kernelmanager.ui.gpu.GenericGpuScreen
import com.zuan.kernelmanager.ui.proces.ProcessManagerScreen

sealed class NavigationRoute(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : NavigationRoute("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object SoC : NavigationRoute("soc", "SoC", Icons.Filled.Memory, Icons.Outlined.Memory)
    object Battery : NavigationRoute("battery", "Battery", Icons.Filled.BatteryFull, Icons.Outlined.Battery0Bar)
    object KernelParameter : NavigationRoute("kernel", "Kernel", Icons.Filled.Storage, Icons.Outlined.Storage)
    
    // Extension Routes
    object FastShell : NavigationRoute("FastShell", "FastShell", Icons.Filled.Code, Icons.Outlined.Code)
    object SetEdit : NavigationRoute("SetEdit", "Set_Edit", Icons.Filled.Description, Icons.Outlined.Description)
    object FpsManager : NavigationRoute("fps_manager", "FPS Manager", Icons.Filled.Speed, Icons.Outlined.Speed)
    
    // New Extension Route
    object ProcessManager : NavigationRoute("process_manager", "Process Monitor", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    
    // GPU Sub-screens
    object Adreno : NavigationRoute("adreno_screen", "Adreno Settings", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    object Mtk : NavigationRoute("mtk_screen", "Mtk Settings", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    object GenericGpu : NavigationRoute("generic_gpu_screen", "GPU Settings", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
}

@Composable
fun ZuanKernelManagerNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NavigationRoute.Home.route) {
        // Main Tabs
        composable(NavigationRoute.Home.route) { HomeScreen(navController = navController) }
        composable(NavigationRoute.SoC.route) { SoCScreen(navController = navController) }
        composable(NavigationRoute.Battery.route) { BatteryScreen(navController = navController) }
        composable(NavigationRoute.KernelParameter.route) { KernelParameterScreen(navController = navController) }
        
        // Extension Screens
        composable(NavigationRoute.FastShell.route) { FastShellScreen(navController = navController) }
        composable(NavigationRoute.SetEdit.route) { SetEditScreen(navController = navController) }
        composable(NavigationRoute.FpsManager.route) { FpsManagerScreen(navController = navController) }
        
        // New Extension Screen
        composable(NavigationRoute.ProcessManager.route) { ProcessManagerScreen(navController = navController) }

        // GPU Screens
        composable(NavigationRoute.Adreno.route) { AdrenoScreen(navController = navController) }
        composable(NavigationRoute.Mtk.route) { MtkScreen(navController = navController) }
        composable(NavigationRoute.GenericGpu.route) { GenericGpuScreen(navController = navController) }
    }
}