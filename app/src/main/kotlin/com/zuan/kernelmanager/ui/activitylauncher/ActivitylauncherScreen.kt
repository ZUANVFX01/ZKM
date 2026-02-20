/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.activitylauncher

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- DATA CLASS (Lightweight - No Drawable) ---

data class AppData(
    val label: String,
    val packageName: String,
    val versionName: String,
    val versionCode: String,
    val isSystemApp: Boolean,
    val activityCount: Int
)

data class ActivityItem(
    val name: String,
    val label: String,
    val isExported: Boolean
)

enum class SortOption { NAME_ASC, NAME_DESC }
enum class FilterOption { ALL, SYSTEM, USER }

/**
 * Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan generateThemedBackgroundColor di AboutScreen
 * Hijau → #F8FBF2 (hijau sangat soft)
 * Ungu → #FFF7FC (ungu sangat soft) 
 * Pink → #FFF5F8 (pink sangat soft)
 */
@Composable
fun generateActivityLauncherBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
        // Primary dikurangi intensitasnya 92%, lalu di-blend dengan dark base
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
        // Light Mode: Background putih dengan "tint" dari primary
        // Formula: 97% White + 3% Primary = Warna soft yang tetap berhue sama
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        // Buat warna soft: hampir putih tapi ada sedikit nuansa primary
        // Contoh: Hijau #D4E8CF → #F8FBF2 (248, 251, 242) sangat soft
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),   // 97% white + 3% primary
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        // Card color: Putih bersih untuk card (Material 3 standard)
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

// --- ENTRY POINT ---

@Composable
fun ActivityLauncherScreen(
    rootNavController: NavController,
    viewModel: ActivityLauncherViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val internalNavController = rememberNavController()

    NavHost(navController = internalNavController, startDestination = "app_list") {
        composable("app_list") {
            AppListScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                onBack = { rootNavController.popBackStack() },
                onAppClick = { app ->
                    internalNavController.navigate("app_detail/${app.packageName}")
                }
            )
        }

        composable("app_detail/{packageName}") { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName") ?: ""
            AppDetailScreen(
                packageName = packageName,
                settingsViewModel = settingsViewModel,
                onBack = { internalNavController.popBackStack() }
            )
        }
    }
}

// --- HELPER: ASYNC ICON LOADER ---
@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val icon by produceState<Drawable?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        }
    }

    if (icon != null) {
        Image(
            bitmap = icon!!.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Android, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(4.dp).fillMaxSize()
            )
        }
    }
}

