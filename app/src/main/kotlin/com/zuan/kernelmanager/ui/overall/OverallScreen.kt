/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.overall

import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.components.battery.BatteryWeatherCard
import com.zuan.kernelmanager.ui.settings.BgType 
import com.zuan.kernelmanager.ui.settings.SettingsViewModel 
import com.zuan.kernelmanager.utils.Utils
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint 
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import kotlin.math.abs

// --- COLOR PALETTE ---
val ColorBrandPrimary @Composable get() = MaterialTheme.colorScheme.primary
val ColorBrandSecondary @Composable get() = MaterialTheme.colorScheme.secondary  
val ColorBrandTertiary @Composable get() = MaterialTheme.colorScheme.tertiary
val ColorBrandError = Color(0xFFEF4444) 

// [TAMBAH] Icon Container Style dari AboutScreen (Expressive)
@Composable
fun OverallIconContainer(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    iconColor: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * [UPDATE] Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan AboutScreen:
 * Light: 97% White + 3% Primary
 * Dark: Primary dikurangi 92% (×0.08), blend dengan #0A0A0A
 */
@Composable
fun generateThemeBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
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

fun getAlgorithmColor(algorithm: String): Color {
    return when (algorithm.lowercase()) {
        "lz4" -> Color(0xFF10B981)
        "zstd" -> Color(0xFF3B82F6)
        "lzo" -> Color(0xFFF59E0B)
        "lz4hc" -> Color(0xFF8B5CF6)
        "deflate" -> Color(0xFFEF4444)
        "842" -> Color(0xFFEC4899)
        else -> Color(0xFF6B7280)
    }
}

@Composable
fun OverallScreen(
    navController: NavController,
    hazeState: HazeState,
    viewModel: OverallViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel() 
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()
    val state by viewModel.uiState.collectAsState()
    val manufacturer = remember { android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() } }
    
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val isCardBlur by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    // [FIX] Ambil colorScheme dari MaterialTheme untuk dynamic color
    val colorScheme = MaterialTheme.colorScheme
    val isSystemDark = colorScheme.surface.luminance() < 0.5f
    val isCustomBg = bgType != BgType.SYSTEM

    // [FIX] Logic dynamic color yang benar (sama seperti AboutScreen)
    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified  // [FIX] Jangan hardcode biru, biarkan MaterialTheme handle
            else -> themeColorName.primary
        }
    }

    // [FIX] Tentukan finalPrimary dengan fallback ke colorScheme.primary jika unspecified
    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        colorScheme.primary  // [FIX] Ini ambil dynamic color dari sistem/wallpaper
    } else {
        effectivePrimary
    }

    // [UPDATE] Gunakan generateThemeBackgroundColors yang sudah diupdate formula AboutScreen
    val (themeBgColor, themeCardColor) = generateThemeBackgroundColors(isSystemDark, finalPrimary)

    // [HAPUS] Variabel redundant yang tidak lagi digunakan
    // val customLightCard = Color(0xFFFFFFFF)
    // val customDarkCard = Color(0xFF1D2733)
    // val customLightBg = themeBgColor 
    // val customDarkBg = themeBgColor

    // [UPDATE] Langsung gunakan hasil dari generateThemeBackgroundColors
    val targetCardColor = themeCardColor
    val targetBgColor = themeBgColor
    
    val shouldUseDarkText = !isSystemDark && !isCustomBg
    val customOnSurface = if (shouldUseDarkText) Color(0xFF111111) else Color(0xFFEEEEEE)
    val customOnSurfaceVariant = if (shouldUseDarkText) Color(0xFF666666) else Color(0xFFAAAAAA)

    val isGlassActive = isCardBlur && isCustomBg
    
    // [UPDATE] Background utama mengikuti themeBgColor (tinted soft)
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else targetBgColor 
    
    val cardShape = RoundedCornerShape(28.dp)
    
    val customWidgetHazeStyle = HazeStyle(
        backgroundColor = targetCardColor.copy(alpha = 0.5f),
        blurRadius = 30.dp,
        noiseFactor = 0.0f,
        tints = emptyList() 
    )

    // [UPDATE] AppBar juga mengikuti themeBgColor agar konsisten
    val appBarHazeStyle = HazeStyle(
        backgroundColor = themeBgColor.copy(alpha = 0.5f), 
        blurRadius = 24.dp,
        noiseFactor = 0.1f, 
        tints = listOf(HazeTint(themeBgColor.copy(alpha = 0.4f))) 
    )
    
    val baseWidgetColor = when {
        isGlassActive -> Color.Transparent 
        isCustomBg -> Color.Black.copy(alpha = cardDarkness) 
        else -> targetCardColor 
    }

    val baseWidgetModifier = if (isGlassActive) {
        Modifier
            .clip(cardShape)
            .hazeEffect(state = hazeState, style = customWidgetHazeStyle)
            .border(1.dp, Color.White.copy(alpha = 0.15f), cardShape)
    } else {
        Modifier.clip(cardShape)
    }

    val updateDialogState = rememberDialogState(initiallyVisible = false)

    LaunchedEffect(Unit) {
        val currentVer = Utils.getAppVersionName(context)
        viewModel.start(currentVer, context)
    }

    val isBuggyAndroid12 = Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2

    val isCollapsed by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.6f } }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    if (state.updateRelease != null) {
        UpdateChangelogDialog(
            state = updateDialogState,
            release = state.updateRelease!!,
            hazeState = hazeState,
            onUpdate = { url ->
                updateDialogState.visible = false
                val fileName = "ZKM_${state.updateRelease!!.tag_name}.apk"
                Utils.downloadAndInstallApk(context, url, fileName)
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ -> 
        Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
            Column(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState, zIndex = 1f).verticalScroll(scrollState)) {
                Spacer(modifier = Modifier.height(headerHeightDp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()))
                
                Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DeviceHeroSection(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)

                    state.updateRelease?.let { release ->
                        val updateCardColor = if (isGlassActive || isCustomBg) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer
                        UpdateCard(release, baseWidgetModifier, updateCardColor, { updateDialogState.visible = true }, customOnSurface)
                    }
                    
                    SectionTitle(stringResource(R.string.overall_dashboard), customOnSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).heightIn(min = 240.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BatteryWeatherCard(
                            info = state.batteryInfo,
                            modifier = baseWidgetModifier.weight(1f).fillMaxHeight(),
                            onClick = { }
                        )
                        OngoingInfoCard(
                            modifier = baseWidgetModifier.weight(1f).fillMaxHeight(),
                            info = state.batteryInfo,
                            containerColor = baseWidgetColor,
                            contentColor = customOnSurface,
                            subColor = customOnSurfaceVariant
                        )
                    }
                    
                    DetailedBatteryStats(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)
        
                    SectionTitle(stringResource(R.string.overall_processor), customOnSurface)
                    CPUMonitorWidget(
                        state = state, 
                        modifier = baseWidgetModifier, 
                        containerColor = baseWidgetColor, 
                        textColor = customOnSurface, 
                        subTextColor = customOnSurfaceVariant,
                        onOpenProcessManager = { navController.navigate(NavigationRoute.ProcessManager.route) },
                        onCoreClick = { index -> viewModel.loadCoreDetail(index) } 
                    )
        
                    SectionTitle(stringResource(R.string.overall_graphics), customOnSurface)
                    GPUWidget(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)
        
                    SectionTitle(stringResource(R.string.overall_memory), customOnSurface)
                    MemoryWidget(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)

                    SectionTitle(stringResource(R.string.overall_vm_params), customOnSurface)
                    VMParametersWidget(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)

                    SectionTitle(stringResource(R.string.overall_display), customOnSurface)
                    DisplayInfoWidget(state, baseWidgetModifier, baseWidgetColor, customOnSurface, customOnSurfaceVariant)
                }
            }

            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isCollapsed,
                        transitionSpec = { (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 }).togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 }) },
                        label = "AppBarTitleAnim"
                    ) { collapsed ->
                        if (collapsed) {
                            Column {
                                Text("$manufacturer ${state.deviceModel}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = customOnSurface)
                                if (state.updateRelease != null) Text(stringResource(R.string.update_available, state.updateRelease!!.tag_name), style = MaterialTheme.typography.labelSmall, color = ColorBrandSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                else Text(state.deviceCodename, style = MaterialTheme.typography.labelSmall, color = customOnSurfaceVariant, maxLines = 1)
                            }
                        } else {
                            Column {
                                Text(stringResource(R.string.overall_title), fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, color = customOnSurface)
                                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium, color = customOnSurfaceVariant, fontWeight = FontWeight.Normal)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = Color.Transparent, scrolledContainerColor = Color.Transparent, actionIconContentColor = customOnSurface, navigationIconContentColor = customOnSurface),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.align(Alignment.TopCenter).onGloballyPositioned { headerHeightPx = it.size.height }.hazeEffect(state = hazeState, style = appBarHazeStyle) { if (isBuggyAndroid12) forceInvalidateOnPreDraw = true }
            )

            if (state.selectedCoreInfo != null) {
                CoreDetailDialogOverlay(
                    info = state.selectedCoreInfo!!,
                    onDismiss = { viewModel.dismissCoreDetail() },
                    onSettings = {
                        viewModel.dismissCoreDetail()
                        navController.navigate(NavigationRoute.SoC.route)
                    },
                    hazeState = hazeState
                )
            }
        }
    }
}

