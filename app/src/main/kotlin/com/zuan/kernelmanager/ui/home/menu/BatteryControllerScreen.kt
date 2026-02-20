/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.home.menu

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatteryControllerScreen(
    navController: NavController,
    viewModel: BatteryControllerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Wallpaper Style States
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

    // FIX: Ambil theme mode dari settings
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color(0xFF4A6595)
            else -> themeColorName.primary
        }
    }

    // FIX: Hitung dark theme berdasarkan setting aplikasi, bukan sistem HP
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    // TERAPKAN THEME BACKGROUND SEPERTI ABOUT SCREEN
    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(effectivePrimary, isDarkTheme)
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    // Penentuan warna card: Jika custom bg gunakan transparan/darkness, jika tidak gunakan themed card color
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }
    
    // Background utama: Jika custom bg transparan, jika tidak gunakan tinted background dari tema
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else tintedBackground
    
    // Warna text menyesuaikan background
    val textColor = if (isGlassActive || isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (isGlassActive || isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val chargingStats by viewModel.chargingStats.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    
    val hazeState = rememberHazeState()

    LaunchedEffect(Unit) {
        viewModel.loadBatteryData(context)
    }

    Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
        // BACKGROUND LAYER (Custom Wallpaper)
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
                    VideoWallpaperPlayer(uri = uri, modifier = Modifier.fillMaxSize().then(blurModifier))
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
            // Jika tidak custom bg, tetap gunakan hazeSource untuk konsistensi
            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 0f))
        }
        
        // WEATHER OVERLAY
        WeatherEffectOverlay(effect = weatherEffect, intensity = weatherIntensity, modifier = Modifier.fillMaxSize())

        // CONTENT
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { 
                BatteryHeader(
                    navController = navController,
                    batteryInfo = batteryInfo,
                    chargingStats = chargingStats,
                    isCustomBg = isCustomBg,
                    textColor = textColor
                ) 
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = effectivePrimary,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text(stringResource(R.string.battery_tab_dashboard), color = textColor) })
                    Tab(selected = selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text(stringResource(R.string.battery_tab_controls), color = textColor) })
                    Tab(selected = selectedTab == 2, onClick = { viewModel.onTabSelected(2) }, text = { Text(stringResource(R.string.battery_tab_settings), color = textColor) })
                }
                HorizontalDivider(color = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                when (selectedTab) {
                    0 -> BatteryDashboardTab(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, effectivePrimary)
                    1 -> BatteryControlsTab(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, effectivePrimary)
                    2 -> BatterySettingsTab(viewModel, isGlassActive, hazeState, finalCardColor, textColor, subTextColor, effectivePrimary)
                }
            }
        }
    }
}

@Composable
fun BatteryHeader(
    navController: NavController, 
    batteryInfo: BatteryInfo?, 
    chargingStats: ChargingStats?,
    isCustomBg: Boolean,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCustomBg) Color.Transparent else MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) { 
                Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.btn_back), tint = textColor) 
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.battery_statistics_title), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = textColor)
        }
    }
}

/**
 * GENERATE TINTED BACKGROUND - CARA KERJA SEPERTI ABOUT SCREEN
 * Light Mode: 97% White + 3% Primary = Background soft ber-tint
 * Dark Mode: Primary dikurangi intensitas 90%, blend dengan dark base
 */
@Composable
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
        // Dark Mode: Nuansa primary yang sangat gelap, hampir hitam
        // Primary dikurangi intensitasnya 90%, lalu di-blend dengan dark base
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
        // Formula: 95% White + 5% Primary = Warna soft yang tetap berhue sama
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
        
        // Card color: lebih terang tapi tetap ada nuansa
        val cardColor = Color(
            red = 1f,
            green = 1f,
            blue = 1f,
            alpha = 1f
        ) // Putih bersih untuk card (Material 3 standard)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun GlassBatteryCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(state = hazeState, style = HazeStyle(backgroundColor = cardColor.copy(alpha = 0.5f), blurRadius = 24.dp, noiseFactor = 0.1f, tints = emptyList()))
            .border(width = 1.dp, brush = Brush.linearGradient(colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f))), shape = shape)
    } else {
        Modifier.clip(shape)
    }
    Card(modifier = modifier.then(glassModifier), shape = shape, colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardColor)) {
        content()
    }
}

