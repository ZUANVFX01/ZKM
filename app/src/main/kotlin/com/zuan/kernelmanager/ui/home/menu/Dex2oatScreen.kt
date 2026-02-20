/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.home.menu

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.foundation.border

/**
 * Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan generateThemedBackgroundColor di AboutScreen
 * Hijau → #F8FBF2 (hijau sangat soft)
 * Ungu → #FFF7FC (ungu sangat soft) 
 * Pink → #FFF5F8 (pink sangat soft)
 */
@Composable
fun generateDex2oatBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
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

@Composable
fun Dex2oatScreen(
    navController: NavController,
    viewModel: Dex2oatViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    
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
    
    // FIX: Ambil theme mode dari settings (bukan dari sistem langsung)
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    
    val effectivePrimary = if (isDynamic) {
        MaterialTheme.colorScheme.primary
    } else if (isCustomColor) {
        Color(customPrimary)
    } else {
        themeColorName.primary
    }

    // FIX: Hitung dark theme berdasarkan setting aplikasi, bukan sistem HP
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // SESUAI ABOUTSCREEN: Generate tinted background untuk semua kasus SYSTEM
    val (themeBackground, themeCardColor) = if (bgType == BgType.SYSTEM) {
        generateDex2oatBackgroundColors(isDarkTheme, effectivePrimary)
    } else {
        Pair(Color.Transparent, Color(0xFFFFFFFF))
    }
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    val mainBackgroundColor = if (bgType != BgType.SYSTEM) {
        Color.Transparent
    } else {
        themeBackground
    }
    
    // Card base color dari generate, bukan dari MaterialTheme langsung
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> themeCardColor
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
    
    val dividerColor = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle(initialValue = emptyList())
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val processingStatus by viewModel.processingStatus.collectAsStateWithLifecycle()
    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val hazeState = rememberHazeState()

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        if (isCustomBg && bgUriString != null) {
            val blurModifier = if (isBgBlur && blurStrength > 0f) {
                Modifier.blur(blurStrength.dp)
            } else if (isBgBlur) {
                Modifier.blur(20.dp)
            } else {
                Modifier
            }
            
            val saturationMatrix = ColorMatrix().apply { setToSaturation(bgSaturation) }
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassDexHeader(
                    navController = navController,
                    viewModel = viewModel,
                    isCustomBg = isCustomBg,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary
                )
            },
            floatingActionButton = {
                if (!isLoading && processingStatus == null) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.compileAll() },
                        containerColor = if (isGlassActive) effectivePrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = { Icon(Icons.Rounded.ElectricBolt, null) },
                        text = { Text(stringResource(R.string.dex2oat_compile_all)) }
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                GlassInfoCard(
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    cardColor = finalCardColor,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary,
                    onClick = { uriHandler.openUri("https://source.android.com/docs/core/runtime/jit-compiler") }
                )

                GlassSearchModeCard(
                    viewModel = viewModel,
                    searchQuery = searchQuery,
                    selectedMode = selectedMode,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    cardColor = finalCardColor,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    primaryColor = effectivePrimary
                )
                
                if (processingStatus != null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = effectivePrimary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(
                                if (isGlassActive) effectivePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = processingStatus ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isGlassActive) Color.White else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = effectivePrimary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppCompileItemGlass(
                                app = app,
                                isEnabled = processingStatus == null,
                                onCompile = { viewModel.compileApp(app) },
                                onReset = { viewModel.resetApp(app) },
                                isGlassActive = isGlassActive,
                                hazeState = hazeState,
                                textColor = textColor,
                                subTextColor = subTextColor,
                                primaryColor = effectivePrimary
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp),
                                color = dividerColor.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassInfoCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = primaryColor.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
            .clickable(onClick = onClick)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else primaryColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Info, null, tint = if (isGlassActive) Color.White else primaryColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dex2oat_art_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dex2oat_art_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.dex2oat_view_source),
                    style = MaterialTheme.typography.labelSmall.copy(textDecoration = TextDecoration.Underline),
                    color = if (isGlassActive) Color.White.copy(alpha = 0.8f) else primaryColor
                )
            }
        }
    }
}

