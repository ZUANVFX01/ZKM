/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.settings

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeStyle
import androidx.compose.foundation.BorderStroke

@Composable
fun WallpaperStyleScreen(
    navController: NavController,
    viewModel: WallpaperStyleViewModel = viewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamic by viewModel.isDynamicColor.collectAsState()
    val selectedThemeColor by viewModel.currentThemeColor.collectAsState()
    val effectiveColor by viewModel.effectiveThemeColor.collectAsState()
    
    val isCustomColor by viewModel.isCustomColor.collectAsState()
    val customPrimary by viewModel.customPrimaryColor.collectAsState()
    val customSecondary by viewModel.customSecondaryColor.collectAsState()
    val customTertiary by viewModel.customTertiaryColor.collectAsState()
    
    val weatherEffect by viewModel.weatherEffect.collectAsState()
    val weatherIntensity by viewModel.weatherIntensity.collectAsState()
    
    val bgSaturation by viewModel.bgSaturation.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()
    val isBgBlur by viewModel.isBgBlur.collectAsState()

    val context = LocalContext.current
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    val baseColorScheme = remember(isDynamic, effectiveColor, isDark, isCustomColor) {
        if (isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (isDark) {
                darkColorScheme(
                    primary = effectiveColor.primary,
                    onPrimary = if (effectiveColor.primary.luminance() > 0.5f) Color.Black else Color.White,
                    primaryContainer = effectiveColor.primary.copy(alpha = 0.3f),
                    onPrimaryContainer = effectiveColor.primary,
                    secondary = effectiveColor.secondary,
                    secondaryContainer = effectiveColor.secondary.copy(alpha = 0.3f),
                    onSecondaryContainer = effectiveColor.secondary,
                    tertiary = effectiveColor.tertiary,
                    tertiaryContainer = effectiveColor.tertiary.copy(alpha = 0.3f),
                    onTertiaryContainer = effectiveColor.tertiary,
                    surfaceVariant = effectiveColor.primary.copy(alpha = 0.1f),
                    onSurfaceVariant = effectiveColor.primary
                )
            } else {
                lightColorScheme(
                    primary = effectiveColor.primary,
                    onPrimary = if (effectiveColor.primary.luminance() > 0.5f) Color.Black else Color.White,
                    primaryContainer = effectiveColor.primary.copy(alpha = 0.2f),
                    onPrimaryContainer = effectiveColor.primary,
                    secondary = effectiveColor.secondary,
                    secondaryContainer = effectiveColor.secondary.copy(alpha = 0.2f),
                    onSecondaryContainer = effectiveColor.secondary,
                    tertiary = effectiveColor.tertiary,
                    tertiaryContainer = effectiveColor.tertiary.copy(alpha = 0.2f),
                    onTertiaryContainer = effectiveColor.tertiary,
                    surfaceVariant = effectiveColor.primary.copy(alpha = 0.1f),
                    onSurfaceVariant = effectiveColor.primary
                )
            }
        }
    }

    val targetColorScheme = baseColorScheme.copy(
        background = if (isDark) Color(0xFF151E27) else Color(0xFFF0F0F0),
        surfaceContainer = if (isDark) Color(0xFF1D2733) else Color(0xFFFFFFFF),
        surface = if (isDark) Color(0xFF1D2733) else Color(0xFFFFFFFF)
    )

    val hazeState = rememberHazeState()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.setBackgroundMedia(it) }
    }
    
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var editingColorType by remember { mutableStateOf<ColorType?>(null) }

    MaterialTheme(
        colorScheme = targetColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.wallpaper_style_title), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, stringResource(R.string.btn_cancel))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) {
                // PREVIEW AREA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(9f / 19f)
                            .clip(RoundedCornerShape(24.dp))
                            .border(4.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                            .background(Color.Black)
                    ) {
                        // BACKGROUND LAYER dengan Weather Effects
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(state = hazeState, zIndex = 0f)
                        ) {
                            BackgroundRendererWithEffects(
                                viewModel = viewModel,
                                bgSaturation = bgSaturation,
                                blurStrength = if (isBgBlur) blurStrength else 0f
                            )
                            
                            WeatherEffectOverlay(
                                effect = weatherEffect,
                                intensity = weatherIntensity,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // CONTENT LAYER
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Spacer(Modifier.height(24.dp))
                                Text("10:00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Tue, Jan 6", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(12.dp))
                                StyleableCardPreview(viewModel, hazeState) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Smartphone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(stringResource(R.string.preview_title), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                            Text(stringResource(R.string.preview_live), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CONTROLS AREA
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    tonalElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
                        Box(modifier = Modifier.height(350.dp)) {
                            when (selectedTab) {
                                0 -> ColorsTabContent(
                                    viewModel = viewModel,
                                    onShowCustomColorPicker = { colorType ->
                                        editingColorType = colorType
                                        showCustomColorPicker = true
                                    }
                                )
                                1 -> BackgroundTabContent(
                                    viewModel = viewModel,
                                    imagePickerLauncher = imagePickerLauncher
                                )
                                2 -> EffectsTabContent(viewModel = viewModel)
                            }
                        }
                        
                        // Tab buttons
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TabButton(stringResource(R.string.tab_colors), selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
                            TabButton(stringResource(R.string.tab_background), selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
                            TabButton(stringResource(R.string.tab_effects), selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
                        }
                    }
                }
            }
        }
        
        // Custom Color Picker Dialog
        if (showCustomColorPicker && editingColorType != null) {
            val initialColor = when (editingColorType) {
                ColorType.PRIMARY -> Color(customPrimary)
                ColorType.SECONDARY -> Color(customSecondary)
                ColorType.TERTIARY -> Color(customTertiary)
                null -> Color(0xFF4A6595)
            }
            
            AdvancedColorPickerDialog(
                initialColor = initialColor,
                onDismiss = { showCustomColorPicker = false },
                onColorSelected = { color ->
                    when (editingColorType) {
                        ColorType.PRIMARY -> viewModel.setCustomPrimaryColor(color)
                        ColorType.SECONDARY -> viewModel.setCustomSecondaryColor(color)
                        ColorType.TERTIARY -> viewModel.setCustomTertiaryColor(color)
                        null -> {}
                    }
                    showCustomColorPicker = false
                }
            )
        }
    }
}

enum class ColorType { PRIMARY, SECONDARY, TERTIARY }

// ==========================================
// TAB CONTENTS
// ==========================================

@Composable
fun ColorsTabContent(
    viewModel: WallpaperStyleViewModel,
    onShowCustomColorPicker: (ColorType) -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val isDynamic by viewModel.isDynamicColor.collectAsState()
    val selectedThemeColor by viewModel.currentThemeColor.collectAsState()
    
    val isCustomColor by viewModel.isCustomColor.collectAsState()
    val customPrimary by viewModel.customPrimaryColor.collectAsState()
    val customSecondary by viewModel.customSecondaryColor.collectAsState()
    val customTertiary by viewModel.customTertiaryColor.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        // Dark Theme Toggle
        Text(
            stringResource(R.string.style_theme_header),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.dark_theme), style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (themeMode == ThemeMode.DARK) stringResource(R.string.dark_theme_on) else stringResource(R.string.dark_theme_off),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = themeMode == ThemeMode.DARK,
                onCheckedChange = { viewModel.setThemeMode(if (it) ThemeMode.DARK else ThemeMode.LIGHT) }
            )
        }
        
        Spacer(Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
        Spacer(Modifier.height(24.dp))
        
        // Color Palette Grid
        ColorPresetGrid(
            selectedColor = selectedThemeColor,
            isDynamic = isDynamic,
            isCustomColor = isCustomColor,
            customColor = Color(customPrimary),
            onColorSelected = { viewModel.setThemeColor(it) },
            onDynamicSelected = { viewModel.setDynamicColor(true) },
            onCustomColorSelected = { viewModel.setCustomColorEnabled(true) }
        )
        
        // Custom Color Editor (shown when custom color is selected)
        if (isCustomColor) {
            Spacer(Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            Spacer(Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.custom_color_editor),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomColorButton(
                    label = stringResource(R.string.color_primary),
                    color = Color(customPrimary),
                    onClick = { onShowCustomColorPicker(ColorType.PRIMARY) },
                    modifier = Modifier.weight(1f)
                )
                CustomColorButton(
                    label = stringResource(R.string.color_secondary),
                    color = Color(customSecondary),
                    onClick = { onShowCustomColorPicker(ColorType.SECONDARY) },
                    modifier = Modifier.weight(1f)
                )
                CustomColorButton(
                    label = stringResource(R.string.color_tertiary),
                    color = Color(customTertiary),
                    onClick = { onShowCustomColorPicker(ColorType.TERTIARY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomColorButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun BackgroundTabContent(
    viewModel: WallpaperStyleViewModel,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val bgType by viewModel.bgType.collectAsState()
    val isBgBlur by viewModel.isBgBlur.collectAsState()
    val bgContrast by viewModel.bgContrast.collectAsState()
    val cardDarkness by viewModel.cardDarkness.collectAsState()
    val isCardBlur by viewModel.isCardBlur.collectAsState()
    
    val bgSaturation by viewModel.bgSaturation.collectAsState()
    val blurStrength by viewModel.blurStrength.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            stringResource(R.string.background_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BgOptionCard(
                stringResource(R.string.bg_type_gallery),
                Icons.Outlined.Image,
                bgType == BgType.GALLERY,
                Modifier.weight(1f)
            ) {
                imagePickerLauncher.launch(arrayOf("image/*", "video/*", "video/mp4", "video/x-matroska"))
            }
            BgOptionCard(
                stringResource(R.string.bg_type_system),
                Icons.Outlined.Android,
                bgType == BgType.SYSTEM,
                Modifier.weight(1f)
            ) { viewModel.setBgType(BgType.SYSTEM) }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
        Spacer(modifier = Modifier.height(24.dp))

        // Background Adjustments
        Text(
            stringResource(R.string.bg_adjustments),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Saturation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Colorize, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.saturation), style = MaterialTheme.typography.bodyLarge)
                Text("${(bgSaturation * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Slider(
            value = bgSaturation,
            onValueChange = { viewModel.setBgSaturation(it) },
            valueRange = 0f..2f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Blur toggle and strength
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.BlurOn, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.bg_blur), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.bg_blur_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isBgBlur,
                onCheckedChange = { viewModel.setBgBlurEnabled(it) }
            )
        }
        
        if (isBgBlur) {
            Text(stringResource(R.string.blur_strength, blurStrength.toInt()), style = MaterialTheme.typography.labelMedium)
            Slider(
                value = blurStrength,
                onValueChange = { viewModel.setBlurStrength(it) },
                valueRange = 5f..50f,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Contrast
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.Contrast, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.dark_overlay), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.dark_overlay_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Slider(
            value = bgContrast,
            onValueChange = { viewModel.setBgContrast(it) },
            valueRange = 0f..0.8f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
        Spacer(modifier = Modifier.height(24.dp))

        // Glass Effects
        Text(
            stringResource(R.string.glass_effects),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Icon(Icons.Outlined.BlurLinear, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.card_blur), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.card_blur_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = isCardBlur,
                onCheckedChange = { viewModel.setCardBlurEnabled(it) }
            )
        }

        if (!isCardBlur) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.card_opacity), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Slider(
                value = cardDarkness,
                onValueChange = { viewModel.setCardDarkness(it) },
                valueRange = 0f..1f
            )
        }
    }
}

@Composable
fun EffectsTabContent(viewModel: WallpaperStyleViewModel) {
    val weatherEffect by viewModel.weatherEffect.collectAsState()
    val weatherIntensity by viewModel.weatherIntensity.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            stringResource(R.string.weather_effects),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Effect selection grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherEffectButton(
                label = stringResource(R.string.effect_none),
                isSelected = weatherEffect == WeatherEffect.NONE,
                onClick = { viewModel.setWeatherEffect(WeatherEffect.NONE) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                label = stringResource(R.string.effect_fog),
                isSelected = weatherEffect == WeatherEffect.FOG,
                onClick = { viewModel.setWeatherEffect(WeatherEffect.FOG) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                label = stringResource(R.string.effect_rain),
                isSelected = weatherEffect == WeatherEffect.RAIN,
                onClick = { viewModel.setWeatherEffect(WeatherEffect.RAIN) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherEffectButton(
                label = stringResource(R.string.effect_snow),
                isSelected = weatherEffect == WeatherEffect.SNOW,
                onClick = { viewModel.setWeatherEffect(WeatherEffect.SNOW) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                label = stringResource(R.string.effect_sun_rays),
                isSelected = weatherEffect == WeatherEffect.SUN_RAYS,
                onClick = { viewModel.setWeatherEffect(WeatherEffect.SUN_RAYS) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Intensity slider
        if (weatherEffect != WeatherEffect.NONE) {
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.effect_intensity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.intensity_low), style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(8.dp))
                Slider(
                    value = weatherIntensity,
                    onValueChange = { viewModel.setWeatherIntensity(it) },
                    valueRange = 0.1f..1f,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.intensity_high), style = MaterialTheme.typography.labelSmall)
            }
            Text(
                "${(weatherIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Effect description
        val effectDescription = when (weatherEffect) {
            WeatherEffect.NONE -> stringResource(R.string.effect_desc_none)
            WeatherEffect.FOG -> stringResource(R.string.effect_desc_fog)
            WeatherEffect.RAIN -> stringResource(R.string.effect_desc_rain)
            WeatherEffect.SNOW -> stringResource(R.string.effect_desc_snow)
            WeatherEffect.SUN_RAYS -> stringResource(R.string.effect_desc_sun)
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                effectDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun WeatherEffectButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// ==========================================
// SUB COMPONENTS
// ==========================================

@Composable
fun BgOptionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    icon,
                    null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BackgroundRendererWithEffects(
    viewModel: WallpaperStyleViewModel,
    bgSaturation: Float,
    blurStrength: Float
) {
    val bgType by viewModel.bgType.collectAsState()
    val solidColor by viewModel.solidColor.collectAsState()
    val expressiveThemeId by viewModel.expressiveThemeId.collectAsState()
    val customImageUri by viewModel.backgroundImageUri.collectAsState()
    val isVideo by viewModel.isVideoWallpaper.collectAsState()
    val bgContrast by viewModel.bgContrast.collectAsState()

    Box(Modifier.fillMaxSize()) {
        when (bgType) {
            BgType.SYSTEM -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            BgType.PRESET_SOLID -> Box(Modifier.fillMaxSize().background(Color(solidColor)))
            BgType.EXPRESSIVE -> ExpressiveWallpaperCanvas(themeId = expressiveThemeId)
            BgType.GALLERY -> {
                if (customImageUri != null) {
                    val uri = Uri.parse(customImageUri)
                    
                    val blurModifier = if (blurStrength > 0f) {
                        Modifier.blur(blurStrength.dp)
                    } else {
                        Modifier
                    }
                    
                    if (isVideo) {
                        VideoWallpaperPlayer(
                            uri = uri,
                            modifier = Modifier.fillMaxSize().then(blurModifier)
                        )
                    } else {
                        val saturationModifier = if (bgSaturation != 1f) {
                            Modifier.graphicsLayer {
                                val matrix = ColorMatrix().apply {
                                    setToSaturation(bgSaturation)
                                }
                                this.colorFilter = ColorMatrixColorFilter(matrix)
                            }
                        } else {
                            Modifier
                        }
                        
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().then(blurModifier).then(saturationModifier)
                        )
                    }
                    
                    if (bgContrast > 0f) {
                        Box(
                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgContrast))
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize().background(Color.DarkGray))
                }
            }
        }
    }
}

@Composable
private fun StyleableCardPreview(
    viewModel: WallpaperStyleViewModel,
    hazeState: HazeState,
    content: @Composable ColumnScope.() -> Unit
) {
    val bgType by viewModel.bgType.collectAsState()
    val isHazeEnabled by viewModel.isCardBlur.collectAsState()
    val cardDarkness by viewModel.cardDarkness.collectAsState()
    
    val shape = RoundedCornerShape(16.dp)
    val boxModifier = Modifier.fillMaxWidth().zIndex(1f).clip(shape)

    if (bgType == BgType.SYSTEM) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp), content = content)
        }
    } else {
        if (isHazeEnabled) {
            Box(
                modifier = boxModifier.hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        tints = emptyList(),
                        blurRadius = 20.dp,
                        noiseFactor = 0.05f
                    )
                ).border(1.dp, Color.White.copy(0.2f), shape)
            ) {
                Column(Modifier.padding(12.dp), content = content)
            }
        } else {
            Box(
                modifier = boxModifier.background(
                    MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
                )
            ) {
                Column(Modifier.padding(12.dp), content = content)
            }
        }
    }
}