// Update Dashboard Tab dengan glass support
@Composable
fun BatteryDashboardTab(
    viewModel: BatteryControllerViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val info by viewModel.batteryInfo.collectAsStateWithLifecycle()
    val stats by viewModel.chargingStats.collectAsStateWithLifecycle()
    val batteryCapacity by viewModel.batteryCapacity.collectAsStateWithLifecycle()
    val peakTemp by viewModel.peakTemp.collectAsStateWithLifecycle()
    val peakCharge by viewModel.peakChargeCurrent.collectAsStateWithLifecycle()
    val peakDischarge by viewModel.peakDischargeCurrent.collectAsStateWithLifecycle()
    val history by viewModel.currentHistory.collectAsStateWithLifecycle()

    if (info == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
            CircularWavyProgressIndicator(modifier = Modifier.size(48.dp), color = primaryColor) 
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassBatteryCard(isGlassActive = isGlassActive, hazeState = hazeState, cardColor = cardColor, modifier = Modifier.weight(1f).aspectRatio(1f)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator(
                            progress = { info!!.percentage / 100f },
                            modifier = Modifier.size(110.dp),
                            color = if (info!!.isCharging) Color(0xFF4CAF50) else primaryColor,
                            trackColor = if (isGlassActive) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${info!!.percentage}%", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = textColor)
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f).aspectRatio(1f), verticalArrangement = Arrangement.SpaceBetween) {
                    MiniInfoCardGlass(
                        label = stringResource(R.string.battery_status), 
                        value = if (info!!.isCharging) stringResource(R.string.battery_charging) else stringResource(R.string.battery_discharging), 
                        icon = Icons.Rounded.Power, 
                        isGlassActive = isGlassActive, 
                        textColor = textColor, 
                        subTextColor = subTextColor
                    )
                    MiniInfoCardGlass(
                        label = stringResource(R.string.battery_temp), 
                        value = "${info!!.temperature}°C", 
                        icon = Icons.Rounded.Thermostat, 
                        isGlassActive = isGlassActive, 
                        textColor = textColor, 
                        subTextColor = subTextColor
                    )
                    MiniInfoCardGlass(
                        label = stringResource(R.string.battery_voltage), 
                        value = "%.3fv".format(info!!.voltage / 1000f), 
                        icon = Icons.Rounded.ElectricBolt, 
                        isGlassActive = isGlassActive, 
                        textColor = textColor, 
                        subTextColor = subTextColor
                    )
                }
            }
        }

        item {
            BatterySectionTitleGlass(stringResource(R.string.battery_peak_records), textColor)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                PeakWavyGaugeGlass(stringResource(R.string.battery_temp_max), "${peakTemp}°C", peakTemp / 50f, Color(0xFFFF9800), isGlassActive, textColor)
                PeakWavyGaugeGlass(stringResource(R.string.battery_discharge), "-${peakDischarge}mA", peakDischarge / 5000f, Color(0xFF2196F3), isGlassActive, textColor)
                PeakWavyGaugeGlass(stringResource(R.string.battery_charge_max), "+${peakCharge}mA", peakCharge / 6000f, Color(0xFF4CAF50), isGlassActive, textColor)
            }
        }

        item {
            BatterySectionTitleGlass(stringResource(R.string.battery_current_live_chart), textColor)
            GlassBatteryCard(isGlassActive = isGlassActive, hazeState = hazeState, cardColor = cardColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(240.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentLabel = if (info!!.isCharging) "+${info!!.currentNow} mA" else "-${info!!.currentNow} mA"
                    Text(currentLabel, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = if (info!!.isCharging) Color(0xFF4CAF50) else primaryColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    LiveRoundedBarChartGlass(data = history, isGlassActive = isGlassActive, primaryColor = primaryColor)
                }
            }
        }

        item {
            BatterySectionTitleGlass(stringResource(R.string.battery_bms_data), textColor)
            GlassBatteryCard(isGlassActive = isGlassActive, hazeState = hazeState, cardColor = cardColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val pwr = if (stats != null) String.format("%.2f W", stats!!.chargingPower) else "N/A"
                    BmsRowGlass(stringResource(R.string.battery_discharge_charge_speed), "${info!!.currentNow} mA", textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_charging_type), info!!.chargingType, textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_level), "${info!!.percentage}%", textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_current_voltage), "%.3f v".format(info!!.voltage / 1000f), textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_max_voltage), "%.3f v".format((stats?.maxVoltage ?: 0) / 1000f), textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_power_watt), pwr, textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_temp_label), "${info!!.temperature}°C", textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_tech), info!!.technology, textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_cycle_count), "${info!!.cycleCount}", textColor, subTextColor)
                    BmsRowGlass(stringResource(R.string.battery_real_capacity), "$batteryCapacity %", textColor, subTextColor)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun MiniInfoCardGlass(label: String, value: String, icon: ImageVector, isGlassActive: Boolean, textColor: Color, subTextColor: Color) {
    Card(modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = subTextColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = subTextColor)
                Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = textColor)
            }
        }
    }
}