@Composable
fun CoreDetailDialogOverlay(
    info: CoreDetailInfo,
    onDismiss: () -> Unit,
    onSettings: () -> Unit,
    hazeState: HazeState
) {
    BackHandler(onBack = onDismiss)

    val isDark = isSystemInDarkTheme()
    val iosCardColor = if (isDark) Color(0xFF252525).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.85f)
    val titleColor = if (isDark) Color.White else Color.Black
    val subtitleColor = if (isDark) Color.Gray else Color.DarkGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 15.dp, 
                        backgroundColor = Color.Black.copy(alpha = 0.2f), 
                        tints = emptyList() 
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        )

        AnimatedVisibility(
            visible = true,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(150)) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 30.dp,
                            backgroundColor = iosCardColor,
                            tints = emptyList() 
                        )
                    )
                    .border(
                        1.dp, 
                        Color.White.copy(alpha = if(isDark) 0.1f else 0.4f), 
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = Color(0xFFD84315),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.core_detail_title, info.coreIndex),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.core_detail_governor), style = MaterialTheme.typography.titleMedium, color = subtitleColor)
                        Text(info.governor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD84315))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = subtitleColor.copy(alpha=0.15f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.core_detail_params), style = MaterialTheme.typography.labelLarge, color = subtitleColor, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if(isDark) Color.Black.copy(alpha=0.2f) else Color(0xFFF2F2F7))
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 250.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (info.tunables.isEmpty()) {
                                Text(stringResource(R.string.core_detail_no_params), style = MaterialTheme.typography.bodySmall, color = subtitleColor)
                            } else {
                                info.tunables.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(key, style = MaterialTheme.typography.bodySmall, color = subtitleColor, modifier = Modifier.weight(1f))
                                        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = titleColor, textAlign = TextAlign.End)
                                    }
                                    if (key != info.tunables.keys.last()) {
                                        HorizontalDivider(color = subtitleColor.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(isDark) Color.White.copy(alpha=0.1f) else Color.White,
                                contentColor = titleColor
                            ),
                            border = if(!isDark) BorderStroke(1.dp, Color.LightGray) else null,
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) { Text(stringResource(R.string.core_detail_cancel), fontWeight = FontWeight.SemiBold) }
                        
                        Button(
                            onClick = onSettings,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315), contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) { Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.core_detail_settings), fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
