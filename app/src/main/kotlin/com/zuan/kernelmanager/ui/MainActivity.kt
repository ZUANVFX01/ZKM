/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.navigation.LiquidNavigationBar
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.settings.SettingsPreference
import com.zuan.kernelmanager.ui.settings.availableColors
import com.zuan.kernelmanager.ui.settings.WeatherEffect
import com.zuan.kernelmanager.ui.settings.NavStyle
import com.zuan.kernelmanager.ui.theme.ThemeMode
import com.zuan.kernelmanager.ui.theme.ZuanKernelManagerTheme
import com.zuan.kernelmanager.utils.ContextUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.exitProcess

import com.zuan.kernelmanager.ui.navigation.StandardNavigationBar
import com.zuan.kernelmanager.ui.navigation.MainSwipeScreen
import com.zuan.kernelmanager.ui.navigation.ActiveDialog
import com.zuan.kernelmanager.ui.navigation.ToolsMenuContent
import com.zuan.kernelmanager.ui.navigation.PowerMenuContent
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay

import com.zuan.kernelmanager.ui.update.UpdateChecker
import com.zuan.kernelmanager.ui.update.UpdateInfo
import com.zuan.kernelmanager.ui.update.UpdateDialog

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

import com.zuan.kernelmanager.ui.home.menu.DebloatFreezeScreen
import com.zuan.kernelmanager.ui.home.menu.ThermalDevicesScreen
import com.zuan.kernelmanager.ui.home.menu.DisplayScreen
import com.zuan.kernelmanager.ui.home.menu.BatteryControllerScreen
import com.zuan.kernelmanager.ui.home.menu.DozeModeScreen
import com.zuan.kernelmanager.ui.home.menu.Dex2oatScreen

import com.zuan.kernelmanager.ui.IntroScreen