@Composable
fun PeakWavyGaugeGlass(title: String, value: String, progress: Float, color: Color, isGlassActive: Boolean, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(60.dp),
                color = color,
                trackColor = if (isGlassActive) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = textColor)
        Text(title, style = MaterialTheme.typography.labelSmall, color = textColor.copy(0.7f))
    }
}

@Composable
fun LiveRoundedBarChartGlass(data: List<ChartPoint>, isGlassActive: Boolean, primaryColor: Color) {
    if (data.isEmpty()) return
    val textMeasurer = rememberTextMeasurer()
    val labelColor = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val chargeColor = Color(0xFF4CAF50)
    val dischargeColor = primaryColor
    val highlightColor = MaterialTheme.colorScheme.tertiary
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxVal = data.maxOfOrNull { it.value }?.coerceAtLeast(100) ?: 100
        val textHeight = 40.dp.toPx()
        val graphHeight = size.height - (textHeight * 2)
        val barCount = 18
        val stepX = size.width / barCount
        val barWidth = stepX * 0.65f
        val cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)

        drawLine(color = labelColor.copy(alpha = 0.2f), start = Offset(0f, textHeight + graphHeight), end = Offset(size.width, textHeight + graphHeight), strokeWidth = 2f)

        data.forEachIndexed { i, point ->
            val barHeight = (point.value.toFloat() / maxVal) * graphHeight
            val xOffset = (i * stepX) + (stepX - barWidth) / 2
            val yOffset = textHeight + graphHeight - barHeight
            val isLatest = i == data.lastIndex
            val barColor = when {
                isLatest -> highlightColor
                point.isCharging -> chargeColor
                else -> dischargeColor
            }

            drawRoundRect(color = barColor, topLeft = Offset(xOffset, yOffset), size = Size(barWidth, barHeight.coerceAtLeast(10f)), cornerRadius = cornerRadius)

            if (isLatest || i % 4 == 0) {
                val valueText = "${point.value}"
                val textLayoutResult = textMeasurer.measure(text = valueText, style = TextStyle(color = labelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold))
                drawText(textLayoutResult = textLayoutResult, topLeft = Offset(x = xOffset + (barWidth / 2) - (textLayoutResult.size.width / 2), y = yOffset - textLayoutResult.size.height - 8f))
            }

            if (i == 0 || i == data.lastIndex || i % 4 == 0) {
                val timeText = timeFormat.format(Date(point.timeStamp))
                val timeLayout = textMeasurer.measure(text = timeText, style = TextStyle(color = labelColor.copy(alpha=0.7f), fontSize = 9.sp))
                drawText(textLayoutResult = timeLayout, topLeft = Offset(x = xOffset + (barWidth / 2) - (timeLayout.size.width / 2), y = textHeight + graphHeight + 12f))
            }
        }
    }
}

@Composable
fun BmsRowGlass(label: String, value: String, textColor: Color, subTextColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = subTextColor)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = textColor)
    }
}