fun CPUMonitorWidget(
    state: DashboardState, 
    modifier: Modifier, 
    containerColor: Color, 
    textColor: Color, 
    subTextColor: Color,
    onOpenProcessManager: () -> Unit,
    onCoreClick: (Int) -> Unit 
) {
    OSWidgetContainer(modifier, containerColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.cpu_total_load), style = MaterialTheme.typography.labelLarge, color = subTextColor); Spacer(Modifier.weight(1f))
            Text(state.currentCpuLoad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.height(120.dp).fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            state.cpuHistory.forEach { load ->
                val animatedHeight by animateFloatAsState(targetValue = load.coerceIn(0.1f, 1f), label = "BarHeight")
                Box(Modifier.weight(1f).padding(horizontal = 4.dp).fillMaxHeight(animatedHeight).clip(RoundedCornerShape(24.dp)).background(if (load > 0.8f) ColorBrandTertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)))
            }
        }
        Spacer(Modifier.height(24.dp)); HorizontalDivider(color = subTextColor.copy(alpha = 0.2f)); Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.cpu_core_usage), style = MaterialTheme.typography.labelMedium, color = subTextColor); Spacer(Modifier.height(12.dp))
        if (state.cpuFreqs.size >= 8 && state.perCoreLoads.size >= 8) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    CompactCoreItem(7, state.cpuFreqs[7], state.perCoreLoads[7], true, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(6, state.cpuFreqs[6], state.perCoreLoads[6], false, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(5, state.cpuFreqs[5], state.perCoreLoads[5], false, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(4, state.cpuFreqs[4], state.perCoreLoads[4], false, textColor, subTextColor, onCoreClick)
                }
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    CompactCoreItem(3, state.cpuFreqs[3], state.perCoreLoads[3], false, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(2, state.cpuFreqs[2], state.perCoreLoads[2], false, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(1, state.cpuFreqs[1], state.perCoreLoads[1], false, textColor, subTextColor, onCoreClick)
                    CompactCoreItem(0, state.cpuFreqs[0], state.perCoreLoads[0], false, textColor, subTextColor, onCoreClick)
                }
            }
        } else { Text(stringResource(R.string.cpu_loading), style = MaterialTheme.typography.bodySmall, color = subTextColor) }
        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = subTextColor.copy(alpha = 0.2f)); Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onOpenProcessManager() }.padding(vertical = 4.dp), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.cpu_top_processes), style = MaterialTheme.typography.labelMedium, color = subTextColor)
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = stringResource(R.string.cpu_view_all), tint = subTextColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.processList.take(5).forEach { process -> 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onOpenProcessManager() }.padding(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    if (process.icon != null) {
                        Image(bitmap = process.icon, contentDescription = null, modifier = Modifier.size(32.dp))
                    } else {
                        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                             Text(process.name.take(1).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(process.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, color = textColor, fontWeight = FontWeight.Medium)
                        Text(process.packageName, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = subTextColor, fontSize = 10.sp)
                    }
                    Text("${process.cpuUsage}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }
    }
}

@Composable
fun CompactCoreItem(index: Int, freq: String, load: Int, isPrime: Boolean = false, textColor: Color, subTextColor: Color, onClick: (Int) -> Unit) {
    Column(Modifier.width(74.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick(index) }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val barColor = if (isPrime) ColorBrandTertiary else MaterialTheme.colorScheme.primary
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(16.dp)) {
            for (i in 1..4) {
                val isActive = load >= (i * 25 - 15); val barHeight = 0.4f + (0.15f * i)
                Box(Modifier.width(4.dp).fillMaxHeight(barHeight).clip(RoundedCornerShape(1.dp)).background(if (isActive) barColor else subTextColor.copy(alpha=0.3f)))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("$load%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = textColor)
        Text(freq, style = MaterialTheme.typography.labelSmall, color = subTextColor, fontSize = 9.sp, maxLines = 1)
    }
}

@Composable
fun GPUWidget(state: DashboardState, modifier: Modifier, containerColor: Color, textColor: Color, subTextColor: Color) {
    OSWidgetContainer(modifier, containerColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // [UPDATE] Pakai Icon Container
            OverallIconContainer(
                icon = Icons.Rounded.GraphicEq,
                containerColor = ColorBrandPrimary.copy(alpha = 0.12f),
                iconColor = ColorBrandPrimary,
                size = 44.dp,
                iconSize = 24.dp
            )
            Spacer(Modifier.width(12.dp))
            Column { Text(stringResource(R.string.gpu_load), style = MaterialTheme.typography.labelMedium, color = subTextColor); Text("${state.gpuLoad}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor) }
            Spacer(Modifier.weight(1f))
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                Text(state.gpuFreq, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Spacer(Modifier.height(16.dp)); LinearProgressIndicator(progress = { state.gpuLoad / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = ColorBrandPrimary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = subTextColor.copy(alpha = 0.2f)); Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.gpu_renderer_info), style = MaterialTheme.typography.labelSmall, color = subTextColor); Spacer(Modifier.height(4.dp))
        Text(state.gpuDetail, style = MaterialTheme.typography.bodySmall, color = textColor, lineHeight = 16.sp)
    }
}

@Composable
fun MemoryWidget(state: DashboardState, modifier: Modifier, containerColor: Color, textColor: Color, subTextColor: Color) {
    OSWidgetContainer(modifier, containerColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // [UPDATE] Icon Container untuk RAM
            OverallIconContainer(
                icon = Icons.Rounded.Memory,
                containerColor = ColorBrandTertiary.copy(alpha = 0.12f),
                iconColor = ColorBrandTertiary,
                size = 44.dp,
                iconSize = 24.dp
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.ram_physical), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textColor)
        }
        Spacer(Modifier.height(12.dp))
        ModernProgressBar(
            percentText = "${(state.ramProgress * 100).toInt()}%", 
            progress = state.ramProgress, 
            color = ColorBrandTertiary, 
            used = state.memUsed, 
            free = state.memFree, 
            total = state.memTotal, 
            textColor = textColor, 
            subTextColor = subTextColor
        )
        
        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = subTextColor.copy(alpha = 0.15f))
        Spacer(Modifier.height(20.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // [UPDATE] Icon Container untuk Swap
            OverallIconContainer(
                icon = Icons.Rounded.SwapHoriz,
                containerColor = ColorBrandSecondary.copy(alpha = 0.12f),
                iconColor = ColorBrandSecondary,
                size = 44.dp,
                iconSize = 24.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ram_swap_zram), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textColor)
            }
            
            if (state.zramAlgorithm != "Unknown") {
                val algoColor = getAlgorithmColor(state.zramAlgorithm)
                Surface(
                    color = algoColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, algoColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(algoColor)
                        )
                        Text(
                            state.zramAlgorithm.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = algoColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ModernProgressBar(
            percentText = "${(state.swapProgress * 100).toInt()}%", 
            progress = state.swapProgress, 
            color = ColorBrandSecondary, 
            used = state.swapUsed, 
            free = state.swapFree, 
            total = state.swapTotal, 
            textColor = textColor, 
            subTextColor = subTextColor
        )
    }
}

@Composable
fun ModernProgressBar(percentText: String, progress: Float, color: Color, used: String, free: String, total: String, textColor: Color, subTextColor: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            Text(percentText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(8.dp))
        LinearWavyProgressIndicator(
            progress = { progress }, 
            modifier = Modifier.fillMaxWidth().height(7.dp), 
            color = color, 
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            MemoryDetailItem(stringResource(R.string.ram_used), used, false, textColor, subTextColor)
            MemoryDetailItem(stringResource(R.string.ram_free), free, false, textColor, subTextColor)
            MemoryDetailItem(stringResource(R.string.ram_total), total, true, textColor, subTextColor)
        }
    }
}

@Composable
fun MemoryDetailItem(label: String, value: String, alignEnd: Boolean = false, textColor: Color, subTextColor: Color) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = subTextColor, fontSize = 9.sp)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = textColor)
    }
}