class MainActivity : AppCompatActivity() {
    private var isRoot = false
    private var showRootDialog by mutableStateOf(false)

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ -> }

    private val checkRoot = Runnable {
        Shell.getShell { shell ->
            isRoot = shell.isRoot
            if (!isRoot) { showRootDialog = true }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtils.updateBaseContext(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
            val sharedPrefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            val dpi = sharedPrefs.getInt("app_custom_dpi", 0)
            if (dpi != 0) { overrideConfiguration.densityDpi = dpi }
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val prefs = SettingsPreference.getInstance(this)
        val savedDpi = prefs.appDpi.value
        val savedLanguage = prefs.currentLanguageCode.value
        
        if (savedLanguage != "system") {
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val targetLocale = when (savedLanguage) {
                "zh-CN" -> "zh-CN"
                "zh-SG" -> "zh-SG"  
                "zh-TW" -> "zh-TW"
                "zh-HK" -> "zh-HK"
                "zh-MO" -> "zh-MO"
                "ru" -> "ru-RU"
                "uk" -> "uk-UA"
                "be" -> "be-BY"
                "kk" -> "kk-KZ"
                "ro-MD" -> "ro-MD"
                "in" -> "in-ID"
                "en" -> "en-US"
                else -> savedLanguage
            }
            
            if (currentLocales.toLanguageTags() != targetLocale) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLocale))
            }
        }

        splashScreen.setKeepOnScreenCondition { false }
        
        enableEdgeToEdge()
        Thread(checkRoot).start()
        askNotificationPermission()

        // ====== IPC ROOT DAEMON START ======
        com.zuan.kernelmanager.utils.RootIpcManager.bind(this)
        // ===================================

        setContent {
            val savedDpiState by prefs.appDpi.collectAsState()
            val themeMode by prefs.themeMode.collectAsState()
            val hasCompletedIntro by prefs.hasCompletedIntro.collectAsState()
            
            val isDynamicColor by prefs.isDynamicColor.collectAsState()
            val themeColorName by prefs.themeColorName.collectAsState()
            val isCustomColor by prefs.isCustomColor.collectAsState()
            val customPrimary by prefs.customPrimaryColor.collectAsState()
            val customSecondary by prefs.customSecondaryColor.collectAsState()
            val customTertiary by prefs.customTertiaryColor.collectAsState()
            
            var showCustomSplash by remember { mutableStateOf(true) }
            var showIntro by remember { mutableStateOf(false) }

            val currentDensity = LocalDensity.current
            val targetDpi = if (savedDpiState != 0) savedDpiState else savedDpi
            val appDensity = remember(targetDpi) {
                    if (targetDpi > 0) { Density(targetDpi.toFloat() / 160f, currentDensity.fontScale) } 
                    else { currentDensity }
                }
            
            val useDarkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
                }

            CompositionLocalProvider(LocalDensity provides appDensity) {
                val themeColors = remember(isDynamicColor, themeColorName, isCustomColor, customPrimary, customSecondary, customTertiary) {
                    when {
                        isCustomColor -> Triple(
                            Color(customPrimary),
                            Color(customSecondary),
                            Color(customTertiary)
                        )
                        isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> null
                        else -> {
                            val selectedColor = availableColors.find { it.name == themeColorName } ?: availableColors[1]
                            Triple(selectedColor.primary, selectedColor.secondary, selectedColor.tertiary)
                        }
                    }
                }
                
                ZuanKernelManagerTheme(
                    darkTheme = useDarkTheme,
                    customPrimary = themeColors?.first,
                    customSecondary = themeColors?.second,
                    customTertiary = themeColors?.third
                ) {
                    Crossfade(
                        targetState = when {
                            showCustomSplash -> "splash"
                            !hasCompletedIntro -> "intro"
                            else -> "main"
                        },
                        animationSpec = tween(durationMillis = 800),
                        label = "AppFlowTransition"
                    ) { state ->
                        when (state) {
                            "splash" -> {
                                Zuan3DCustomSplashScreen(
                                    onSplashFinished = { showCustomSplash = false }
                                )
                            }
                            "intro" -> {
                                IntroScreen(
                                    onIntroCompleted = { },
                                    onDeclined = { exitProcess(0) }
                                )
                            }
                            "main" -> {
                                ZuanKernelManagerApp(showRootDialog = showRootDialog)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // ====== MATIKAN IPC ROOT DAEMON SAAT APP DITUTUP ======
    override fun onDestroy() {
        super.onDestroy()
        com.zuan.kernelmanager.utils.RootIpcManager.unbind()
    }
    // ======================================================

    companion object {
        init {
            @Suppress("DEPRECATION")
            if (Shell.getCachedShell() == null) {
                Shell.setDefaultBuilder(
                    Shell.Builder.create()
                        .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
                        .setTimeout(20),
                )
            }
        }
    }
    
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun ZuanKernelManagerApp(showRootDialog: Boolean = false) {
    val context = LocalContext.current
    
    val backgroundHazeState = remember { HazeState() }
    val contentHazeState = remember { HazeState() }
    
    val navController = rememberNavController()

    val prefs = remember { SettingsPreference.getInstance(context) }
    val isCustomBg by prefs.isCustomBackground.collectAsState()
    val bgUriString by prefs.backgroundImageUri.collectAsState()
    val isVideo by prefs.isVideoWallpaper.collectAsState()
    val isBgBlur by prefs.isBgBlur.collectAsState()
    val bgContrast by prefs.bgContrast.collectAsState()
    
    val weatherEffect by prefs.weatherEffect.collectAsState()
    val weatherIntensity by prefs.weatherIntensity.collectAsState()
    
    val bgSaturation by prefs.bgSaturation.collectAsState()
    val blurStrength by prefs.blurStrength.collectAsState()
    
    val isDynamicColor by prefs.isDynamicColor.collectAsState()
    val themeColorName by prefs.themeColorName.collectAsState()
    val isCustomColor by prefs.isCustomColor.collectAsState()
    val customPrimary by prefs.customPrimaryColor.collectAsState()
    val customSecondary by prefs.customSecondaryColor.collectAsState()
    val customTertiary by prefs.customTertiaryColor.collectAsState()
    
    val navStyle by prefs.navStyle.collectAsState()
    val navLabelState by prefs.navLabelState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    var activeDialog by remember { mutableStateOf(ActiveDialog.NONE) }

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var hasCheckedUpdate by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!hasCheckedUpdate) {
            hasCheckedUpdate = true
            delay(1500)
            val info = UpdateChecker.checkForUpdate(context)
            if (info != null) {
                updateInfo = info
                showUpdateDialog = true
            }
        }
    }

    val showBottomBar = (currentRoute in listOf(
        NavigationRoute.Overall.route,
        NavigationRoute.Dashboard.route,
        NavigationRoute.SoC.route,
        NavigationRoute.About.route,
        NavigationRoute.MainSwipeContainer.route
    )) && (navStyle != NavStyle.MODERN_TABS)

    val bottomBarHeight = 150.dp
    val bottomBarHeightPx = with(LocalDensity.current) { bottomBarHeight.toPx() }
    
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }

    val isAppInDarkMode = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val mainAppBackgroundColor = if (isAppInDarkMode) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.background
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = Velocity.Zero 
        }
    }

    val startDestination = if (navStyle == NavStyle.MODERN_TABS) {
        NavigationRoute.MainSwipeContainer.route
    } else {
        NavigationRoute.Overall.route
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainAppBackgroundColor)
            .nestedScroll(nestedScrollConnection)
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = backgroundHazeState, zIndex = 0f)
                .hazeSource(state = contentHazeState, zIndex = 0f)
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
            } else {
                Box(modifier = Modifier.fillMaxSize().background(mainAppBackgroundColor))
            }
            
            WeatherEffectOverlay(
                effect = weatherEffect,
                intensity = weatherIntensity,
                modifier = Modifier.fillMaxSize()
            )
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = contentHazeState, zIndex = 1f),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            
            val contentPadding = if (!showBottomBar) {
                PaddingValues(bottom = 0.dp)
            } else {
                innerPadding
            }

            Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(NavigationRoute.MainSwipeContainer.route) {
                        MainSwipeScreen(
                            navController = navController,
                            hazeState = backgroundHazeState,
                            onOpenDialog = { dialog -> activeDialog = dialog }
                        )
                    }
                    
                    composable(NavigationRoute.Overall.route) { OverallScreen(navController = navController, hazeState = backgroundHazeState) }
                    composable(NavigationRoute.Dashboard.route) { HomeScreen(navController = navController, hazeState = backgroundHazeState) }
                    composable(NavigationRoute.SoC.route) { SoCScreen(navController = navController, hazeState = backgroundHazeState) }
                    composable(NavigationRoute.Settings.route) { SettingsScreen(navController = navController, hazeState = backgroundHazeState) }
                    composable(NavigationRoute.About.route) { AboutScreen(navController = navController, hazeState = backgroundHazeState) }
                    composable(NavigationRoute.WallpaperStyle.route) { WallpaperStyleScreen(navController = navController) }
                    composable(NavigationRoute.General.route) { GeneralScreen(navController = navController) }
                    composable(NavigationRoute.Terminal.route) { TerminalScreen() }
                    composable(NavigationRoute.SetEdit.route) { SetEditScreen(navController = navController) }
                    composable(NavigationRoute.FpsManager.route) { FpsManagerScreen(navController = navController) }
                    composable(NavigationRoute.Language.route) { LanguageScreen(navController = navController) }
                    composable(NavigationRoute.ProcessManager.route) { 
                        ProcessManagerScreen(
                            navController = navController,
                            hazeState = backgroundHazeState
                        ) 
                    }

                    composable(NavigationRoute.KernelFlasher.route) { KernelFlasherScreen(rootNavController = navController) }
                    
                    composable(NavigationRoute.ActivityLauncher.route) { ActivityLauncherScreen(rootNavController = navController) }
                    composable(NavigationRoute.KsuWebUI.route) { KsuWebuiScreen(navController = navController) }
                    
                    // GPU Screens - pakai package baru
                    composable(NavigationRoute.Adreno.route) { AdrenoScreen(navController = navController) }
                    composable(NavigationRoute.Mtk.route) { MtkScreen(navController = navController) }
                    
                    
                    composable(NavigationRoute.LogsView.route) { LogsViewScreen() }
                    
                    composable(NavigationRoute.DebloatFreeze.route) { DebloatFreezeScreen(navController = navController) }
                    composable(NavigationRoute.ThermalDevices.route) { ThermalDevicesScreen(navController = navController) }
                    composable(NavigationRoute.Display.route) { DisplayScreen(navController = navController) }
                    composable(NavigationRoute.BatteryController.route) { BatteryControllerScreen(navController = navController) }
                    composable(NavigationRoute.DozeMode.route) { DozeModeScreen(navController = navController) }
                    composable(NavigationRoute.Dex2oat.route) { Dex2oatScreen(navController = navController) }
                    
                    composable(NavigationRoute.Changelogs.route) { 
                        ChangelogsScreen(navController = navController, hazeState = backgroundHazeState) 
                    }
                    
                    composable(NavigationRoute.OpenSource.route) { 
                        OpenSourceScreen(navController = navController, hazeState = backgroundHazeState) 
                    }
                }
            }
        }
        
        if (activeDialog != ActiveDialog.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
                    .hazeEffect(
                        state = contentHazeState,
                        style = HazeStyle(blurRadius = 25.dp, tint = HazeTint(Color.Transparent))
                    )
                    .background(Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { activeDialog = ActiveDialog.NONE },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.clickable(enabled = false) {}) {
                    when (activeDialog) {
                        ActiveDialog.TOOLS -> ToolsMenuContent(onDismiss = { activeDialog = ActiveDialog.NONE })
                        ActiveDialog.POWER -> PowerMenuContent(onDismiss = { activeDialog = ActiveDialog.NONE })
                        else -> {}
                    }
                }
            }
        }

        if (showUpdateDialog && updateInfo != null) {
            UpdateDialog(
                updateInfo = updateInfo!!,
                hazeState = contentHazeState,
                onDismiss = { showUpdateDialog = false },
                onUpdateClick = {
                    val downloadId = UpdateChecker.downloadUpdate(context, updateInfo!!)
                    UpdateChecker.registerInstallReceiver(context, downloadId)
                }
            )
        }

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(x = 0, y = bottomBarOffsetHeightPx.roundToInt()) }
            ) {
                when (navStyle) {
                    NavStyle.LIQUID_CAPSULE -> {
                        LiquidNavigationBar(navController = navController, hazeState = contentHazeState)
                    }
                    NavStyle.CLASSIC_MATERIAL -> {
                        StandardNavigationBar(
                            navController = navController,
                            hazeState = contentHazeState,
                            labelState = navLabelState
                        )
                    }
                    else -> {}
                }
            }
        }

        AnimatedVisibility(visible = showRootDialog, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .hazeEffect(state = contentHazeState, style = HazeStyle(blurRadius = 20.dp, tint = HazeTint(Color.Black.copy(alpha = 0.4f))))
                    .background(Color.Black.copy(alpha = 0.1f))
                    .clickable(enabled = false) {}
            )
        }
        AnimatedVisibility(
            visible = showRootDialog,
            enter = scaleIn(initialScale = 0.9f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(24.dp)) {
                GlassRootDialog()
            }
        }
    }
}

@Composable
fun GlassRootDialog() {
    val dialogShape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier.widthIn(max = 360.dp)
            .clip(dialogShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = dialogShape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_app),
            contentDescription = "App Logo",
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Root Access Required",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Zuan Kernel Manager needs root access to modify kernel settings. Please root your device to continue.",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { exitProcess(0) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text(text = "Exit Application", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}