@Composable
fun BatterySectionTitleGlass(title: String, textColor: Color) {
    Text(text = title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = textColor.copy(0.9f), modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
}

// Controls & Settings Tab juga perlu diupdate dengan glass support
@Composable
fun BatteryControlsTab(
    viewModel: BatteryControllerViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val context = LocalContext.current
    val isChargingEnabled by viewModel.isChargingEnabled.collectAsStateWithLifecycle()
    val isFastChargeEnabled by viewModel.isFastChargeEnabled.collectAsStateWithLifecycle()
    val isFastChargeSupported by viewModel.isFastChargeSupported.collectAsStateWithLifecycle()
    val isBatterySaverEnabled by viewModel.isBatterySaverEnabled.collectAsStateWithLifecycle()
    val chargingSpeed by viewModel.chargingSpeed.collectAsStateWithLifecycle()
    val isBypassEnabled by viewModel.isBypassEnabled.collectAsStateWithLifecycle()
    val isBypassSupported by viewModel.isBypassSupported.collectAsStateWithLifecycle()
    val smartCutoffEnabled by viewModel.smartCutoffEnabled.collectAsStateWithLifecycle()
    val smartCutoffLimit by viewModel.smartCutoffLimit.collectAsStateWithLifecycle()
    val isSmartChargeSupported by viewModel.isSmartChargeSupported.collectAsStateWithLifecycle()
    val monitorEnabled by viewModel.monitorEnabled.collectAsStateWithLifecycle()
    val hasThermalSconfig by viewModel.hasThermalSconfig.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { ModernControlCardGlass(stringResource(R.string.battery_monitor_service), stringResource(R.string.battery_monitor_service_desc), Icons.Rounded.Visibility, monitorEnabled, isGlassActive, textColor, subTextColor, primaryColor) { viewModel.toggleMonitor(context, it) } }
        item { ModernControlCardGlass(stringResource(R.string.battery_enable_charging), stringResource(R.string.battery_enable_charging_desc), Icons.Rounded.Power, isChargingEnabled, isGlassActive, textColor, subTextColor, primaryColor) { viewModel.toggleCharging(it) } }
        
        if (isSmartChargeSupported) {
            item { SmartCutoffCardGlass(smartCutoffEnabled, smartCutoffLimit, { viewModel.toggleSmartCutoff(context, it) }, { viewModel.setSmartCutoffLimit(context, it) }, isGlassActive, hazeState, cardColor, textColor, subTextColor, primaryColor) }
        }
        
        if (hasThermalSconfig) {
            item { ThermalProfileCardGlass(viewModel, isGlassActive, hazeState, cardColor, textColor, subTextColor, primaryColor) }
        }
        
        if (isFastChargeSupported) {
            item { ModernControlCardGlass(stringResource(R.string.battery_fast_charging), stringResource(R.string.battery_fast_charging_desc), Icons.Rounded.Speed, isFastChargeEnabled, isGlassActive, textColor, subTextColor, primaryColor) { viewModel.toggleFastCharge(it) } }
        }
        
        if (isBypassSupported) {
            item { ModernControlCardGlass(stringResource(R.string.battery_bypass_charging), stringResource(R.string.battery_bypass_charging_desc), Icons.Rounded.PowerOff, isBypassEnabled, isGlassActive, textColor, subTextColor, primaryColor) { viewModel.toggleBypass(it) } }
        }
        
        item {
            BatterySectionTitleGlass(stringResource(R.string.battery_charging_speed_limit), textColor)
            FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.chargingSpeedOptions.forEach { speed ->
                    FilterChip(selected = speed == chargingSpeed, onClick = { viewModel.setChargingSpeed(speed) }, label = { Text("${speed}mA", color = textColor) })
                }
            }
        }
        
        item { ModernControlCardGlass(stringResource(R.string.battery_saver_mode), stringResource(R.string.battery_saver_mode_desc), Icons.Rounded.EnergySavingsLeaf, isBatterySaverEnabled, isGlassActive, textColor, subTextColor, primaryColor) { viewModel.toggleBatterySaver(it) } }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun ModernControlCardGlass(title: String, subtitle: String, icon: ImageVector, checked: Boolean, isGlassActive: Boolean, textColor: Color, subTextColor: Color, primaryColor: Color, onCheckedChange: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onCheckedChange(!checked) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainer)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (checked) primaryColor else if (isGlassActive) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = if (checked) Color.White else subTextColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subTextColor)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SmartCutoffCardGlass(enabled: Boolean, limit: Float, onToggle: (Boolean) -> Unit, onLimitChange: (Float) -> Unit, isGlassActive: Boolean, hazeState: HazeState, cardColor: Color, textColor: Color, subTextColor: Color, primaryColor: Color) {
    GlassBatteryCard(isGlassActive = isGlassActive, hazeState = hazeState, cardColor = cardColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(primaryColor.copy(0.2f)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_battery_android_frame_shield), contentDescription = null, tint = primaryColor)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.battery_smart_cutoff), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                    Text(stringResource(R.string.battery_smart_cutoff_desc), style = MaterialTheme.typography.bodySmall, color = subTextColor)
                }
                Switch(checked = enabled, onCheckedChange = onToggle)
            }
            if (enabled) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.battery_limit_label, limit.toInt()), style = MaterialTheme.typography.labelMedium, color = primaryColor, fontWeight = FontWeight.Bold)
                Slider(value = limit, onValueChange = onLimitChange, valueRange = 50f..100f, steps = 9)
            }
        }
    }
}