@Composable
fun VMParametersWidget(state: DashboardState, modifier: Modifier, containerColor: Color, textColor: Color, subTextColor: Color) {
    OSWidgetContainer(modifier, containerColor) {
        val vmParams = state.vmParameters
        
        // [UPDATE] Menggunakan theme colors (Primary, Secondary, Tertiary) secara bergantian
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            VMParamItem(
                label = stringResource(R.string.vm_swappiness),
                value = vmParams.swappiness,
                icon = Icons.Rounded.WaterDrop,
                iconColor = ColorBrandPrimary, // Theme Primary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            VMParamItem(
                label = stringResource(R.string.vm_extra_free),
                value = vmParams.extraFreeKbytes,
                icon = Icons.Rounded.AddCircle,
                iconColor = ColorBrandSecondary, // Theme Secondary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            VMParamItem(
                label = stringResource(R.string.vm_watermark),
                value = vmParams.watermarkScaleFactor,
                icon = Icons.Rounded.SignalCellularAlt,
                iconColor = ColorBrandTertiary, // Theme Tertiary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            VMParamItem(
                label = stringResource(R.string.vm_vfs_cache),
                value = vmParams.vfsCachePressure,
                icon = Icons.Rounded.Storage,
                iconColor = ColorBrandPrimary, // Theme Primary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            VMParamItem(
                label = stringResource(R.string.vm_dirty_ratio),
                value = vmParams.dirtyRatio,
                icon = Icons.Rounded.DirtyLens,
                iconColor = ColorBrandSecondary, // Theme Secondary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            VMParamItem(
                label = stringResource(R.string.vm_dirty_bg),
                value = vmParams.dirtyBgRatio,
                icon = Icons.Rounded.CleaningServices,
                iconColor = ColorBrandTertiary, // Theme Tertiary
                textColor = textColor,
                subTextColor = subTextColor
            )
        }
    }
}

