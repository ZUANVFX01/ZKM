/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalLayoutApi::class
)

package com.zuan.kernelmanager.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

/**
 * [UPDATE] Generate SOFT TINTED background berdasarkan primary color tema
 * Sama persis dengan AboutScreen & OverallScreen:
 * Light: 97% White + 3% Primary
 * Dark: Primary dikurangi 92% (×0.08), blend dengan #0A0A0A
 */
@Composable
fun generateHomeBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
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
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    navController: NavController,
    hazeState: HazeState
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val extensions by viewModel.filteredExtensions.collectAsStateWithLifecycle(initialValue = emptyList())
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val isCardBlur by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()

    val isSystemDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val isCustomBg = bgType != BgType.SYSTEM
    
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

    // [UPDATE] Gunakan generateHomeBackgroundColors yang sudah diupdate formula AboutScreen
    val (themeBgColor, themeCardColor) = generateHomeBackgroundColors(isSystemDark, finalPrimary)

    // [HAPUS] Variabel redundant yang tidak lagi digunakan
    // val customLightCard = Color(0xFFFFFFFF)
    // val customDarkCard = Color(0xFF1D2733)
    
    // [UPDATE] Langsung gunakan hasil dari generateHomeBackgroundColors
    val targetCardColor = themeCardColor
    val targetBgColor = themeBgColor

    val shouldUseDarkText = !isSystemDark && !isCustomBg
    val customOnSurface = if (shouldUseDarkText) Color(0xFF111111) else Color(0xFFEEEEEE)
    val customOnSurfaceVariant = if (shouldUseDarkText) Color(0xFF666666) else Color(0xFFAAAAAA)

    val isGlassActive = isCardBlur && isCustomBg

    // [UPDATE] Background utama mengikuti themeBgColor (tinted soft)
    val mainBackgroundColor = if (isCustomBg) Color.Transparent else targetBgColor

    // [UPDATE] AppBar juga mengikuti themeBgColor agar konsisten
    val appBarHazeStyle = HazeStyle(
        backgroundColor = themeBgColor.copy(alpha = 0.5f),
        blurRadius = 24.dp,
        noiseFactor = 0.1f,
        tints = listOf(HazeTint(themeBgColor.copy(alpha = 0.4f)))
    )

    val cardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        else -> targetCardColor
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )

    val scrollState = rememberScrollState()
    val isCollapsed by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.7f }
    }

    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    val groupedExtensions = remember(extensions) {
        extensions.groupBy { it.type }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDeviceInfo(context)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(mainBackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(
                    modifier = Modifier.height(
                        headerHeightDp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp
                    )
                )

                DeviceInfoCompact(
                    viewModel = viewModel,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    cardColor = cardColor,
                    textColor = customOnSurface,
                    subTextColor = customOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                groupedExtensions.forEach { (category, items) ->
                    
                    // [UPDATE] Mapping kategori ke string resource
                    val categoryDisplayName = when(category) {
                        "System" -> stringResource(R.string.cat_system)
                        "Monitor" -> stringResource(R.string.cat_monitor)
                        "Root" -> stringResource(R.string.cat_root)
                        else -> category
                    }
                    
                    Text(
                        text = categoryDisplayName.uppercase(), 
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = customOnSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 8.dp, bottom = 8.dp, top = 16.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = 2
                    ) {
                        items.forEach { item ->
                            CompactFeatureCard(
                                item = item,
                                isGlassActive = isGlassActive,
                                hazeState = hazeState,
                                onClick = { navController.navigate(item.route) },
                                modifier = Modifier.weight(1f),
                                cardColor = cardColor,
                                textColor = customOnSurface,
                                subTextColor = customOnSurfaceVariant
                            )
                        }
                        
                        if (items.size % 2 != 0) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }

            LargeTopAppBar(
                title = {
                    AppBarContent(
                        isCollapsed = isCollapsed,
                        scrollFraction = scrollBehavior.state.collapsedFraction,
                        textColor = customOnSurface,
                        subTextColor = customOnSurfaceVariant
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    actionIconContentColor = customOnSurface,
                    navigationIconContentColor = customOnSurface
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onGloballyPositioned { headerHeightPx = it.size.height }
                    .hazeEffect(
                        state = hazeState,
                        style = appBarHazeStyle
                    )
                    .graphicsLayer {
                        alpha = 1f - (scrollBehavior.state.collapsedFraction * 0.3f)
                    }
            )
        }
    }
}

@Composable
private fun DeviceInfoCompact(
    viewModel: HomeViewModel,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    val deviceInfo by viewModel.deviceInfo.collectAsStateWithLifecycle()

    ExpressiveGlassCard(
        isGlassActive = isGlassActive,
        hazeState = hazeState,
        shape = RoundedCornerShape(20.dp),
        cardColor = cardColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Smartphone,
                    contentDescription = stringResource(R.string.home_device_content_desc),
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = deviceInfo.deviceName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
                Text(
                    text = "${deviceInfo.manufacturer} • Android ${deviceInfo.androidVersion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }
        }
    }
}

@Composable
fun CompactFeatureCard(
    item: ExtensionItem,
    isGlassActive: Boolean,
    hazeState: HazeState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val shape = RoundedCornerShape(16.dp)

    val cardModifier = if (isGlassActive) {
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

    Surface(
        modifier = modifier
            .height(72.dp)
            .then(cardModifier)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        shape = shape,
        color = containerColor,
        tonalElevation = if (isGlassActive) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(item.nameRes),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = textColor,
                    maxLines = 1
                )
                
                Text(
                    text = stringResource(item.descriptionRes),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp
                    ),
                    color = subTextColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ExpressiveGlassCard(
    isGlassActive: Boolean,
    hazeState: HazeState,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    cardColor: Color,
    content: @Composable () -> Unit
) {
    val glassModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f),
                    blurRadius = 30.dp,
                    noiseFactor = 0.08f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
    }

    val containerColor = if (isGlassActive) Color.Transparent else cardColor

    Surface(
        modifier = modifier.then(glassModifier),
        shape = shape,
        color = containerColor,
        tonalElevation = if (isGlassActive) 0.dp else 1.dp
    ) {
        content()
    }
}

@Composable
private fun AppBarContent(
    isCollapsed: Boolean,
    scrollFraction: Float,
    textColor: Color,
    subTextColor: Color
) {
    AnimatedContent(
        targetState = isCollapsed,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300, easing = EaseOutQuart)) +
                    slideInVertically { it / 2 })
                .togetherWith(
                    fadeOut(animationSpec = tween(200)) +
                            slideOutVertically { -it / 2 }
                )
        },
        label = "AppBarTransition"
    ) { collapsed ->
        if (collapsed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.home_features),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textColor
                )
            }
        } else {
            Column {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = subTextColor
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.home_dashboard),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = textColor
                )
            }
        }
    }
}