@Composable
fun ThermalProfileCardGlass(viewModel: BatteryControllerViewModel, isGlassActive: Boolean, hazeState: HazeState, cardColor: Color, textColor: Color, subTextColor: Color, primaryColor: Color) {
    val currentProfile by viewModel.thermalSconfig.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    val profileName = when(currentProfile) { 
        "0" -> stringResource(R.string.battery_thermal_default)
        "13" -> stringResource(R.string.battery_thermal_gaming)
        "10" -> stringResource(R.string.battery_thermal_benchmark)
        else -> "Custom ($currentProfile)" 
    }
    
    GlassBatteryCard(isGlassActive = isGlassActive, hazeState = hazeState, cardColor = cardColor, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { showDialog = true }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE67E22).copy(alpha=0.2f)), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_battery_profile), contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.battery_thermal_profile), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = textColor)
                Text(profileName, style = MaterialTheme.typography.bodySmall, color = subTextColor)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = subTextColor)
        }
    }
    
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false }, 
            title = { Text(stringResource(R.string.battery_select_thermal_profile)) }, 
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { 
                    TextButton(onClick = { viewModel.updateThermalSconfig("0"); showDialog = false }) { 
                        Text(stringResource(R.string.battery_thermal_default), modifier = Modifier.fillMaxWidth()) 
                    } 
                    TextButton(onClick = { viewModel.updateThermalSconfig("13"); showDialog = false }) { 
                        Text(stringResource(R.string.battery_thermal_gaming), modifier = Modifier.fillMaxWidth()) 
                    } 
                    TextButton(onClick = { viewModel.updateThermalSconfig("10"); showDialog = false }) { 
                        Text(stringResource(R.string.battery_thermal_benchmark), modifier = Modifier.fillMaxWidth()) 
                    } 
                } 
            }, 
            confirmButton = { 
                TextButton(onClick = { showDialog = false }) { 
                    Text(stringResource(R.string.btn_cancel)) 
                } 
            }
        )
    }
}

@Composable
fun BatterySettingsTab(
    viewModel: BatteryControllerViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color,
    primaryColor: Color
) {
    val chargingLimit by viewModel.chargingLimit.collectAsStateWithLifecycle()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { BatterySectionTitleGlass(stringResource(R.string.battery_system_charging_limit), textColor) }
        item { 
            Text(stringResource(R.string.battery_system_charging_limit_desc), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp), color = subTextColor)
            FlowRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.chargingLimitOptions.forEach { limit ->
                    FilterChip(selected = limit == chargingLimit, onClick = { viewModel.setChargingLimit(limit) }, label = { Text("$limit%", color = textColor) })
                }
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)); BatterySectionTitleGlass(stringResource(R.string.battery_health_tips), textColor) }
        item { BatteryTipCardGlass(Icons.Rounded.AcUnit, stringResource(R.string.battery_tip_temp_title), stringResource(R.string.battery_tip_temp_desc), isGlassActive, textColor, subTextColor) }
        item { BatteryTipCardGlass(Icons.Rounded.BatteryChargingFull, stringResource(R.string.battery_tip_discharge_title), stringResource(R.string.battery_tip_discharge_desc), isGlassActive, textColor, subTextColor) }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun BatteryTipCardGlass(icon: ImageVector, title: String, description: String, isGlassActive: Boolean, textColor: Color, subTextColor: Color) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.White.copy(0.1f) else MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor.copy(0.8f), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = textColor)
                Text(description, style = MaterialTheme.typography.bodySmall, color = subTextColor)
            }
        }
    }
}