@Composable
fun VMParamItem(
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Icon dengan theme color
        OverallIconContainer(
            icon = icon,
            containerColor = iconColor.copy(alpha = 0.12f),
            iconColor = iconColor,
            size = 36.dp,
            iconSize = 18.dp,
            cornerRadius = 10.dp
        )
        
        Spacer(Modifier.width(12.dp))
        
        // Label dan Value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun DisplayInfoWidget(state: DashboardState, modifier: Modifier, containerColor: Color, textColor: Color, subTextColor: Color) {
    OSWidgetContainer(modifier, containerColor) {
        val displayInfo = state.displayInfo
        
        // [UPDATE] Menggunakan theme colors (Primary, Secondary, Tertiary) secara bergantian
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            DisplayInfoRow(
                label = stringResource(R.string.display_resolution),
                value = displayInfo.resolution,
                icon = Icons.Rounded.AspectRatio,
                iconColor = ColorBrandPrimary, // Theme Primary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            DisplayInfoRow(
                label = stringResource(R.string.display_density),
                value = displayInfo.densityDpi,
                subValue = displayInfo.density,
                icon = Icons.Rounded.ZoomIn,
                iconColor = ColorBrandSecondary, // Theme Secondary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            DisplayInfoRow(
                label = stringResource(R.string.display_technology),
                value = displayInfo.technology,
                icon = Icons.Rounded.DisplaySettings,
                iconColor = ColorBrandTertiary, // Theme Tertiary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            DisplayInfoRow(
                label = stringResource(R.string.display_hdr),
                value = displayInfo.hdrSupport,
                icon = Icons.Rounded.HdrOn,
                iconColor = if (displayInfo.hdrSupport.contains("Not")) subTextColor else ColorBrandPrimary, // Theme Primary jika support
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            DisplayInfoRow(
                label = stringResource(R.string.display_refresh_rate),
                value = displayInfo.refreshRate,
                icon = Icons.Rounded.Speed,
                iconColor = ColorBrandSecondary, // Theme Secondary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            DisplayInfoRow(
                label = stringResource(R.string.display_orientation),
                value = displayInfo.orientation,
                icon = Icons.Rounded.ScreenRotation,
                iconColor = ColorBrandTertiary, // Theme Tertiary
                textColor = textColor,
                subTextColor = subTextColor
            )
            
            HorizontalDivider(color = subTextColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
            
            // Physical Size dengan theme color
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                OverallIconContainer(
                    icon = Icons.Rounded.Straighten,
                    containerColor = ColorBrandPrimary.copy(alpha = 0.15f), // Theme Primary
                    iconColor = ColorBrandPrimary,
                    size = 44.dp,
                    iconSize = 24.dp,
                    cornerRadius = 12.dp
                )
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.display_physical_size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = subTextColor,
                        fontSize = 13.sp
                    )
                    Text(
                        displayInfo.diagonalSize,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        displayInfo.physicalSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// [UPDATE] Composable untuk row display info dengan theme color
@Composable
fun DisplayInfoRow(
    label: String,
    value: String,
    subValue: String? = null,
    icon: ImageVector,
    iconColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Icon dengan theme color
        OverallIconContainer(
            icon = icon,
            containerColor = iconColor.copy(alpha = 0.15f),
            iconColor = iconColor,
            size = 36.dp,
            iconSize = 18.dp,
            cornerRadius = 10.dp
        )
        
        Spacer(Modifier.width(12.dp))
        
        // Label dan Value
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = subTextColor,
                fontSize = 13.sp
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 15.sp
                )
                if (subValue != null) {
                    Text(
                        " ($subValue)",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun RoundedPolygon.toShape(): Shape {
    return object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
            val path = android.graphics.Path(); this@toShape.toPath(path)
            val bounds = RectF(); path.computeBounds(bounds, true)
            val matrix = Matrix(); matrix.setRectToRect(bounds, RectF(0f, 0f, size.width, size.height), Matrix.ScaleToFit.FILL)
            path.transform(matrix); return Outline.Generic(path.asComposePath())
        }
    }
}

@Composable
fun TemperatureReactorCard(
    info: com.zuan.kernelmanager.ui.overall.OverallBatteryInfo,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val tempValue = try { info.temp.replace(" °C", "").toFloat() } catch (e: Exception) { 30f }
    val (targetColor, statusText) = when {
        tempValue < 30f -> Color(0xFF00E5FF) to stringResource(R.string.temp_freezing)
        tempValue < 36f -> Color(0xFF00C853) to stringResource(R.string.temp_cool)
        tempValue < 41f -> Color(0xFFFFC107) to stringResource(R.string.temp_warm)
        tempValue < 45f -> Color(0xFFFF5722) to stringResource(R.string.temp_hot)
        else -> Color(0xFFD50000) to stringResource(R.string.temp_overheat)
    }
    val animatedColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(1000), label = "ColorAnim")
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseDuration = when {
        tempValue > 43f -> 600   
        tempValue > 38f -> 1000  
        else -> 2000             
    }
    val pulseScale by infiniteTransition.animateFloat(initialValue = 0.8f, targetValue = 1.1f, animationSpec = infiniteRepeatable(animation = tween(pulseDuration, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "PulseScale")
    val rotationAnim by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "Rotation")

    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.temp_temperature), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = info.temp, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium, color = textColor)
                Surface(color = animatedColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    Text(text = statusText, color = animatedColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(50.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2); val radius = size.width / 2
                    drawCircle(color = textColor.copy(alpha = 0.05f), radius = radius, style = Stroke(width = 4.dp.toPx()))
                    drawCircle(brush = Brush.radialGradient(colors = listOf(animatedColor.copy(alpha = 0.5f), Color.Transparent), center = center, radius = radius * pulseScale), radius = radius * pulseScale)
                    rotate(rotationAnim) { drawArc(brush = Brush.sweepGradient(colors = listOf(Color.Transparent, animatedColor, Color.Transparent)), startAngle = -90f, sweepAngle = 200f, useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)) }
                    drawCircle(color = animatedColor, radius = 4.dp.toPx())
                }
            }
        }
    }
}

