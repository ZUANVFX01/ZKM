/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Description 
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zuan.kernelmanager.R
import dev.chrisbanes.haze.HazeState

// --- IMPORTS SCREEN ---
import com.zuan.kernelmanager.ui.overall.OverallScreen 
import com.zuan.kernelmanager.ui.home.HomeScreen
import com.zuan.kernelmanager.ui.soc.SoCScreen 
import com.zuan.kernelmanager.ui.settings.SettingsScreen
import com.zuan.kernelmanager.ui.about.AboutScreen 
import com.zuan.kernelmanager.ui.about.changelogs.ChangelogsScreen
import com.zuan.kernelmanager.ui.about.opensource.OpenSourceScreen
import com.zuan.kernelmanager.ui.settings.WallpaperStyleScreen 
import com.zuan.kernelmanager.ui.general.GeneralScreen
import com.zuan.kernelmanager.ui.terminal.TerminalScreen
import com.zuan.kernelmanager.ui.setedit.SetEditScreen
import com.zuan.kernelmanager.ui.fpsmanager.FpsManagerScreen
import com.zuan.kernelmanager.ui.proces.ProcessManagerScreen
import com.zuan.kernelmanager.ui.settings.LanguageScreen
import com.zuan.kernelmanager.ui.flasher.KernelFlasherScreen
import com.zuan.kernelmanager.ui.activitylauncher.ActivityLauncherScreen
import com.zuan.kernelmanager.ui.ksuweb.KsuWebuiScreen
import com.zuan.kernelmanager.ui.gpu.adreno.AdrenoScreen
import com.zuan.kernelmanager.ui.gpu.mtk.MtkScreen
import com.zuan.kernelmanager.ui.logsview.LogsViewScreen

// Import Menu System
import com.zuan.kernelmanager.ui.home.menu.DebloatFreezeScreen
import com.zuan.kernelmanager.ui.home.menu.ThermalDevicesScreen
import com.zuan.kernelmanager.ui.home.menu.DisplayScreen
import com.zuan.kernelmanager.ui.home.menu.BatteryControllerScreen
import com.zuan.kernelmanager.ui.home.menu.DozeModeScreen
import com.zuan.kernelmanager.ui.home.menu.Dex2oatScreen

