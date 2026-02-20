/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class, 
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.logsview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Import untuk Wallpaper Style Support
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.settings.WeatherEffect
import com.zuan.kernelmanager.ui.theme.ThemeMode

// Import CircularWavy
import androidx.compose.material3.CircularWavyProgressIndicator

/**
 * GENERATE TINTED BACKGROUND - CARA KERJA SEPERTI ABOUT SCREEN
 * Light Mode: 97% White + 3% Primary = Background soft ber-tint
 * Dark Mode: Primary dikurangi intensitas 92%, blend dengan dark base
 */
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
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
        
        // Card color: putih bersih
        val cardColor = Color.White
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun LogsViewScreen(
    viewModel: LogsViewViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // --- WALLPAPER STYLE SETTINGS COLLECTION ---
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    
    // Weather Effects
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    
    // Card Style
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    // Theme Colors
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // Ambil theme mode dari ViewModel untuk mengikuti pengaturan aplikasi, bukan sistem
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    // Penentuan primary color (sama logic dengan AboutScreen & BatteryScreen)
    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColorName.primary
        }
    }

    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val context = LocalContext.current
    
    // Hitung isDark berdasarkan themeMode
    val isAppDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }
    
    // TERAPKAN THEME BACKGROUND SEPERTI ABOUT SCREEN & BATTERY SCREEN
    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(finalPrimary, isAppDark)
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    // Penentuan warna card
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }

    // Background utama
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else tintedBackground

    // --- EXISTING STATES ---
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val displayOptions by viewModel.displayOptions.collectAsStateWithLifecycle()
    val settings = viewModel.settings
    
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val hazeState = remember { HazeState() }
    val scope = rememberCoroutineScope()
    
    var selectedLog by remember { mutableStateOf<LogEntry?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }
    var showDisplayOptionsSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
        ) {
            // --- BACKGROUND LAYER (WALLPAPER STYLE) ---
            if (isCustomBg && bgUriString != null) {
                // Apply blur
                val blurModifier = if (isBgBlur && blurStrength > 0f) {
                    Modifier.blur(blurStrength.dp)
                } else if (isBgBlur) {
                    Modifier.blur(20.dp)
                } else {
                    Modifier
                }
                
                // Apply saturation
                val saturationMatrix = ColorMatrix().apply {
                    setToSaturation(bgSaturation)
                }
                val colorFilter = ColorFilter.colorMatrix(saturationMatrix)
                
                val uri = Uri.parse(bgUriString)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState, zIndex = 0f)
                ) {
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = bgContrast))
                        )
                    }
                }
            } else {
                // Default background dengan tinted theme
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tintedBackground)
                        .hazeSource(state = hazeState, zIndex = 0f)
                )
            }
            
            // --- WEATHER EFFECT OVERLAY ---
            WeatherEffectOverlay(
                effect = weatherEffect,
                intensity = weatherIntensity,
                modifier = Modifier.fillMaxSize()
            )

            // --- CONTENT LIST ---
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    top = 150.dp,
                    bottom = 100.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(if (displayOptions.isCompact) 8.dp else 12.dp)
            ) {
                items(
                    items = logs,
                    key = { it.id }
                ) { log ->
                    LogItemCardExpressive(
                        log = log,
                        isSystemDark = isAppDark,
                        isCustomBg = isCustomBg,
                        isGlassActive = isGlassActive,
                        hazeState = hazeState,
                        cardColor = finalCardColor,
                        options = displayOptions,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedLog = log
                            showDetailSheet = true
                        }
                    )
                }
                if (logs.isEmpty()) {
                    item { 
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp), 
                            contentAlignment = Alignment.Center
                        ) { 
                            Text(
                                stringResource(R.string.logs_empty), 
                                color = if(isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        } 
                    }
                }
            }

            // --- CHROME TOOLBAR dengan GLASS STYLE ---
            ChromeStyleToolbar(
                modifier = Modifier.align(Alignment.TopCenter),
                viewModel = viewModel,
                logsCount = logs.size,
                totalCached = viewModel.settings.maxLogsInMemory,
                hazeState = hazeState,
                isSystemDark = isAppDark,
                isCustomBg = isCustomBg,
                effectivePrimary = finalPrimary,
                onOpenOptions = { 
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    showDisplayOptionsSheet = true 
                },
                onOpenSettings = {
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    showSettingsSheet = true
                },
                onClearFocus = { focusManager.clearFocus() }
            )
            
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(52.dp),
                            color = finalPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        
                        Text(
                            stringResource(R.string.logs_reading), 
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isCustomBg) Color.White.copy(0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            val isAtBottom = !scrollState.canScrollForward
            AnimatedVisibility(
                visible = !isAtBottom && logs.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                enter = scaleIn(spring(dampingRatio = 0.6f, stiffness = 300f)) + fadeIn(),
                exit = scaleOut(spring(dampingRatio = 0.8f, stiffness = 300f)) + fadeOut()
            ) {
                // FAB dengan glass effect jika custom background
                if (isGlassActive) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .hazeEffect(
                                state = hazeState,
                                style = HazeStyle(
                                    backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    blurRadius = 20.dp,
                                    noiseFactor = 0.1f,
                                    tints = emptyList()
                                )
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    scrollState.animateScrollToItem(0)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.ArrowDownward, 
                            contentDescription = stringResource(R.string.logs_scroll_bottom), 
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = { 
                            scope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.logs_scroll_bottom), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }

    // Bottom Sheets (tetap sama)
    if (showDisplayOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDisplayOptionsSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { 
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            DisplayOptionsContent(
                options = displayOptions,
                onUpdate = { viewModel.updateDisplayOptions(it) },
                onClose = { showDisplayOptionsSheet = false }
            )
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            SettingsContent(
                currentSettings = settings,
                onUpdate = { viewModel.updateSettings(it) },
                onClose = { showSettingsSheet = false }
            )
        }
    }

    if (showDetailSheet && selectedLog != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            LogDetailContent(
                log = selectedLog!!,
                isSystemDark = isAppDark,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Log Entry", selectedLog!!.fullRaw)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, R.string.logs_copied, Toast.LENGTH_SHORT).show()
                    showDetailSheet = false
                }
            )
        }
    }
}

