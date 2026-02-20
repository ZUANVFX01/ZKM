/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalFoundationApi::class
)

package com.zuan.kernelmanager.ui.setedit

import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SettingsSystemDaydream
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Enum Category
enum class PropCategory(val titleRes: Int, val command: String, val icon: ImageVector) {
    All(R.string.setedit_tab_all, "ALL", Icons.Rounded.Dns),
    Global(R.string.setedit_tab_global, "settings list global", Icons.Rounded.Public),
    Secure(R.string.setedit_tab_secure, "settings list secure", Icons.Rounded.Lock),
    System(R.string.setedit_tab_system, "settings list system", Icons.Rounded.SettingsSystemDaydream),
    Android(R.string.setedit_tab_android, "getprop", Icons.Rounded.Android)
}

data class SetEditItem(val key: String, val value: String, val sourceCategory: PropCategory)

// Data class untuk tracking history
data class EditHistoryItem(
    val id: String = System.currentTimeMillis().toString(),
    val action: EditAction,
    val item: SetEditItem,
    val oldValue: String? = null,
    val newValue: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class EditAction {
    DELETED, MODIFIED, CREATED
}

// Sensitive keywords yang perlu konfirmasi ekstra
val SENSITIVE_KEYWORDS = listOf(
    "adb_enabled", "development_settings_enabled", "install_non_market_apps",
    "enabled_input_methods", "accessibility_enabled", "location_providers_allowed",
    "assisted_gps_enabled", "mock_location", "verifier_verify_adb_installs",
    "package_verifier_enable", "backup_enabled", "auto_time", "auto_time_zone",
    "airplane_mode_on", "cell_on", "data_roaming", "usb_mass_storage_enabled"
)

fun isSensitiveItem(key: String): Boolean {
    return SENSITIVE_KEYWORDS.any { key.contains(it, ignoreCase = true) }
}

/**
 * [UPDATE] Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan AboutScreen, OverallScreen, HomeScreen & SoCScreen:
 * Light: 97% White + 3% Primary
 * Dark: Primary dikurangi 92% (×0.08), blend dengan #0A0A0A
 */
@Composable
fun generateSetEditBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
        // Primary dikurangi intensitasnya 92%, lalu di-blend dengan dark base
        val darkTintedBg = primaryColor.copy(
            red = primaryColor.red * 0.08f,
            green = primaryColor.green * 0.08f,
            blue = primaryColor.blue * 0.08f,
            alpha = 1f
        ).compositeOver(Color(0xFF0A0A0A))
        
        // Card color: lebih terang tapi tetap ada nuansa (15%)
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
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        // Card color: Putih bersih untuk card (Material 3 standard)
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun SetEditScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // --- SETTINGS COLLECTION ---
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
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // [FIX] Ambil themeMode dari ViewModel
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    // [FIX] ColorScheme dari MaterialTheme untuk dynamic color
    val colorScheme = MaterialTheme.colorScheme

    // [FIX] Logic dynamic color yang benar
    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColorName.primary
        }
    }

    // [FIX] FinalPrimary dengan fallback
    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        colorScheme.primary
    } else {
        effectivePrimary
    }

    val context = LocalContext.current
    
    // [FIX] Gunakan themeMode untuk hitung isDark, bukan langsung isSystemInDarkTheme()
    val isDark = when (themeMode) {
        com.zuan.kernelmanager.ui.theme.ThemeMode.LIGHT -> false
        com.zuan.kernelmanager.ui.theme.ThemeMode.DARK -> true
        com.zuan.kernelmanager.ui.theme.ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // [UPDATE] Gunakan isDark yang sudah dihitung dari themeMode
    val (themeBgColor, themeCardColor) = generateSetEditBackgroundColors(isDark, finalPrimary)
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    // [UPDATE] Gunakan themeCardColor untuk konsistensi
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
    }
    
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else themeBgColor

    val categories = PropCategory.values()
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val currentTabIndex = pagerState.currentPage
    val scope = rememberCoroutineScope()

    var propList by remember { mutableStateOf<List<SetEditItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<SetEditItem?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    // [NEW] History tracking state
    var editHistory by remember { mutableStateOf<List<EditHistoryItem>>(emptyList()) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSensitiveDialog by remember { mutableStateOf(false) }
    var pendingDeleteItem by remember { mutableStateOf<SetEditItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val hazeState = rememberHazeState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    
    val localDensity = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(localDensity) { headerHeightPx.toDp() }

    val isAndroid12Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // [NEW] FAB Expressive state
    var isFabExpanded by remember { mutableStateOf(false) }
    val fabRotation by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = tween(300)
    )

    // [NEW] Scroll state untuk FAB animation
    val listState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    
    // Observe scroll direction
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentOffset = listState.firstVisibleItemScrollOffset
        val isScrollingDown = currentOffset > previousScrollOffset
        
        // Only toggle if scrolled more than 50px to avoid jitter
        if (kotlin.math.abs(currentOffset - previousScrollOffset) > 50) {
            isFabVisible = !isScrollingDown || currentOffset < 100
            previousScrollOffset = currentOffset
        }
    }

    // Animation for FAB visibility
    val fabScale by animateFloatAsState(
        targetValue = if (isFabVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "fab_scale"
    )
    
    val fabAlpha by animateFloatAsState(
        targetValue = if (isFabVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "fab_alpha"
    )

    fun refreshData(index: Int) {
        scope.launch(Dispatchers.IO) {
            isLoading = true
            val currentCategory = categories[index]
            val resultList = mutableListOf<SetEditItem>()

            fun fetchAndParse(cat: PropCategory): List<SetEditItem> {
                val output = Shell.cmd(cat.command).exec().out
                return output.mapNotNull { line ->
                    if (cat == PropCategory.Android) {
                        val parts = line.split("]: [")
                        if (parts.size == 2) {
                            SetEditItem(
                                key = parts[0].replace("[", "").trim(),
                                value = parts[1].replace("]", "").trim(),
                                sourceCategory = cat
                            )
                        } else null
                    } else {
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            SetEditItem(
                                key = parts[0].trim(),
                                value = parts[1].trim(),
                                sourceCategory = cat
                            )
                        } else null
                    }
                }
            }

            if (currentCategory == PropCategory.All) {
                categories.filter { it != PropCategory.All }.forEach { cat ->
                    resultList.addAll(fetchAndParse(cat))
                }
                resultList.sortBy { it.key }
            } else {
                resultList.addAll(fetchAndParse(currentCategory))
            }

            withContext(Dispatchers.Main) {
                propList = resultList
                isLoading = false
            }
        }
    }

    LaunchedEffect(currentTabIndex) { refreshData(currentTabIndex) }

    val filteredList = remember(propList, searchQuery) {
        if (searchQuery.isEmpty()) propList else propList.filter { it.key.contains(searchQuery, ignoreCase = true) || it.value.contains(searchQuery, ignoreCase = true) }
    }

    // [NEW] Filter history untuk yang dihapus saja
    val deletedHistory = remember(editHistory) {
        editHistory.filter { it.action == EditAction.DELETED }.take(10)
    }

    val view = LocalView.current
    
    // [FIX] Gunakan isDark yang sudah dihitung dari themeMode untuk status bar
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark && !isCustomBg
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0.dp),
        floatingActionButton = {
            // [FIX] FAB lebih besar dan lebih ke atas dengan animasi scroll
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(bottom = 32.dp, end = 8.dp) // [FIX] Naikin dari 16.dp jadi 32.dp biar lebih ke atas
                    .graphicsLayer {
                        scaleX = fabScale
                        scaleY = fabScale
                        alpha = fabAlpha
                    }
            ) {
                // Expanded actions
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp), // [FIX] Increase spacing
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // History button
                        if (deletedHistory.isNotEmpty()) {
                            FloatingActionButton(
                                onClick = { 
                                    showHistorySheet = true
                                    isFabExpanded = false
                                },
                                containerColor = finalPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.size(56.dp), // [FIX] Besarin dari 48.dp jadi 56.dp
                                shape = CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(6.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        ) {
                                            Text("${deletedHistory.size}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "History",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp) // [FIX] Icon lebih besar
                                    )
                                }
                            }
                        }

                        // Add new button
                        FloatingActionButton(
                            onClick = { 
                                showAddDialog = true
                                isFabExpanded = false
                            },
                            containerColor = finalPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.size(56.dp), // [FIX] Besarin dari 48.dp jadi 56.dp
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add New",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp) // [FIX] Icon lebih besar
                            )
                        }
                    }
                }

                // Main FAB - [FIX] Ukuran 64.dp biar lebih gede
                FloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    containerColor = finalPrimary,
                    contentColor = if (finalPrimary.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.size(64.dp), // [FIX] Besarin dari default jadi 64.dp
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Menu",
                        modifier = Modifier
                            .rotate(fabRotation)
                            .size(28.dp) // [FIX] Icon lebih besar
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
            
            // Background Layer dengan Haze
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
            
            WeatherEffectOverlay(
                effect = weatherEffect,
                intensity = weatherIntensity,
                modifier = Modifier.fillMaxSize()
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = finalPrimary)
                    }
                } else {
                    // [FIX] Tambahin state = listState
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = headerHeightDp + 16.dp, 
                            bottom = 100.dp // [FIX] Tambahin padding bawah biar gak ketutupan FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { item ->
                            SetEditItemCard(
                                item = item, 
                                onClick = { selectedItem = item; showBottomSheet = true },
                                isGlassActive = isGlassActive,
                                hazeState = hazeState,
                                cardColor = finalCardColor,
                                primaryColor = finalPrimary,
                                isSensitive = isSensitiveItem(item.key)
                            )
                        }
                        if (filteredList.isEmpty()) {
                            item { 
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(200.dp), 
                                    contentAlignment = Alignment.Center
                                ) { 
                                    Text(
                                        stringResource(R.string.setedit_empty), 
                                        color = if(isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                } 
                            }
                        }
                    }
                }
            }

            // Header dengan Haze Effect
            val headerHazeStyle = if (isCustomBg) {
                HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = 0.3f),
                    blurRadius = 30.dp,
                    noiseFactor = 0.1f,
                    tints = listOf(HazeTint(Color.Black.copy(alpha = 0.1f)))
                )
            } else {
                HazeStyle(
                    backgroundColor = themeBgColor.copy(alpha = 0.7f),
                    blurRadius = 30.dp,
                    noiseFactor = 0.05f,
                    tints = listOf(HazeTint(themeBgColor.copy(alpha = 0.3f)))
                )
            }
            
            val headerTextColor = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
            val headerSubColor = if (isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .onGloballyPositioned { coordinates ->
                        headerHeightPx = coordinates.size.height
                    }
                    .hazeEffect(
                        state = hazeState,
                        style = headerHazeStyle
                    ) {
                        progressive = dev.chrisbanes.haze.HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f
                        )
                    }
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            stringResource(R.string.setedit_title), 
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold, 
                                color = headerTextColor
                            )
                        )
                        Text(
                            stringResource(R.string.setedit_subtitle), 
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = headerSubColor
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isGridView = true }, 
                            modifier = Modifier
                                .background(if (isGridView) finalPrimary.copy(alpha = 0.3f) else Color.Transparent, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Outlined.GridView, stringResource(R.string.setedit_view_grid), tint = headerTextColor)
                        }
                        IconButton(
                            onClick = { isGridView = false }, 
                            modifier = Modifier
                                .background(if (!isGridView) finalPrimary.copy(alpha = 0.3f) else Color.Transparent, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Outlined.FormatListBulleted, stringResource(R.string.setedit_view_list), tint = headerTextColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            stringResource(R.string.setedit_search_hint), 
                            style = MaterialTheme.typography.bodyLarge.copy(color = headerSubColor)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.setedit_search_hint), tint = headerSubColor) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if(isCustomBg) Color.White.copy(0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        unfocusedContainerColor = if(isCustomBg) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = finalPrimary,
                        focusedTextColor = headerTextColor,
                        unfocusedTextColor = headerTextColor
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(16.dp))

                SetEditTabRow(
                    categories = categories,
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
                    primaryColor = finalPrimary,
                    isCustomBg = isCustomBg
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // [NEW] Sensitive Item Delete Confirmation Dialog
    if (showSensitiveDialog && pendingDeleteItem != null) {
        AlertDialog(
            onDismissRequest = { 
                showSensitiveDialog = false
                pendingDeleteItem = null
            },
            icon = {
                Icon(
                    Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "⚠️ Sensitive Setting",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "You are about to delete a critical system setting:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            pendingDeleteItem!!.key,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = Color(0xFFFF6B35)
                        )
                    }
                    Text(
                        "This may cause system instability, security vulnerabilities, or unexpected behavior. Are you sure?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        performDelete(
                            pendingDeleteItem!!,
                            scope,
                            snackbarHostState,
                            editHistory,
                            onHistoryUpdate = { editHistory = it },
                            onRefresh = { refreshData(currentTabIndex) },
                            onDismiss = { 
                                showSensitiveDialog = false
                                pendingDeleteItem = null
                                showBottomSheet = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Anyway")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showSensitiveDialog = false
                        pendingDeleteItem = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // [NEW] History Bottom Sheet
    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recently Deleted",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${deletedHistory.size} items",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (deletedHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "No deleted items",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(deletedHistory) { historyItem ->
                            HistoryItemCard(
                                historyItem = historyItem,
                                onRestore = {
                                    // Restore logic
                                    scope.launch(Dispatchers.IO) {
                                        val item = historyItem.item
                                        val cat = item.sourceCategory
                                        val cmd = if (cat == PropCategory.Android) 
                                            "setprop ${item.key} \"${item.value}\"" 
                                        else 
                                            "settings put ${cat.command.split(" ")[2]} ${item.key} \"${item.value}\""
                                        
                                        val result = Shell.cmd(cmd).exec()
                                        withContext(Dispatchers.Main) {
                                            if (result.isSuccess) {
                                                snackbarHostState.showSnackbar("Restored: ${item.key}")
                                                editHistory = editHistory.filter { it.id != historyItem.id }
                                                refreshData(currentTabIndex)
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to restore")
                                            }
                                        }
                                    }
                                },
                                primaryColor = finalPrimary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showHistorySheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }

    // [NEW] Add New Property Dialog
    if (showAddDialog) {
        var newKey by remember { mutableStateOf("") }
        var newValue by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf(PropCategory.Global) }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { 
                Text(
                    "Add New Property",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category selector
                    Text(
                        "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PropCategory.values().filter { it != PropCategory.All }) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(stringResource(cat.titleRes)) },
                                leadingIcon = if (selectedCategory == cat) {
                                    { Icon(cat.icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Key Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKey.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                val cmd = if (selectedCategory == PropCategory.Android)
                                    "setprop $newKey \"$newValue\""
                                else
                                    "settings put ${selectedCategory.command.split(" ")[2]} $newKey \"$newValue\""
                                
                                val result = Shell.cmd(cmd).exec()
                                withContext(Dispatchers.Main) {
                                    if (result.isSuccess) {
                                        val newItem = SetEditItem(newKey, newValue, selectedCategory)
                                        editHistory = editHistory + EditHistoryItem(
                                            action = EditAction.CREATED,
                                            item = newItem
                                        )
                                        snackbarHostState.showSnackbar("Created: $newKey")
                                        refreshData(currentTabIndex)
                                        showAddDialog = false
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to create")
                                    }
                                }
                            }
                        }
                    },
                    enabled = newKey.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showBottomSheet && selectedItem != null) {
        val msgSaved = stringResource(R.string.setedit_msg_saved, "")
        val msgFailed = stringResource(R.string.setedit_msg_failed)
        val msgDeleted = stringResource(R.string.setedit_msg_deleted, "")
        val msgErrDel = stringResource(R.string.setedit_msg_error_delete)

        val activeCategory = selectedItem!!.sourceCategory

        EditPropBottomSheet(
            item = selectedItem!!,
            activeCategory = activeCategory, 
            onDismiss = { showBottomSheet = false },
            onSave = { key, newValue ->
                scope.launch(Dispatchers.IO) {
                    val oldValue = selectedItem!!.value
                    val cmd = if (activeCategory == PropCategory.Android) "setprop $key \"$newValue\"" else "settings put ${activeCategory.command.split(" ")[2]} $key \"$newValue\""
                    val result = Shell.cmd(cmd).exec()
                    withContext(Dispatchers.Main) {
                        if (result.isSuccess) { 
                            // [NEW] Track modification
                            editHistory = editHistory + EditHistoryItem(
                                action = EditAction.MODIFIED,
                                item = selectedItem!!,
                                oldValue = oldValue,
                                newValue = newValue
                            )
                            snackbarHostState.showSnackbar("$msgSaved $key")
                            refreshData(currentTabIndex)
                            showBottomSheet = false
                        }
                        else { snackbarHostState.showSnackbar(msgFailed) }
                    }
                }
            },
            onDelete = { key ->
                // [NEW] Check if sensitive
                if (isSensitiveItem(key)) {
                    pendingDeleteItem = selectedItem
                    showSensitiveDialog = true
                } else {
                    performDelete(
                        selectedItem!!,
                        scope,
                        snackbarHostState,
                        editHistory,
                        onHistoryUpdate = { editHistory = it },
                        onRefresh = { refreshData(currentTabIndex) },
                        onDismiss = { showBottomSheet = false }
                    )
                }
            },
            activeColor = finalPrimary
        )
    }
}

// [NEW] Helper function untuk delete
private fun performDelete(
    item: SetEditItem,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    currentHistory: List<EditHistoryItem>,
    onHistoryUpdate: (List<EditHistoryItem>) -> Unit,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit
) {
    scope.launch(Dispatchers.IO) {
        val cat = item.sourceCategory
        val cmd = if (cat == PropCategory.Android) "" else "settings delete ${cat.command.split(" ")[2]} ${item.key}"
        if (cmd.isNotEmpty()) {
            Shell.cmd(cmd).exec()
            withContext(Dispatchers.Main) { 
                // Track deletion
                onHistoryUpdate(currentHistory + EditHistoryItem(
                    action = EditAction.DELETED,
                    item = item
                ))
                snackbarHostState.showSnackbar("Deleted: ${item.key}")
                onRefresh()
                onDismiss()
            }
        } else { 
            withContext(Dispatchers.Main) { 
                snackbarHostState.showSnackbar("Cannot delete Android props") 
            } 
        }
    }
}

@Composable
fun HistoryItemCard(
    historyItem: EditHistoryItem,
    onRestore: () -> Unit,
    primaryColor: Color
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = remember(historyItem.timestamp) {
        dateFormat.format(Date(historyItem.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        historyItem.item.key,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Value: ${historyItem.item.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    timeString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            IconButton(
                onClick = onRestore,
                modifier = Modifier
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = "Restore",
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
fun SetEditTabRow(
    categories: Array<PropCategory>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    primaryColor: Color,
    isCustomBg: Boolean
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) { listState.animateScrollToItem(selectedIndex) }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp), 
        horizontalArrangement = Arrangement.spacedBy(8.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = selectedIndex == index
            
            if (isSelected) {
                Button(
                    onClick = { onTabSelected(index) },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = if (primaryColor.luminance() > 0.5f) Color.Black else Color.White
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(imageVector = category.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(category.titleRes), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            } else {
                val containerColor = if (isCustomBg) Color.White.copy(0.1f) else Color.Transparent
                val contentColor = if (isCustomBg) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                val borderColor = if (isCustomBg) Color.White.copy(0.3f) else MaterialTheme.colorScheme.outlineVariant

                OutlinedButton(
                    onClick = { onTabSelected(index) },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(imageVector = category.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(category.titleRes), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SetEditItemCard(
    item: SetEditItem, 
    onClick: () -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    primaryColor: Color,
    isSensitive: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
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

    val textColor = if (isGlassActive || cardColor.alpha < 1f) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isGlassActive || cardColor.alpha < 1f) Color.White.copy(0.7f) else MaterialTheme.colorScheme.secondary
    val iconTint = if (isGlassActive || cardColor.alpha < 1f) Color.White.copy(0.8f) else primaryColor.copy(alpha = 0.8f)
    
    val valueBoxColor = if (isGlassActive || cardColor.alpha < 1f) Color.White.copy(0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
    val valueTextColor = if (isGlassActive || cardColor.alpha < 1f) Color.White.copy(0.9f) else MaterialTheme.colorScheme.secondary

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardColor),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp).then(glassModifier)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.key, 
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), 
                        color = textColor, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isSensitive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Sensitive",
                            tint = Color(0xFFFF6B35),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(item.value)) }, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = stringResource(R.string.setedit_copy_value), tint = iconTint, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = valueBoxColor, 
                shape = RoundedCornerShape(8.dp), 
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.value, 
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 11.sp), 
                    color = valueTextColor, 
                    maxLines = 3, 
                    overflow = TextOverflow.Ellipsis, 
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun EditPropBottomSheet(item: SetEditItem, activeCategory: PropCategory, onDismiss: () -> Unit, onSave: (String, String) -> Unit, onDelete: (String) -> Unit, activeColor: Color) {
    val clipboardManager = LocalClipboardManager.current
    var editValue by remember { mutableStateOf(item.value) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val isSensitive = remember(item.key) { isSensitiveItem(item.key) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface) {
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect { if (dialogWindowProvider != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { dialogWindowProvider.window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND); dialogWindowProvider.window.attributes.blurBehindRadius = 64; dialogWindowProvider.window.setDimAmount(0.3f) } }

        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().verticalScroll(scrollState).padding(start = 24.dp, end = 24.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.setedit_sheet_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Surface(color = activeColor.copy(alpha=0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(stringResource(activeCategory.titleRes), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = activeColor)
                }
            }
            
            // [NEW] Warning for sensitive items
            if (isSensitive) {
                Surface(
                    color = Color(0xFFFF6B35).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF6B35)
                        )
                        Text(
                            "This is a sensitive system setting. Modifying it may affect system security or stability.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF6B35)
                        )
                    }
                }
            }
            
            Text(stringResource(R.string.setedit_label_key), style = MaterialTheme.typography.labelLarge, color = activeColor)
            OutlinedButton(onClick = { clipboardManager.setText(AnnotatedString(item.key)) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, activeColor.copy(0.5f))) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp), tint = activeColor); Spacer(Modifier.width(8.dp)); Text(item.key, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(stringResource(R.string.setedit_label_original), style = MaterialTheme.typography.labelLarge, color = activeColor)
            OutlinedButton(onClick = { clipboardManager.setText(AnnotatedString(item.value)) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, border = BorderStroke(1.dp, activeColor.copy(0.5f))) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp), tint = activeColor); Spacer(Modifier.width(8.dp)); Text(item.value, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
            }
            OutlinedTextField(value = editValue, onValueChange = { editValue = it }, label = { Text(stringResource(R.string.setedit_label_edit)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = activeColor, focusedLabelColor = activeColor, cursorColor = activeColor))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = { onDelete(item.key) }, 
                    colors = if (isSensitive) ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFF6B35).copy(alpha = 0.2f), 
                        contentColor = Color(0xFFFF6B35)
                    ) else ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer, 
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ), 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.setedit_btn_remove))
                }
                Button(onClick = { onSave(item.key, editValue) }, colors = ButtonDefaults.buttonColors(containerColor = activeColor), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.setedit_btn_save))
                }
            }
        }
    }
}