// --- SCREEN 1: APP LIST ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: ActivityLauncherViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    onAppClick: (AppData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    
    val allApps by viewModel.allApps.collectAsState()
    val isInitialLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadProgress.collectAsState()

    val isAndroid12 = Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2
    val hazeState = rememberHazeState()
    
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(localDensity) { headerHeightPx.toDp() }

    var displayedApps by remember { mutableStateOf<List<AppData>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    var filterOption by remember { mutableStateOf(FilterOption.ALL) }
    
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // State untuk Floating Inspector Button
    var isInspectorActive by remember { mutableStateOf(FloatingActivityService.isRunning) }

    // --- WALLPAPER STYLE STATES ---
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    // Theme Colors
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // [FIX] Ambil themeMode dari ViewModel
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    // FIX: Dynamic color resolution seperti screen lain
    val effectivePrimary = if (isDynamic) {
        MaterialTheme.colorScheme.primary
    } else if (isCustomColor) {
        Color(customPrimary)
    } else {
        themeColorName.primary
    }

    // [FIX] Hitung isDark berdasarkan themeMode, bukan langsung dari sistem
    val isDark = when (themeMode) {
        com.zuan.kernelmanager.ui.theme.ThemeMode.LIGHT -> false
        com.zuan.kernelmanager.ui.theme.ThemeMode.DARK -> true
        com.zuan.kernelmanager.ui.theme.ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // SESUAI ABOUTSCREEN: Generate tinted background untuk semua kasus SYSTEM
    val (themeBackground, themeCardColorBase) = if (bgType == BgType.SYSTEM) {
        generateActivityLauncherBackgroundColors(isDark, effectivePrimary)
    } else {
        Pair(Color.Transparent, Color(0xFFFFFFFF))
    }
    
    val mainBackgroundColor = if (bgType != BgType.SYSTEM) {
        Color.Transparent
    } else {
        themeBackground
    }
    
    // Card base color dari generate, bukan dari MaterialTheme langsung
    val themeCardColor = themeCardColorBase
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    // FIX: Card color calculation lebih bersih
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
    }
    
    // FIX: Content color mengikuti theme
    val headerTextColor = when {
        isCustomBg -> Color.White
        isDynamic -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val headerSubColor = when {
        isCustomBg -> Color.White.copy(0.7f)
        isDynamic -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val cardTextColor = when {
        isGlassActive || (isCustomBg && !isGlassActive) -> Color.White
        isDynamic -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val cardSubTextColor = when {
        isGlassActive || (isCustomBg && !isGlassActive) -> Color.White.copy(0.7f)
        isDynamic -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(allApps, searchQuery, sortOption, filterOption) {
        withContext(Dispatchers.Default) {
            var result = allApps
            result = when (filterOption) {
                FilterOption.ALL -> result
                FilterOption.SYSTEM -> result.filter { it.isSystemApp }
                FilterOption.USER -> result.filter { !it.isSystemApp }
            }
            if (searchQuery.isNotEmpty()) {
                result = result.filter {
                    it.label.contains(searchQuery, ignoreCase = true) || 
                    it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
            result = when (sortOption) {
                SortOption.NAME_ASC -> result.sortedBy { it.label.lowercase() }
                SortOption.NAME_DESC -> result.sortedByDescending { it.label.lowercase() }
            }
            displayedApps = result
        }
    }

    fun onRefresh() {
        scope.launch {
            isRefreshing = true
            viewModel.loadApps(forceRefresh = true)
            isRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        
        // --- BACKGROUND LAYER ---
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

            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f)) {
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
        } else {
            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f))
        }
        
        // --- WEATHER EFFECT OVERLAY ---
        WeatherEffectOverlay(
            effect = weatherEffect,
            intensity = weatherIntensity,
            modifier = Modifier.fillMaxSize()
        )

        // --- CONTENT LAYER ---
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0,0,0,0)
        ) { _ -> 
            
            Box(modifier = Modifier.fillMaxSize()) {
                
                // LAYER 1: CONTENT (Source Haze)
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onRefresh() },
                    state = pullRefreshState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isInitialLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularWavyProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(52.dp), color = effectivePrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = "Loading Apps...", style = MaterialTheme.typography.labelLarge, color = headerTextColor)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = headerHeightDp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp, 
                                bottom = 100.dp
                            )
                        ) {
                            item {
                                ActivityStatsDashboard(
                                    allApps = allApps,
                                    currentFilter = filterOption,
                                    onFilterChange = { filterOption = it },
                                    isGlassActive = isGlassActive,
                                    hazeState = hazeState,
                                    cardColor = finalCardColor,
                                    textColor = cardTextColor,
                                    subTextColor = cardSubTextColor,
                                    primaryColor = effectivePrimary
                                )
                            }

                            items(displayedApps, key = { it.packageName }) { app ->
                                AppListItem(
                                    app = app, 
                                    onClick = { onAppClick(app) },
                                    isGlassActive = isGlassActive,
                                    hazeState = hazeState,
                                    cardColor = finalCardColor,
                                    textColor = cardTextColor,
                                    subTextColor = cardSubTextColor,
                                    primaryColor = effectivePrimary
                                )
                                HorizontalDivider(color = if (isCustomBg) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                            }
                            
                            if (displayedApps.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("Tidak ada aplikasi ditemukan", color = headerSubColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // LAYER 2: FLOATING HEADER
                val headerHazeStyle = if (isCustomBg) {
                    HazeStyle(
                        backgroundColor = Color.Black.copy(alpha = 0.2f),
                        blurRadius = 24.dp,
                        noiseFactor = 0.1f,
                        tints = emptyList()
                    )
                } else {
                    HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        tint = null, 
                        blurRadius = 24.dp,
                        noiseFactor = 0.05f
                    )
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            headerHeightPx = coordinates.size.height
                        }
                        .hazeEffect(
                            state = hazeState,
                            style = headerHazeStyle
                        ) {
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0.4f,
                                preferPerformance = true
                            )
                            if (isAndroid12) forceInvalidateOnPreDraw = true
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onBack) { 
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = headerTextColor) 
                            }
                            Text("Activity Launcher", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = headerTextColor)
                            
                            Row {
                                IconButton(onClick = { 
                                    val newState = !isInspectorActive
                                    if (newState) {
                                        if (!Settings.canDrawOverlays(context)) {
                                            Toast.makeText(context, "Permission Required", Toast.LENGTH_SHORT).show()
                                            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                                        } else {
                                            val intent = Intent(context, FloatingActivityService::class.java)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                context.startForegroundService(intent)
                                            } else {
                                                context.startService(intent)
                                            }
                                            isInspectorActive = true
                                            Toast.makeText(context, "Inspector Started", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        context.stopService(Intent(context, FloatingActivityService::class.java))
                                        isInspectorActive = false
                                    }
                                }) { 
                                    Icon(
                                        imageVector = Icons.Default.Layers, 
                                        contentDescription = "Toggle Inspector",
                                        tint = if (isInspectorActive) effectivePrimary else headerTextColor
                                    ) 
                                }
                                
                                IconButton(onClick = { showBottomSheet = true }) { 
                                    Icon(Icons.Default.MoreVert, "Options", tint = headerTextColor) 
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                            placeholder = { Text("Cari aplikasi...", color = headerSubColor) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = headerSubColor) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.extraLarge, 
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = if (isCustomBg) Color.White.copy(0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                unfocusedContainerColor = if (isCustomBg) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = headerTextColor,
                                unfocusedTextColor = headerTextColor,
                                cursorColor = effectivePrimary
                            )
                        )
                        HorizontalDivider(color = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }

                // LAYER 3: BOTTOM SHEET OVERLAY
                AnimatedVisibility(
                    visible = showBottomSheet,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize().zIndex(2f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.regular()
                            ) {
                                if (isAndroid12) forceInvalidateOnPreDraw = true
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showBottomSheet = false }
                    )
                }
            }
        }
    }
    
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Transparent
        ) {
            BottomSheetContent(currentSort = sortOption, currentFilter = filterOption, onSortSelected = { sortOption = it }, onFilterSelected = { filterOption = it })
        }
    }
}