@Composable
fun LiveCurrentChart(history: List<Int>, color: Color, modifier: Modifier = Modifier) {
    if (history.isEmpty()) return
    Canvas(modifier = modifier) {
        val min = history.minOrNull() ?: 0; val max = history.maxOrNull() ?: 100
        val range = (max - min).toFloat().coerceAtLeast(1f) 
        val widthPerPoint = size.width / (history.size - 1).coerceAtLeast(1)
        val path = Path()
        history.forEachIndexed { index, value ->
            val x = index * widthPerPoint
            val normalizedY = size.height - ((value - min) / range * size.height)
            if (index == 0) path.moveTo(x, normalizedY) else {
                val prevX = (index - 1) * widthPerPoint
                val prevVal = history[index - 1]
                val prevY = size.height - ((prevVal - min) / range * size.height)
                path.cubicTo(prevX + widthPerPoint / 2, prevY, x - widthPerPoint / 2, normalizedY, x, normalizedY)
            }
        }
        drawPath(path = path, color = color.copy(alpha = 0.5f), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        val fillPath = Path(); fillPath.addPath(path); fillPath.lineTo(size.width, size.height); fillPath.lineTo(0f, size.height); fillPath.close()
        drawPath(path = fillPath, brush = Brush.verticalGradient(colors = listOf(color.copy(alpha = 0.2f), Color.Transparent), startY = 0f, endY = size.height))
    }
}

@Composable
fun OngoingInfoCard(modifier: Modifier = Modifier, info: com.zuan.kernelmanager.ui.overall.OverallBatteryInfo, containerColor: Color, contentColor: Color, subColor: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.battery_details), style = MaterialTheme.typography.bodyMedium, color = subColor)
            
            // [UPDATE] Icon Container untuk Bolt
            Row(verticalAlignment = Alignment.CenterVertically) {
                OverallIconContainer(
                    icon = Icons.Outlined.Bolt,
                    containerColor = Color(0xFFFFC107).copy(alpha = 0.12f),
                    iconColor = Color(0xFFFFC107),
                    size = 40.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column { Text(stringResource(R.string.battery_voltage), style = MaterialTheme.typography.labelSmall, color = subColor); Text(info.voltage.replace(" mV", " V"), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = contentColor) }
            }
            
            // [UPDATE] Icon Container untuk Schedule
            Row(verticalAlignment = Alignment.CenterVertically) {
                OverallIconContainer(
                    icon = Icons.Outlined.Schedule,
                    containerColor = Color(0xFF3B82F6).copy(alpha = 0.12f),
                    iconColor = Color(0xFF3B82F6),
                    size = 40.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column { Text(stringResource(R.string.battery_uptime), style = MaterialTheme.typography.labelSmall, color = subColor); Text(info.uptime, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = contentColor) }
            }
            
            // [UPDATE] Icon Container untuk Bedtime
            Row(verticalAlignment = Alignment.CenterVertically) {
                OverallIconContainer(
                    icon = Icons.Outlined.Bedtime,
                    containerColor = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                    iconColor = Color(0xFF8B5CF6),
                    size = 40.dp,
                    iconSize = 20.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column { Text(stringResource(R.string.battery_deep_sleep), style = MaterialTheme.typography.labelSmall, color = subColor); Text(info.deepSleep.substringBefore(" ("), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = contentColor) }
            }
        }
    }
}

