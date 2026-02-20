/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)

package com.zuan.kernelmanager.ui.about.opensource

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.about.ExpressiveCornerRadius
import com.zuan.kernelmanager.ui.about.IconContainerRadius
import com.zuan.kernelmanager.ui.about.ThemedIconContainer
import com.zuan.kernelmanager.ui.about.generateThemedBackgroundColor
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@Composable
fun OpenSourceScreen(
    navController: NavController,
    hazeState: HazeState,
    viewModel: OpenSourceViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isCollapsed by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f }
    }
    
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val isCustomBg = bgType == BgType.GALLERY
    
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
    
    val (tintedBackground, _) = generateThemedBackgroundColor(finalPrimary, useDarkTheme)
    val appBarHazeStyle = HazeMaterials.regular()
    
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Open Source",
                        style = if (isCollapsed) {
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        },
                        color = onSurfaceColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = onSurfaceColor
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .zIndex(2f)
                    .hazeEffect(state = hazeState, style = appBarHazeStyle)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isCustomBg) Color.Transparent else tintedBackground)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 1f)
                    .padding(horizontal = 16.dp)
            ) {
                TabRow(
                    selectedTabIndex = uiState.selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = finalPrimary,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                            color = finalPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    OpenSourceTab.values().forEach { tab ->
                        Tab(
                            selected = uiState.selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = {
                                Text(
                                    text = when(tab) {
                                        OpenSourceTab.CONTRIBUTORS -> "Contributors"
                                        OpenSourceTab.LIBRARIES -> "Libraries"
                                    },
                                    fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = when(tab) {
                                        OpenSourceTab.CONTRIBUTORS -> Icons.Outlined.Group
                                        OpenSourceTab.LIBRARIES -> Icons.Outlined.Code
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith 
                        fadeOut(animationSpec = tween(300))
                    },
                    label = "OpenSourceContent"
                ) { tab ->
                    when (tab) {
                        OpenSourceTab.CONTRIBUTORS -> ContributorsGrid(
                            contributors = uiState.contributors,
                            hazeState = hazeState,
                            isGlassActive = isHazeEnabled && isCustomBg,
                            accentColor = finalPrimary,
                            onContributorClick = { contributor ->
                                openUrl(context, contributor.githubUrl)
                            }
                        )
                        OpenSourceTab.LIBRARIES -> LibrariesList(
                            libraries = uiState.libraries,
                            hazeState = hazeState,
                            isGlassActive = isHazeEnabled && isCustomBg,
                            accentColor = finalPrimary,
                            onLibraryClick = { library ->
                                openUrl(context, library.url)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContributorsGrid(
    contributors: List<OpenSourceContributor>,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    onContributorClick: (OpenSourceContributor) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(contributors) { contributor ->
            ContributorCard(
                contributor = contributor,
                hazeState = hazeState,
                isGlassActive = isGlassActive,
                accentColor = accentColor,
                onClick = { onContributorClick(contributor) }
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun ContributorCard(
    contributor: OpenSourceContributor,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(ExpressiveCornerRadius)
    
    val cardModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
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
        Modifier
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = if (!isGlassActive) 1.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.1f),
                border = BorderStroke(2.dp, accentColor.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = contributor.avatarUrl,
                    contentDescription = contributor.username,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Username
            Text(
                text = contributor.username,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Contribution
            Text(
                text = contributor.contribution,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Repository Badge
            if (contributor.repository != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = contributor.repository,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }
            
            // [BARU] License Badge dengan Border
            if (contributor.license.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tentukan warna berdasarkan jenis license
                val (licenseColor, bgAlpha) = when {
                    contributor.license.contains("Apache") -> Pair(Color(0xFF4CAF50), 0.15f) // Hijau
                    contributor.license.contains("GPL") -> Pair(Color(0xFF9C27B0), 0.15f) // Ungu
                    else -> Pair(accentColor, 0.15f)
                }
                
                Surface(
                    color = licenseColor.copy(alpha = bgAlpha),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = licenseColor.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = contributor.license,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = licenseColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LibrariesList(
    libraries: List<OpenSourceLibrary>,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    onLibraryClick: (OpenSourceLibrary) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(libraries) { library ->
            LibraryCard(
                library = library,
                hazeState = hazeState,
                isGlassActive = isGlassActive,
                accentColor = accentColor,
                onClick = { onLibraryClick(library) }
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun LibraryCard(
    library: OpenSourceLibrary,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(ExpressiveCornerRadius)
    
    val cardModifier = if (isGlassActive) {
        Modifier
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.05f,
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
        Modifier
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier)
            .clickable(onClick = onClick),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = if (!isGlassActive) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemedIconContainer(
                icon = Icons.Filled.Code,
                primaryColor = accentColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Library License Badge
                    val libLicenseColor = when {
                        library.license.contains("Apache") -> Color(0xFF4CAF50)
                        library.license.contains("GPL") -> Color(0xFF9C27B0)
                        else -> accentColor
                    }
                    
                    Surface(
                        color = libLicenseColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, libLicenseColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = library.license,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = libLicenseColor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "by ${library.author}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = library.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