sealed class NavigationRoute(val route: String, val titleRes: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    // Route Khusus Container Swipe
    object MainSwipeContainer : NavigationRoute("main_swipe_container", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)

    object Overall : NavigationRoute("overall_screen", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)
    object Dashboard : NavigationRoute("dashboard_grid", R.string.nav_home, Icons.Filled.GridView, Icons.Outlined.GridView)
    object SoC : NavigationRoute("soc_screen", R.string.nav_soc, Icons.Filled.Tune, Icons.Outlined.Tune)
    object Settings : NavigationRoute("settings_screen", R.string.nav_pref, Icons.Filled.Settings, Icons.Outlined.Settings)
    object About : NavigationRoute("about_screen", R.string.nav_pref, Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent)
    object General : NavigationRoute("general_screen", R.string.sect_general, Icons.Filled.GridView, Icons.Outlined.GridView)
    object WallpaperStyle : NavigationRoute("wallpaper_style_screen", R.string.sect_appearance, Icons.Filled.Palette, Icons.Outlined.Palette)
    
    object Terminal : NavigationRoute("terminal_screen", R.string.nav_fastshell, Icons.Filled.Code, Icons.Outlined.Code)
    object SetEdit : NavigationRoute("SetEdit", R.string.nav_setedit, Icons.Filled.Description, Icons.Outlined.Description)
    object FpsManager : NavigationRoute("fps_manager", R.string.nav_fps, Icons.Filled.Speed, Icons.Outlined.Speed)
    object Language : NavigationRoute("language_screen", R.string.pref_language, Icons.Filled.Translate, Icons.Outlined.Translate)
    object ProcessManager : NavigationRoute("process_manager", R.string.nav_process, Icons.Filled.Assessment, Icons.Outlined.Assessment)
    object KernelFlasher : NavigationRoute("kernel_flasher", R.string.nav_flasher, Icons.Filled.SystemUpdate, Icons.Outlined.SystemUpdate)
    object ActivityLauncher : NavigationRoute("activity_launcher", R.string.nav_launcher, Icons.Filled.Apps, Icons.Outlined.Apps)
    object KsuWebUI : NavigationRoute("ksu_web", R.string.nav_ksuweb, Icons.Filled.Public, Icons.Outlined.Public)
    object Adreno : NavigationRoute("adreno_screen", R.string.nav_gpu, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    object Mtk : NavigationRoute("mtk_screen", R.string.nav_gpu, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    object GenericGpu : NavigationRoute("generic_gpu_screen", R.string.nav_gpu, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq)
    
    object LogsView : NavigationRoute("logs_view_screen", R.string.sect_general, Icons.Rounded.Description, Icons.Outlined.Description)

    // Route untuk System Tools
    object DebloatFreeze : NavigationRoute("debloat_freeze_screen", R.string.sect_general, Icons.Filled.AppBlocking, Icons.Outlined.AppBlocking)
    object ThermalDevices : NavigationRoute("thermal_devices_screen", R.string.sect_general, Icons.Filled.Thermostat, Icons.Outlined.Thermostat)
    object Display : NavigationRoute("display_screen", R.string.sect_general, Icons.Filled.DisplaySettings, Icons.Outlined.DisplaySettings)
    object BatteryController : NavigationRoute("battery_controller_screen", R.string.sect_general, Icons.Filled.BatteryChargingFull, Icons.Outlined.BatteryChargingFull)
    object DozeMode : NavigationRoute("doze_mode_screen", R.string.sect_general, Icons.Filled.Bedtime, Icons.Outlined.Bedtime)
    object Dex2oat : NavigationRoute("dex2oat_screen", R.string.sect_general, Icons.Filled.Build, Icons.Outlined.Build)
    
    // Route untuk Changelogs & Open Source
    object Changelogs : NavigationRoute("changelogs_screen", R.string.sect_general, Icons.Filled.History, Icons.Outlined.History)
    object OpenSource : NavigationRoute("opensource_screen", R.string.sect_general, Icons.Filled.Code, Icons.Outlined.Code)
}

@Composable
fun ZuanKernelManagerNavHost(
    navController: NavHostController,
    hazeState: HazeState 
) {
    NavHost(navController = navController, startDestination = NavigationRoute.Overall.route) {
        
        composable(NavigationRoute.Overall.route) { OverallScreen(navController = navController, hazeState = hazeState) }
        composable(NavigationRoute.Dashboard.route) { HomeScreen(navController = navController, hazeState = hazeState) }
        composable(NavigationRoute.SoC.route) { SoCScreen(navController = navController, hazeState = hazeState) }
        composable(NavigationRoute.Settings.route) { SettingsScreen(navController = navController, hazeState = hazeState) }
        composable(NavigationRoute.About.route) { AboutScreen(navController = navController, hazeState = hazeState) }
        composable(NavigationRoute.WallpaperStyle.route) { WallpaperStyleScreen(navController = navController) }
        composable(NavigationRoute.General.route) { GeneralScreen(navController = navController) }
        composable(NavigationRoute.Terminal.route) { TerminalScreen() }
        composable(NavigationRoute.SetEdit.route) { SetEditScreen(navController = navController) }
        composable(NavigationRoute.FpsManager.route) { FpsManagerScreen(navController = navController) }
        composable(NavigationRoute.Language.route) { LanguageScreen(navController = navController) }
        
        // [FIXED] ProcessManager dengan hazeState parameter
        composable(NavigationRoute.ProcessManager.route) { 
            ProcessManagerScreen(
                navController = navController, 
                hazeState = hazeState  // Pakai hazeState (bukan backgroundHazeState)
            ) 
        }
        
        composable(NavigationRoute.KernelFlasher.route) { KernelFlasherScreen(rootNavController = navController) }
        
        composable(NavigationRoute.ActivityLauncher.route) { ActivityLauncherScreen(rootNavController = navController) }
        composable(NavigationRoute.KsuWebUI.route) { KsuWebuiScreen(navController = navController) }
        composable(NavigationRoute.Adreno.route) { AdrenoScreen(navController = navController) }
        composable(NavigationRoute.Mtk.route) { MtkScreen(navController = navController) }
        
        
        composable(NavigationRoute.LogsView.route) { LogsViewScreen() }

        // System Tools
        composable(NavigationRoute.DebloatFreeze.route) { DebloatFreezeScreen(navController = navController) }
        composable(NavigationRoute.ThermalDevices.route) { ThermalDevicesScreen(navController = navController) }
        composable(NavigationRoute.Display.route) { DisplayScreen(navController = navController) }
        composable(NavigationRoute.BatteryController.route) { BatteryControllerScreen(navController = navController) }
        composable(NavigationRoute.DozeMode.route) { DozeModeScreen(navController = navController) }
        composable(NavigationRoute.Dex2oat.route) { Dex2oatScreen(navController = navController) }
        
        // Changelogs & Open Source
        composable(NavigationRoute.Changelogs.route) { 
            ChangelogsScreen(navController = navController, hazeState = hazeState) 
        }
        composable(NavigationRoute.OpenSource.route) { 
            OpenSourceScreen(navController = navController, hazeState = hazeState) 
        }
    }
}