// --- SCREEN 2: APP DETAIL (ACTIVITIES) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    packageName: String, 
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isAndroid12 = Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2
    
    val hazeState = rememberHazeState()
    
    var appData by remember { mutableStateOf<AppData?>(null) }
    var activities by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    var appIcon by remember { mutableStateOf<Drawable?>(null) }
    
    var selectedActivity by remember { mutableStateOf<ActivityItem?>(null) }
    val isDialogVisible = selectedActivity != null

    // --- WALLPAPER STYLE STATES ---
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    // Theme Colors
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // [FIX] Ambil themeMode dari ViewModel
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    // FIX: Dynamic color resolution seperti screen lain
    val effectivePrimary = if (isDynamic) {
        MaterialTheme.colorScheme.primary
    } else if (isCustomColor) {
        Color(customPrimary)
    } else {
        themeColorName.primary
    }

    // [FIX] Hitung isDark berdasarkan themeMode, bukan langsung dari sistem
    val isDark = when (themeMode) {
        com.zuan.kernelmanager.ui.theme.ThemeMode.LIGHT -> false
        com.zuan.kernelmanager.ui.theme.ThemeMode.DARK -> true
        com.zuan.kernelmanager.ui.theme.ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // SESUAI ABOUTSCREEN: Generate tinted background untuk semua kasus SYSTEM
    val (themeBackground, themeCardColorBase) = if (bgType == BgType.SYSTEM) {
        generateActivityLauncherBackgroundColors(isDark, effectivePrimary)
    } else {
        Pair(Color.Transparent, Color(0xFFFFFFFF))
    }
    
    val mainBackgroundColor = if (bgType != BgType.SYSTEM) {
        Color.Transparent
    } else {
        themeBackground
    }
    
    // Card base color dari generate
    val themeCardColor = themeCardColorBase
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    // FIX: Card color calculation lebih bersih
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
    }
    
    // FIX: Content color mengikuti theme
    val headerTextColor = when {
        isCustomBg -> Color.White
        isDynamic -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val textColor = when {
        isGlassActive || isCustomBg -> Color.White
        isDynamic -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val subTextColor = when {
        isGlassActive || isCustomBg -> Color.White.copy(0.7f)
        isDynamic -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val packInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                val appInfo = packInfo.applicationInfo
                
                appIcon = pm.getApplicationIcon(packageName)

                if (appInfo != null) {
                    val vName = packInfo.versionName ?: "Unknown"
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packInfo.longVersionCode.toString() else packInfo.versionCode.toString()
                    
                    appData = AppData(
                        label = pm.getApplicationLabel(appInfo).toString(),
                        packageName = packInfo.packageName,
                        versionName = vName,
                        versionCode = vCode,
                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        activityCount = packInfo.activities?.size ?: 0
                    )
                    val rawActivities = packInfo.activities ?: emptyArray()
                    activities = rawActivities.map { act ->
                        ActivityItem(act.name, act.loadLabel(pm).toString(), act.exported)
                    }.sortedBy { it.label }
                }
            } catch (e: Exception) { e.printStackTrace() }
            isLoading = false
        }
    }

    fun launchActivity(actName: String) {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.component = ComponentName(packageName, actName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: SecurityException) { Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show() }
          catch (e: Exception) { Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show() }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        
        // --- BACKGROUND LAYER ---
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

            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f)) {
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
        } else {
            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f))
        }
        
        // --- WEATHER EFFECT OVERLAY ---
        WeatherEffectOverlay(
            effect = weatherEffect,
            intensity = weatherIntensity,
            modifier = Modifier.fillMaxSize()
        )

        // --- CONTENT LAYER ---
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0,0,0,0)
        ) { _ ->
            
            Box(modifier = Modifier.fillMaxSize()) {
                
                // LAYER 1: KONTEN LIST
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(52.dp), color = effectivePrimary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 80.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                            bottom = 16.dp
                        )
                    ) {
                        item {
                            appData?.let { app ->
                                GlassCard(
                                    isGlassActive = isGlassActive,
                                    hazeState = hazeState,
                                    cardColor = finalCardColor,
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (appIcon != null) {
                                            Image(bitmap = appIcon!!.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(64.dp))
                                        } else {
                                            Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(64.dp), tint = textColor)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(app.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                                            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = effectivePrimary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Versi: ${app.versionName} (${app.versionCode})", style = MaterialTheme.typography.bodySmall, color = subTextColor)
                                            Text("Total Activity: ${app.activityCount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = subTextColor)
                                        }
                                    }
                                }
                            }
                        }
                        
                        item { 
                            HorizontalDivider(color = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant) 
                        }
                        
                        item { 
                            Text(
                                "Daftar Aktivitas", 
                                modifier = Modifier.padding(16.dp), 
                                style = MaterialTheme.typography.titleSmall, 
                                color = effectivePrimary
                            ) 
                        }
                        
                        items(activities) { activity ->
                            GlassListItem(
                                isGlassActive = isGlassActive,
                                hazeState = hazeState,
                                cardColor = finalCardColor,
                                modifier = Modifier.clickable { selectedActivity = activity },
                                headlineContent = { 
                                    Text(
                                        text = activity.label, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis, 
                                        style = MaterialTheme.typography.bodyMedium, 
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    ) 
                                },
                                supportingContent = { 
                                    Text(
                                        text = activity.name, 
                                        maxLines = 1, 
                                        overflow = TextOverflow.Ellipsis, 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = subTextColor
                                    ) 
                                },
                                leadingContent = { 
                                    if(appIcon != null) {
                                        Image(bitmap = appIcon!!.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(32.dp))
                                    } else {
                                        Icon(Icons.Default.Android, null, modifier = Modifier.size(32.dp), tint = subTextColor)
                                    }
                                },
                                trailingContent = { 
                                    if (!activity.isExported) { 
                                        Icon(Icons.Default.Warning, contentDescription = "Not Exported", tint = subTextColor, modifier = Modifier.size(16.dp)) 
                                    } 
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = if (isCustomBg) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
                
                // LAYER 2: TOP APP BAR (PROGRESSIVE BLUR)
                val headerHazeStyle = if (isCustomBg) {
                    HazeStyle(
                        backgroundColor = Color.Black.copy(alpha = 0.2f),
                        blurRadius = 24.dp,
                        noiseFactor = 0.1f,
                        tints = emptyList()
                    )
                } else {
                    HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        tint = null, 
                        blurRadius = 24.dp,
                        noiseFactor = 0.05f
                    )
                }
                
                TopAppBar(
                    title = { Text(appData?.label ?: "Detail Aplikasi", color = headerTextColor) },
                    navigationIcon = { 
                        IconButton(onClick = onBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = headerTextColor) 
                        } 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, 
                        scrolledContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .hazeEffect(
                            state = hazeState, 
                            style = headerHazeStyle
                        ) {
                            progressive = HazeProgressive.verticalGradient(
                                startIntensity = 1f,
                                endIntensity = 0.4f,
                                preferPerformance = true
                            )
                            if (isAndroid12) forceInvalidateOnPreDraw = true
                        }
                )
                
                // LAYER 3: DIALOG OVERLAY
                AnimatedVisibility(
                    visible = isDialogVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize().zIndex(2f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeEffect(
                                state = hazeState, 
                                style = HazeMaterials.regular()
                            ) {
                                if (isAndroid12) forceInvalidateOnPreDraw = true
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedActivity = null }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val act = selectedActivity
                        val app = appData
                        
                        if (act != null && app != null) {
                            Card(
                                modifier = Modifier
                                    .widthIn(min = 280.dp, max = 560.dp)
                                    .padding(24.dp)
                                    .clickable(enabled = false) {},
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (appIcon != null) {
                                        Image(bitmap = appIcon!!.toBitmap().asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
                                    } else {
                                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(48.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Launch Activity?", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Column(
                                        modifier = Modifier
                                            .heightIn(max = 300.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        InfoRow(label = "Activity Name", value = act.label)
                                        InfoRow(label = "Class", value = act.name)
                                        InfoRow(label = "Package APP", value = app.packageName)
                                    }
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { launchActivity(act.name); selectedActivity = null },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = effectivePrimary)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp)); Text("Launch")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FilledTonalButton(
                                        onClick = {
                                            val copyText = "${app.packageName}/${act.name}"
                                            clipboardManager.setText(AnnotatedString(copyText))
                                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                            selectedActivity = null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("Copy Info")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    BackHandler(enabled = isDialogVisible) { selectedActivity = null }
}

// --- GLASSMORPHISM COMPONENTS ---

@Composable
fun GlassCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
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
                shape = shape
            )
    } else {
        Modifier.clip(shape)
    }

    val containerColor = if (isGlassActive) Color.Transparent else cardColor

    Card(
        modifier = modifier.then(glassModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        content()
    }
}

@Composable
fun GlassListItem(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.4f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.08f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
    }

    val containerColor = if (isGlassActive) Color.Transparent else cardColor

    ListItem(
        modifier = modifier.then(glassModifier),
        colors = ListItemDefaults.colors(containerColor = containerColor),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent
    )
}

// --- UPDATED COMPONENTS WITH THEMING ---

@Composable
fun ActivityStatsDashboard(
    allApps: List<AppData>,
    currentFilter: FilterOption,
    onFilterChange: (FilterOption) -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val totalApps = allApps.size
    val systemApps = allApps.count { it.isSystemApp }
    val userApps = allApps.count { !it.isSystemApp }
    
    val userColor = primaryColor
    val systemColor = if (isGlassActive) Color.White.copy(0.3f) else MaterialTheme.colorScheme.surfaceContainerHighest 

    val userProgress = if (totalApps > 0) userApps.toFloat() / totalApps.toFloat() else 0f
    
    val animatedProgress by animateFloatAsState(
        targetValue = userProgress,
        animationSpec = spring(
            dampingRatio = 0.6f, 
            stiffness = Spring.StiffnessLow
        ),
        label = "WavyProgress"
    )

    GlassCard(
        isGlassActive = isGlassActive,
        hazeState = hazeState,
        cardColor = cardColor,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Apps Installed ($totalApps)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                
                Row(modifier = Modifier.height(36.dp).clip(RoundedCornerShape(50)).background(if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh).border(1.dp, if (isGlassActive) Color.White.copy(0.3f) else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50)), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onFilterChange(if (currentFilter == FilterOption.USER) FilterOption.ALL else FilterOption.USER) }, modifier = Modifier.size(36.dp).background(if (currentFilter == FilterOption.USER) primaryColor else Color.Transparent, CircleShape)) {
                        Icon(Icons.Default.GridView, null, tint = if (currentFilter == FilterOption.USER) Color.White else subTextColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { onFilterChange(if (currentFilter == FilterOption.SYSTEM) FilterOption.ALL else FilterOption.SYSTEM) }, modifier = Modifier.size(36.dp).background(if (currentFilter == FilterOption.SYSTEM) primaryColor.copy(0.8f) else Color.Transparent, CircleShape)) {
                        Icon(Icons.Default.Android, null, tint = if (currentFilter == FilterOption.SYSTEM) Color.White else subTextColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    WavyDonutChart(
                        progress = animatedProgress,
                        size = 110.dp,
                        color = userColor,
                        trackColor = systemColor
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendItem(label = "User Apps", count = userApps, color = userColor, textColor = subTextColor, onClick = { onFilterChange(FilterOption.USER) })
                    LegendItem(label = "System Apps", count = systemApps, color = systemColor, textColor = subTextColor, onClick = { onFilterChange(FilterOption.SYSTEM) })
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(icon = Icons.Default.Apps, label = "Active Apps", count = totalApps.toString(), textColor = textColor, subTextColor = subTextColor, modifier = Modifier.weight(1f), onClick = { onFilterChange(FilterOption.ALL) })
                DashboardCard(icon = Icons.Default.Delete, label = "Recycle Bin", count = "0", textColor = textColor, subTextColor = subTextColor, modifier = Modifier.weight(1f), onClick = { })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WavyDonutChart(
    progress: Float,
    size: Dp,
    color: Color,
    trackColor: Color
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 16.dp.toPx() }

    CircularWavyProgressIndicator(
        progress = { progress },
        modifier = Modifier.size(size),
        color = color,
        trackColor = trackColor,
        stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        trackStroke = Stroke(width = strokeWidthPx), 
        gapSize = 0.dp, 
        amplitude = { 1.5f } 
    )
}

@Composable
fun LegendItem(label: String, count: Int, color: Color, textColor: Color, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onClick)) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color)); Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = textColor); Spacer(modifier = Modifier.weight(1f))
        Text(text = "($count)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun DashboardCard(
    icon: ImageVector, 
    label: String, 
    count: String, 
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier, 
    onClick: () -> Unit
) {
    Card(
        onClick = onClick, 
        modifier = modifier.height(80.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, subTextColor.copy(0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = textColor)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = subTextColor)
                Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AppListItem(
    app: AppData, 
    onClick: () -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    GlassListItem(
        isGlassActive = isGlassActive,
        hazeState = hazeState,
        cardColor = cardColor,
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { 
            Text(
                text = app.label, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis, 
                fontWeight = FontWeight.SemiBold,
                color = textColor
            ) 
        },
        supportingContent = {
            Column {
                Text(
                    text = app.packageName, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = primaryColor, 
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "v${app.versionName}", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    if (app.isSystemApp) { 
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {}, 
                            label = { Text("System", style = MaterialTheme.typography.labelSmall) }, 
                            modifier = Modifier.height(24.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = primaryColor.copy(0.2f),
                                labelColor = primaryColor
                            )
                        )
                    }
                }
            }
        },
        leadingContent = { 
            AppIcon(packageName = app.packageName, modifier = Modifier.size(48.dp))
        },
        trailingContent = { 
            Column(horizontalAlignment = Alignment.End) { 
                Text(
                    text = "${app.activityCount}", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = primaryColor
                )
                Text(
                    text = "Activities", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = subTextColor
                ) 
            } 
        }
    )
}

@Composable
fun BottomSheetContent(currentSort: SortOption, currentFilter: FilterOption, onSortSelected: (SortOption) -> Unit, onFilterSelected: (FilterOption) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Surface(modifier = Modifier.size(width = 32.dp, height = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.extraLarge) {} }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Pengaturan Tampilan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SortOutlinedButton(text = "Nama A-Z", icon = Icons.Default.ArrowDownward, selected = currentSort == SortOption.NAME_ASC, onClick = { onSortSelected(SortOption.NAME_ASC) }, modifier = Modifier.weight(1f))
            SortOutlinedButton(text = "Nama Z-A", icon = Icons.Default.ArrowUpward, selected = currentSort == SortOption.NAME_DESC, onClick = { onSortSelected(SortOption.NAME_DESC) }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterCard(text = "Semua", selected = currentFilter == FilterOption.ALL, onClick = { onFilterSelected(FilterOption.ALL) }, modifier = Modifier.weight(1f))
            FilterCard(text = "Sistem", selected = currentFilter == FilterOption.SYSTEM, onClick = { onFilterSelected(FilterOption.SYSTEM) }, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterCard(text = "User", selected = currentFilter == FilterOption.USER, onClick = { onFilterSelected(FilterOption.USER) }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SortOutlinedButton(text: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
    val borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline
    OutlinedButton(onClick = onClick, modifier = modifier, colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor, contentColor = contentColor), border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)) { Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(text) }
}

@Composable
fun FilterCard(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Card(modifier = modifier.fillMaxWidth().height(64.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor), shape = MaterialTheme.shapes.medium) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium) } }
}