@Composable
fun DetailedBatteryStats(state: DashboardState, modifier: Modifier, containerColor: Color, contentColor: Color, subColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = modifier.fillMaxWidth().height(160.dp), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(0.dp)) {
            Box(modifier = Modifier.fillMaxSize()) {
                LiveCurrentChart(history = state.batteryHistory, color = contentColor, modifier = Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter))
                Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
                     Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.battery_electric_current), style = MaterialTheme.typography.bodyMedium, color = subColor)
                        Icon(Icons.Default.ChevronRight, null, tint = subColor.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "${abs(state.batteryInfo.currentNow)}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Medium, color = contentColor)
                        Text(text = " mA", style = MaterialTheme.typography.bodyLarge, color = subColor, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "%.1fW".format(state.batteryInfo.wattage), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = contentColor)
                        Text(" • ", color = subColor)
                        Text(text = state.batteryInfo.voltage.replace(" ", ""), style = MaterialTheme.typography.bodyMedium, color = subColor)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            Card(modifier = modifier.weight(1f).fillMaxHeight(), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(0.dp)) {
                 Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.battery_health), style = MaterialTheme.typography.bodyMedium, color = subColor)
                        Icon(Icons.Default.ChevronRight, null, tint = subColor.copy(alpha=0.5f), modifier = Modifier.size(16.dp))
                    }
                    val currentMaxVal = state.batteryInfo.maximumCapacity.substringBefore(" mAh").trim().toFloatOrNull() ?: 0f
                    val designVal = state.batteryInfo.designCapacity.substringBefore(" mAh").trim().toFloatOrNull() ?: 1f
                    val healthPercent = if (currentMaxVal > 0) ((currentMaxVal / designVal) * 100).toInt() else 0
                    val displayHealth = if (healthPercent > 200) 100 else healthPercent
                    Text(text = "$displayHealth%", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = contentColor)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // [UPDATE] Icon Container untuk Health
                        OverallIconContainer(
                            icon = Icons.Rounded.Favorite,
                            containerColor = ColorBrandSecondary.copy(alpha=0.12f),
                            iconColor = ColorBrandSecondary,
                            size = 32.dp,
                            iconSize = 16.dp,
                            cornerRadius = 8.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.battery_real_capacity), style = MaterialTheme.typography.labelSmall, color = subColor, fontSize = 10.sp)
                            Text("${currentMaxVal.toInt()} / ${designVal.toInt()}", style = MaterialTheme.typography.labelMedium, color = contentColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                 }
            }
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TemperatureReactorCard(info = state.batteryInfo, modifier = modifier.weight(1f).fillMaxWidth(), containerColor = containerColor, textColor = contentColor)
                Card(modifier = modifier.weight(1f).fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = containerColor), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.battery_cycles), style = MaterialTheme.typography.bodySmall, color = subColor); Icon(Icons.Default.ChevronRight, null, tint = subColor.copy(alpha=0.3f), modifier = Modifier.size(14.dp))
                        }
                        Text(text = state.batteryInfo.cycleCount, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium, color = contentColor)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, textColor: Color) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(start = 4.dp))
}