@Composable
fun GlassSearchModeCard(
    viewModel: Dex2oatViewModel,
    searchQuery: String,
    selectedMode: String,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val shape = RoundedCornerShape(24.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearch(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.dex2oat_search_hint), color = subTextColor) },
                leadingIcon = { Icon(Icons.Rounded.Search, null, tint = subTextColor) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surface,
                    focusedContainerColor = if (isGlassActive) Color.White.copy(0.15f) else MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = primaryColor.copy(alpha = 0.5f),
                    cursorColor = primaryColor,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.dex2oat_compilation_mode), 
                style = MaterialTheme.typography.labelMedium, 
                color = if (isGlassActive) Color.White else primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dex2oatUtils.COMPILE_MODES.forEach { (modeKey, modeName) ->
                    val selected = selectedMode == modeKey
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onModeSelect(modeKey) },
                        label = { Text(modeName.substringBefore(" "), color = if (selected) Color.White else textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassDexHeader(
    navController: NavController,
    viewModel: Dex2oatViewModel,
    isCustomBg: Boolean,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    var showMenu by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)

    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = 0.3f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
    } else {
        Modifier
    }
    
    val bgColor = if (isCustomBg) Color.Transparent else MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(glassModifier)
            .background(bgColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Rounded.ArrowBack, 
                    contentDescription = stringResource(R.string.dex2oat_back), 
                    tint = textColor
                )
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Rounded.MoreVert, 
                        contentDescription = stringResource(R.string.dex2oat_options), 
                        tint = textColor
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = if (isGlassActive) Color(0xFF1D1D1D) else MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dex2oat_compile_system_only), color = if (isGlassActive) Color.White else Color.Unspecified) },
                        onClick = { 
                            viewModel.compileSystemApps()
                            showMenu = false 
                        },
                        leadingIcon = { Icon(Icons.Rounded.SystemSecurityUpdate, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dex2oat_compile_user_only), color = if (isGlassActive) Color.White else Color.Unspecified) },
                        onClick = { 
                            viewModel.compileUserApps()
                            showMenu = false 
                        },
                        leadingIcon = { Icon(Icons.Rounded.Apps, null) }
                    )
                    HorizontalDivider(color = if (isGlassActive) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant)
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.dex2oat_reset_all), color = MaterialTheme.colorScheme.error) },
                        onClick = { 
                            viewModel.resetAllApps()
                            showMenu = false 
                        },
                        leadingIcon = { Icon(Icons.Rounded.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.dex2oat_title),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun AppCompileItemGlass(
    app: AppCompileItem,
    isEnabled: Boolean,
    onCompile: () -> Unit,
    onReset: () -> Unit,
    isGlassActive: Boolean,
    hazeState: HazeState,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val shape = RoundedCornerShape(12.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.White.copy(alpha = 0.1f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
                    tints = emptyList()
                )
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (app.icon != null) {
            Image(
                painter = rememberAsyncImagePainter(model = app.icon), 
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier.size(48.dp).background(
                    if (isGlassActive) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh, 
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Android, 
                    null, 
                    tint = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = subTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (app.isSystem) {
                Text(
                    text = stringResource(R.string.dex2oat_system_app),
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor
                )
            }
        }

        Row {
            IconButton(onClick = onReset, enabled = isEnabled) {
                Icon(
                    Icons.Rounded.RestartAlt, 
                    null, 
                    tint = if (isEnabled) {
                        if (isGlassActive) Color.White.copy(0.7f) else MaterialTheme.colorScheme.secondary 
                    } else Color.Gray
                )
            }
            IconButton(onClick = onCompile, enabled = isEnabled) {
                Icon(
                    Icons.Rounded.PlayArrow, 
                    null, 
                    tint = if (isEnabled) primaryColor else Color.Gray
                )
            }
        }
    }
}
