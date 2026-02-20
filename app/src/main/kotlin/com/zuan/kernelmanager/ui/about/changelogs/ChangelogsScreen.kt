/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class, ExperimentalLayoutApi::class)

package com.zuan.kernelmanager.ui.about.changelogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.about.ExpressiveCornerRadius
import com.zuan.kernelmanager.ui.about.generateThemedBackgroundColor
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun ChangelogsScreen(
    navController: NavController,
    hazeState: HazeState,
    viewModel: ChangelogsViewModel = viewModel(),
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
    val filteredChangelogs = viewModel.getFilteredChangelogs()
    
    // Warna khas Telegram
    val telegramBlue = Color(0xFF0088cc)
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Changelogs",
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
                actions = {
                    IconButton(onClick = { 
                        if (uiState.expandedVersions.size == uiState.changelogs.size) {
                            viewModel.collapseAll()
                        } else {
                            viewModel.expandAll()
                        }
                    }) {
                        Icon(
                            imageVector = if (uiState.expandedVersions.size == uiState.changelogs.size) {
                                Icons.Outlined.UnfoldLess
                            } else {
                                Icons.Outlined.UnfoldMore
                            },
                            contentDescription = "Toggle Expand",
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
                // Latest Version Card dengan Toggle Source
                uiState.remoteRelease?.let { release ->
                    LatestVersionCard(
                        release = release,
                        hazeState = hazeState,
                        isGlassActive = isHazeEnabled && isCustomBg,
                        accentColor = finalPrimary,
                        telegramBlue = telegramBlue,
                        isChecking = uiState.isCheckingUpdate,
                        sourceMode = uiState.sourceMode,
                        onDownloadClick = {
                            openUrl(context, release.downloadUrl)
                        },
                        onRefreshClick = {
                            viewModel.checkForUpdates()
                        },
                        onSourceToggle = {
                            viewModel.toggleSourceMode()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Check update button jika belum ada data
                if (uiState.remoteRelease == null && !uiState.isCheckingUpdate) {
                    UpdateCheckCard(
                        onCheckClick = { viewModel.checkForUpdates() },
                        hazeState = hazeState,
                        isGlassActive = isHazeEnabled && isCustomBg,
                        accentColor = finalPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Loading indicator
                if (uiState.isCheckingUpdate) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = finalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                FilterChipsRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) },
                    accentColor = finalPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredChangelogs) { entry ->
                        ChangelogCard(
                            entry = entry,
                            isExpanded = uiState.expandedVersions.contains(entry.version),
                            hazeState = hazeState,
                            isGlassActive = isHazeEnabled && isCustomBg,
                            accentColor = finalPrimary,
                            onToggleExpand = { viewModel.toggleVersionExpansion(entry.version) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun LatestVersionCard(
    release: RemoteReleaseInfo,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    telegramBlue: Color,
    isChecking: Boolean,
    sourceMode: UpdateSourceMode,
    onDownloadClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onSourceToggle: () -> Unit
) {
    val shape = RoundedCornerShape(ExpressiveCornerRadius)
    val context = LocalContext.current
    
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
    
    val gradientBrush = Brush.linearGradient(
        colors = if (release.isNewer) {
            listOf(
                accentColor.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        }
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surface,
        tonalElevation = if (!isGlassActive) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (!isGlassActive) gradientBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                .padding(20.dp)
        ) {
            // Header Row dengan Icon, Info, dan Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Icon
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Nama App dan Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Zuan Kernel Manager",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        val tagText = if (release.isNewer) "NEW" else "LATEST"
                        val tagColor = if (release.isNewer) accentColor else MaterialTheme.colorScheme.tertiary
                        val tagTextColor = if (release.isNewer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary
                        
                        Surface(
                            color = tagColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = tagText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = tagTextColor
                            )
                        }
                    }
                    
                    // Versi
                    Text(
                        text = "v${release.version}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    )
                    
                    // Tanggal
                    Text(
                        text = "Released on ${release.publishedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Source Indicator (GitHub/Telegram)
                    val sourceColor = if (release.source == "GitHub") accentColor else telegramBlue
                    val sourceIcon = if (release.source == "GitHub") Icons.Outlined.Code else Icons.Outlined.Send
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = sourceIcon,
                            contentDescription = null,
                            tint = sourceColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "via ${release.source}",
                            style = MaterialTheme.typography.labelSmall,
                            color = sourceColor
                        )
                    }
                }
                
                // Action Buttons Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Refresh Button
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !isChecking,
                        modifier = Modifier.size(40.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = accentColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Toggle Source Button (GitHub <-> Telegram)
                    IconButton(
                        onClick = onSourceToggle,
                        enabled = !isChecking,
                        modifier = Modifier.size(40.dp)
                    ) {
                        val toggleIcon = if (sourceMode == UpdateSourceMode.GITHUB) 
                            Icons.Outlined.Send else Icons.Outlined.Code
                        val toggleColor = if (sourceMode == UpdateSourceMode.GITHUB) 
                            telegramBlue else accentColor
                        
                        Icon(
                            imageVector = toggleIcon,
                            contentDescription = "Toggle Source",
                            tint = toggleColor
                        )
                    }
                }
            }
            
            // Pesan Update / Latest
            if (release.isNewer) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NewReleases,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "New version available from ${release.source}! Update now to get the latest features.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDownloadClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (release.source == "GitHub") accentColor else telegramBlue
                    )
                ) {
                    Icon(
                        imageVector = if (release.source == "GitHub") Icons.Outlined.Download else Icons.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (release.source == "GitHub") "Download Update" else "Open Telegram",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You're on the latest version (${release.source})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun UpdateCheckCard(
    onCheckClick: () -> Unit,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color
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
            .clickable(onClick = onCheckClick),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Check for Updates",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipsRow(
    selectedFilter: ChangeFilter,
    onFilterSelected: (ChangeFilter) -> Unit,
    accentColor: Color
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChangeFilter.values().forEach { filter ->
            val selected = selectedFilter == filter
            FilterChip(
                selected = selected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when(filter) {
                            ChangeFilter.ALL -> "All"
                            ChangeFilter.NEW_FEATURES -> "Features"
                            ChangeFilter.FIXES -> "Fixes"
                            ChangeFilter.IMPROVEMENTS -> "Improvements"
                            ChangeFilter.BETA_ONLY -> "Beta"
                        },
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accentColor.copy(alpha = 0.2f),
                    selectedLabelColor = accentColor,
                    selectedLeadingIconColor = accentColor
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
private fun ChangelogCard(
    entry: ChangelogEntry,
    isExpanded: Boolean,
    hazeState: HazeState,
    isGlassActive: Boolean,
    accentColor: Color,
    onToggleExpand: () -> Unit
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
    
    val headerColor = when {
        entry.isImportant -> accentColor.copy(alpha = 0.15f)
        entry.isBeta -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier)
            .clickable(onClick = onToggleExpand),
        shape = shape,
        color = if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        tonalElevation = if (!isGlassActive) 2.dp else 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "v${entry.version}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (entry.isBeta) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ) {
                                Text("BETA", fontSize = 10.sp)
                            }
                        }
                        
                        if (entry.isImportant) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Important",
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = entry.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onToggleExpand) {
                    AnimatedContent(
                        targetState = isExpanded,
                        transitionSpec = { scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut() }
                    ) { expanded ->
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.changes.forEach { change ->
                        ChangeItemRow(change = change, accentColor = accentColor)
                    }
                }
            }
            
            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.changes.take(2).forEach { change ->
                        ChangeItemRow(change = change, accentColor = accentColor, compact = true)
                    }
                    if (entry.changes.size > 2) {
                        Text(
                            text = "+${entry.changes.size - 2} more changes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangeItemRow(
    change: ChangeItem,
    accentColor: Color,
    compact: Boolean = false
) {
    val typeColor = Color(android.graphics.Color.parseColor(ChangelogsUtils.getChangeTypeColor(change.type)))
    val typeLabel = ChangelogsUtils.getChangeTypeLabel(change.type)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = typeColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.padding(top = if (compact) 0.dp else 2.dp)
        ) {
            Text(
                text = typeLabel,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 9.sp else 10.sp
                ),
                color = typeColor
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = change.description,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = if (compact) 18.sp else 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