// ... (sisa composable ChromeStyleToolbar, LogItemCardExpressive, dll tetap sama)
// Pastikan semua menggunakan effectivePrimary yang sudah di-fix


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChromeStyleToolbar(
    modifier: Modifier = Modifier,
    viewModel: LogsViewViewModel,
    logsCount: Int,
    totalCached: Int,
    hazeState: HazeState,
    isSystemDark: Boolean,
    isCustomBg: Boolean,
    effectivePrimary: Color,
    onOpenOptions: () -> Unit,
    onOpenSettings: () -> Unit,
    onClearFocus: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val widthFraction by animateFloatAsState(
        targetValue = if (isFocused) 0.98f else 0.95f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "ToolbarWidth"
    )

    // Glass style untuk custom background
    val hazeStyle = if (isCustomBg) {
        HazeStyle(
            backgroundColor = Color.Black.copy(alpha = 0.2f),
            blurRadius = 40.dp,
            noiseFactor = 0.1f,
            tints = emptyList()
        )
    } else {
        HazeStyle(
            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            blurRadius = 40.dp,
            noiseFactor = 0.05f,
            tints = listOf(HazeTint(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)))
        )
    }

    val formattedCount = remember(logsCount) {
        NumberFormat.getNumberInstance(Locale.US).format(logsCount)
    }
    
    // Text colors untuk custom background
    val headerTextColor = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
    val headerSubColor = if (isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .padding(top = 48.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(76.dp)
                .shadow(
                    elevation = if (isFocused) 12.dp else 6.dp,
                    shape = RoundedCornerShape(percent = 50),
                    spotColor = effectivePrimary.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(percent = 50))
                .hazeEffect(state = hazeState, style = hazeStyle)
                .border(
                    width = 1.dp,
                    color = if (isFocused) 
                        effectivePrimary.copy(alpha = 0.5f)
                    else 
                        if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(percent = 50)
                )
                .background(
                    if (isCustomBg) 
                        Color.Black.copy(alpha = 0.3f)
                    else 
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = headerSubColor
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    BasicTextField(
                        value = viewModel.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = headerTextColor,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(effectivePrimary),
                        interactionSource = interactionSource,
                        decorationBox = { innerTextField ->
                            if (viewModel.searchQuery.isEmpty()) {
                                Text(
                                    stringResource(R.string.logs_search_hint),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp,
                                        color = headerSubColor.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                    
                    AnimatedVisibility(
                        visible = logsCount > 0,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.logs_count, formattedCount),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.1.sp
                                ),
                                color = headerSubColor.copy(alpha = 0.7f)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (viewModel.isPaused) 
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        else 
                                            effectivePrimary.copy(alpha = 0.8f)
                                    )
                            )
                            
                            Text(
                                text = if (viewModel.isPaused) stringResource(R.string.logs_status_paused) else stringResource(R.string.logs_status_live),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = if (viewModel.isPaused) 
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else 
                                    effectivePrimary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = viewModel.searchQuery.isNotEmpty(),
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    IconButton(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onSearchQueryChange("") 
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.logs_clear_search),
                            modifier = Modifier.size(20.dp),
                            tint = headerSubColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(if (isCustomBg) Color.White.copy(0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            viewModel.togglePause() 
                        },
                        shape = CircleShape,
                        color = if (viewModel.isPaused) 
                            effectivePrimary.copy(alpha = 0.2f)
                        else 
                            Color.Transparent,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = viewModel.isPaused,
                                transitionSpec = {
                                    (scaleIn(spring(dampingRatio = 0.6f)) + fadeIn()) with
                                    (scaleOut(spring(dampingRatio = 0.8f)) + fadeOut())
                                },
                                label = "PlayPause"
                            ) { isPaused ->
                                Icon(
                                    if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                    contentDescription = if (isPaused) stringResource(R.string.logs_resume) else stringResource(R.string.logs_pause),
                                    tint = if (isPaused) 
                                        effectivePrimary
                                    else 
                                        headerTextColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }
                        
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                menuExpanded = true 
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Rounded.MoreVert,
                                contentDescription = stringResource(R.string.logs_menu),
                                modifier = Modifier.size(24.dp),
                                tint = headerTextColor
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .background(
                                    if (isCustomBg) Color(0xFF1A1A1A).copy(alpha = 0.95f)
                                    else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f)
                                )
                                .border(
                                    1.dp,
                                    if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    RoundedCornerShape(20.dp)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 8.dp
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logs_settings), style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { 
                                    Icon(Icons.Rounded.Settings, null, tint = effectivePrimary) 
                                },
                                onClick = { 
                                    menuExpanded = false
                                    onOpenSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logs_display_options), style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { 
                                    Icon(Icons.Rounded.Tune, null, tint = effectivePrimary) 
                                },
                                onClick = { 
                                    menuExpanded = false
                                    onOpenOptions() 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logs_save), style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { 
                                    Icon(Icons.Rounded.Save, null, tint = effectivePrimary) 
                                },
                                onClick = { 
                                    menuExpanded = false
                                    viewModel.saveLogs(context)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        stringResource(R.string.logs_clear), 
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    ) 
                                },
                                leadingIcon = { 
                                    Icon(Icons.Rounded.ClearAll, null, tint = MaterialTheme.colorScheme.error) 
                                },
                                onClick = { 
                                    menuExpanded = false
                                    viewModel.clearLogs()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logs_restart), style = MaterialTheme.typography.bodyLarge) },
                                leadingIcon = { 
                                    Icon(Icons.Rounded.Refresh, null, tint = effectivePrimary) 
                                },
                                onClick = { 
                                    menuExpanded = false
                                    viewModel.restartLogcat()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCardExpressive(
    log: LogEntry,
    isSystemDark: Boolean,
    isCustomBg: Boolean,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    options: LogDisplayOptions,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    // Glassmorphism card
    val shape = RoundedCornerShape(20.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.3f),
                    blurRadius = 24.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = shape
            )
    } else {
        Modifier.clip(shape)
    }
    
    // Colors adjustment untuk custom background
    val textColor = if (isCustomBg || isGlassActive) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isCustomBg || isGlassActive) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    val vPadding = if (options.isCompact) 10.dp else 16.dp
    val fontSize = if (options.isCompact) 12.sp else 13.sp

    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            onClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isGlassActive) Color.Transparent else cardColor.copy(alpha = 0.9f)
        ),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .then(glassModifier)
            .border(
                1.dp, 
                if (isGlassActive) Color.Transparent else {
                    if (isSystemDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                },
                RoundedCornerShape(20.dp)
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isGlassActive) 0.dp else if (isSystemDark) 2.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(0.dp)
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                    .background(log.level.color)
            )
            
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = vPadding)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (options.showTag) {
                        Surface(
                            color = log.level.color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, log.level.color.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = log.tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                ),
                                color = log.level.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val metaParts = buildList {
                        if (options.showTime) add(log.timestamp)
                        if (options.showDate) add(log.date)
                        if (options.showPid) add("P:${log.pid}")
                        if (options.showTid) add("T:${log.tid}")
                    }
                    
                    if (metaParts.isNotEmpty()) {
                        Text(
                            text = metaParts.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = subTextColor.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(log.level.color)
                    )
                }
                
                if (!options.isCompact) Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = fontSize,
                        lineHeight = if (options.isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.sp
                    ),
                    color = textColor.copy(alpha = 0.9f),
                    maxLines = if (options.isCompact) 3 else 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SettingsContent(
    currentSettings: LogSettings,
    onUpdate: (LogSettings) -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.logs_settings_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    stringResource(R.string.logs_settings_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledIconButton(
                onClick = onClose,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Rounded.Check, stringResource(R.string.btn_done))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Poll Interval
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.logs_poll_interval),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.logs_ms, currentSettings.pollInterval),
                    style = MaterialTheme.typography.headlineMedium
                )
                Slider(
                    value = currentSettings.pollInterval.toFloat(),
                    onValueChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onUpdate(currentSettings.copy(pollInterval = it.toLong())) 
                    },
                    valueRange = 50f..1000f,
                    steps = 19
                )
                Text(
                    stringResource(R.string.logs_poll_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Buffers Selection
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.logs_buffers),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val buffers = LogBuffer.entries
                buffers.chunked(2).forEach { rowBuffers ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowBuffers.forEach { buffer ->
                            val isSelected = currentSettings.selectedBuffers.contains(buffer)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                    val newBuffers = if (isSelected) {
                                        currentSettings.selectedBuffers - buffer
                                    } else {
                                        currentSettings.selectedBuffers + buffer
                                    }
                                    onUpdate(currentSettings.copy(selectedBuffers = newBuffers))
                                },
                                label = { Text(buffer.displayName) },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                        if (rowBuffers.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Max Logs
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.logs_max_logs),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                val maxLogsFormatted = NumberFormat.getNumberInstance(Locale.US)
                    .format(currentSettings.maxLogsInMemory)
                Text(
                    maxLogsFormatted,
                    style = MaterialTheme.typography.headlineMedium
                )
                Slider(
                    value = currentSettings.maxLogsInMemory.toFloat(),
                    onValueChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onUpdate(currentSettings.copy(maxLogsInMemory = it.toInt())) 
                    },
                    valueRange = 1000f..500000f,
                    steps = 20
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Save Location
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                val newLocation = if (currentSettings.saveLocation == SaveLocation.INTERNAL) 
                    SaveLocation.EXTERNAL else SaveLocation.INTERNAL
                onUpdate(currentSettings.copy(saveLocation = newLocation))
            },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Rounded.Folder, null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.logs_save_location),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        if (currentSettings.saveLocation == SaveLocation.INTERNAL) 
                            stringResource(R.string.logs_internal_storage) 
                        else 
                            stringResource(R.string.logs_external_docs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = currentSettings.saveLocation == SaveLocation.EXTERNAL,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onUpdate(currentSettings.copy(
                            saveLocation = if (it) SaveLocation.EXTERNAL else SaveLocation.INTERNAL
                        ))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DisplayOptionsContent(
    options: LogDisplayOptions,
    onUpdate: (LogDisplayOptions) -> Unit,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.logs_display_options_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    stringResource(R.string.logs_display_options_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledIconButton(
                onClick = onClose,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Rounded.Check, stringResource(R.string.btn_done))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.logs_metadata),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionSwitch(stringResource(R.string.logs_show_tag), options.showTag) { onUpdate(options.copy(showTag = it)) }
                OptionSwitch(stringResource(R.string.logs_show_time), options.showTime) { onUpdate(options.copy(showTime = it)) }
                OptionSwitch(stringResource(R.string.logs_show_date), options.showDate) { onUpdate(options.copy(showDate = it)) }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    stringResource(R.string.logs_advanced),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OptionSwitch(stringResource(R.string.logs_show_pid), options.showPid) { onUpdate(options.copy(showPid = it)) }
                OptionSwitch(stringResource(R.string.logs_show_tid), options.showTid) { onUpdate(options.copy(showTid = it)) }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Surface(
            onClick = { 
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onUpdate(options.copy(isCompact = !options.isCompact)) 
            },
            shape = RoundedCornerShape(20.dp),
            color = if (options.isCompact) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            border = if (options.isCompact) 
                BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            else 
                null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (options.isCompact) 
                        MaterialTheme.colorScheme.secondary 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (options.isCompact) Icons.Rounded.ViewHeadline else Icons.Rounded.ViewStream,
                            null,
                            tint = if (options.isCompact) 
                                MaterialTheme.colorScheme.onSecondary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.logs_compact_mode),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        stringResource(R.string.logs_compact_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = options.isCompact,
                    onCheckedChange = { 
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onUpdate(options.copy(isCompact = it)) 
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OptionSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onCheckedChange(!checked) 
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            modifier = Modifier.weight(1f)
        )

        val thumbContent: (@Composable () -> Unit)? = if (checked) {
            { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp)) }
        } else null

        Switch(
            checked = checked,
            onCheckedChange = { 
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onCheckedChange(it) 
            },
            thumbContent = thumbContent
        )
    }
}

@Composable
fun LogDetailContent(
    log: LogEntry,
    isSystemDark: Boolean,
    onCopy: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = log.level.color.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, log.level.color.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(log.level.color)
                    )
                    Text(
                        text = log.level.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = log.level.color
                        )
                    )
                }
            }
            
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCopy()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(Icons.Rounded.ContentCopy, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.logs_copy))
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow(stringResource(R.string.logs_time), log.timestamp)
                DetailRow(stringResource(R.string.logs_date), log.date)
                DetailRow(stringResource(R.string.logs_pid), log.pid)
                DetailRow(stringResource(R.string.logs_tid), log.tid)
                DetailRow(stringResource(R.string.logs_tag), log.tag)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            stringResource(R.string.logs_raw_log),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Surface(
            color = if (isSystemDark) Color(0xFF0F0F0F) else Color(0xFFF8F9FA),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = log.fullRaw,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isSystemDark) Color(0xFFE0E0E0) else Color(0xFF1F1F1F),
                modifier = Modifier.padding(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