@Composable
fun UpdateCard(release: GithubRelease, modifier: Modifier, containerColor: Color, onClick: () -> Unit, textColor: Color) {
    OSWidgetContainer(modifier.clickable { onClick() }, containerColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // [UPDATE] Icon Container untuk Update
            OverallIconContainer(
                icon = Icons.Default.SystemUpdate,
                containerColor = MaterialTheme.colorScheme.primary,
                iconColor = Color.White,
                size = 44.dp,
                iconSize = 24.dp,
                cornerRadius = 22.dp // Circle-ish
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(R.string.update_available, release.tag_name), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text(stringResource(R.string.update_tap_install), style = MaterialTheme.typography.bodySmall, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun OSWidgetContainer(modifier: Modifier = Modifier, containerColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(containerColor).padding(20.dp), content = content)
}

@Composable
fun DeviceHeroSection(state: DashboardState, modifier: Modifier, containerColor: Color, textColor: Color, subTextColor: Color) {
    OSWidgetContainer(modifier, containerColor) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(MaterialShapes.Cookie9Sided.toShape()).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(Icons.Rounded.Smartphone, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
            Spacer(Modifier.width(16.dp))
            Column {
                val manufacturer = android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                Text("$manufacturer ${state.deviceModel}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = textColor)
                Text("${state.deviceCodename} • ${state.socName}", style = MaterialTheme.typography.bodyMedium, color = subTextColor)
            }
        }
        Spacer(Modifier.height(20.dp)); HorizontalDivider(color = subTextColor.copy(alpha = 0.2f)); Spacer(Modifier.height(16.dp))
        
        // [UPDATE] Icon Container untuk Android
        Row(verticalAlignment = Alignment.CenterVertically) {
            OverallIconContainer(
                icon = Icons.Outlined.Android,
                containerColor = ColorBrandSecondary.copy(alpha = 0.12f),
                iconColor = ColorBrandSecondary,
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(Modifier.width(12.dp))
            Column { Text(stringResource(R.string.device_android_version), style = MaterialTheme.typography.labelSmall, color = subTextColor); Text("Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = textColor) }
        }
        Spacer(Modifier.height(16.dp))
        
        // [UPDATE] Icon Container untuk Memory/Kernel
        Row(verticalAlignment = Alignment.Top) {
            OverallIconContainer(
                icon = Icons.Outlined.Memory,
                containerColor = ColorBrandPrimary.copy(alpha = 0.12f),
                iconColor = ColorBrandPrimary,
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(Modifier.width(12.dp))
            Column { Text(stringResource(R.string.device_kernel_info), style = MaterialTheme.typography.labelSmall, color = subTextColor); Text(state.kernelVersion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, lineHeight = 20.sp, color = textColor) }
        }
    }
}

@Composable
fun UpdateChangelogDialog(state: com.composables.core.DialogState, release: GithubRelease, hazeState: HazeState, onUpdate: (String) -> Unit) {
    DialogUnstyled(
        state = state,
        hazeState = hazeState,
        isCustomBg = false,
        title = "${stringResource(R.string.home_update_avail)} ${release.tag_name}",
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 250.dp).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.home_update_new), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(release.body, style = MaterialTheme.typography.bodyMedium, lineHeight = 20.sp)
            }
        },
        confirmButton = { DialogTextButton(icon = Icons.Default.CloudDownload, text = "Update", onClick = { val url = release.assets.firstOrNull()?.browser_download_url; if (url != null) onUpdate(url) }, color = MaterialTheme.colorScheme.primary) },
        dismissButton = { DialogTextButton(text = stringResource(R.string.btn_cancel), onClick = { state.visible = false }, color = MaterialTheme.colorScheme.primary) }
    )
}
