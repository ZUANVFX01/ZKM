/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.socmenu

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 * Generate SOFT TINTED background berdasarkan primary color tema (SAMA PERSIS AboutScreen)
 */
@Composable
fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
    return if (isDark) {
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
        val primaryRed = primaryColor.red
        val primaryGreen = primaryColor.green
        val primaryBlue = primaryColor.blue
        
        val softBackground = Color(
            red = 0.97f + (primaryRed * 0.03f),
            green = 0.97f + (primaryGreen * 0.03f),
            blue = 0.97f + (primaryBlue * 0.03f),
            alpha = 1f
        )
        
        val cardColor = Color(0xFFFFFFFF)
        
        Pair(softBackground, cardColor)
    }
}

@Composable
fun CpuGpuScreen(
    navController: NavController,
    hazeState: HazeState, 
    viewModel: CpuGpuViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val socName by viewModel.socName.collectAsStateWithLifecycle()
    val clusters by viewModel.clusterStates.collectAsStateWithLifecycle()
    val gpuState by viewModel.gpuState.collectAsStateWithLifecycle()
    val cpusets by viewModel.cpusetList.collectAsStateWithLifecycle()
    
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val isCustomBg by settingsViewModel.isCustomBackground.collectAsState()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsState()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsState()
    
    val isDynamic by settingsViewModel.isDynamicColor.collectAsState()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsState()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsState()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsState()
    
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    val effectivePrimary = when {
        isCustomColor -> Color(customPrimary)
        isDynamic -> MaterialTheme.colorScheme.primary
        else -> themeColorName.primary
    }

    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(effectivePrimary, useDarkTheme)

    val backgroundColor = when {
        isCustomBg -> Color.Transparent 
        else -> tintedBackground
    }

    val solidCardColor = when {
        isCustomBg -> MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }
    
    val isGlassActive = isCustomBg && isHazeEnabled

    val contentColor = MaterialTheme.colorScheme.onSurface
    val subContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val iconContainerColor = if (isGlassActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    
    val iconTint = MaterialTheme.colorScheme.onSecondaryContainer

    var selectedCluster by remember { mutableStateOf<CpuGpuViewModel.CPUState?>(null) }
    val context = LocalContext.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = Velocity.Zero 
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .nestedScroll(nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { CpuSectionTitle(socName, subContentColor) }
        
        items(clusters) { cluster ->
            CleanInfoCard(
                icon = if (cluster.isPrime) Icons.Rounded.RocketLaunch else Icons.Rounded.Speed,
                title = cluster.name,
                value = "${cluster.currentFreq} MHz",
                subValue = "${cluster.gov} • ${cluster.minFreq}-${cluster.maxFreq} MHz",
                solidCardColor = solidCardColor,
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardDarkness = cardDarkness,
                titleColor = subContentColor,
                valueColor = contentColor,
                iconContainerColor = iconContainerColor,
                iconTint = iconTint,
                onClick = { selectedCluster = cluster }
            )
        }

        item { CpuSectionTitle("Graphics Unit", subContentColor) }
        item {
            CleanInfoCard(
                icon = Icons.Rounded.GraphicEq,
                title = "Graphics Processor",
                value = if (gpuState.currentFreq == "N/A") "N/A" else "${gpuState.currentFreq} MHz",
                subValue = "${gpuState.type.name} • ${gpuState.usage}% Load",
                solidCardColor = solidCardColor,
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardDarkness = cardDarkness,
                titleColor = subContentColor,
                valueColor = contentColor,
                iconContainerColor = iconContainerColor,
                iconTint = iconTint,
                onClick = {
                    when (gpuState.type) {
                        CpuGpuUtils.GpuType.ADRENO -> navController.navigate(NavigationRoute.Adreno.route)
                        CpuGpuUtils.GpuType.MEDIATEK_V2, 
                        CpuGpuUtils.GpuType.MEDIATEK_LEGACY -> navController.navigate(NavigationRoute.Mtk.route)
                        CpuGpuUtils.GpuType.GENERIC_DEVFREQ -> navController.navigate(NavigationRoute.GenericGpu.route)
                        else -> Toast.makeText(context, "GPU Interface not supported", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        if (cpusets.isNotEmpty()) {
            item { CpuSectionTitle("Processor Sets (Cpuset)", subContentColor) }
            items(cpusets) { cpuset ->
                CpusetCard(
                    cpuset = cpuset, 
                    viewModel = viewModel, 
                    solidCardColor = solidCardColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    cardDarkness = cardDarkness,
                    titleColor = contentColor,
                    subtitleColor = subContentColor,
                    iconContainerColor = iconContainerColor,
                    iconTint = iconTint
                )
            }
        }
    }

    if (selectedCluster != null) {
        val liveCluster = clusters.find { it.policyPath == selectedCluster?.policyPath } ?: selectedCluster
        if (liveCluster != null) {
            CpuBottomSheet(
                cluster = liveCluster,
                viewModel = viewModel,
                hazeState = hazeState,
                isGlassActive = isGlassActive || isCustomBg,
                containerColor = solidCardColor,
                useDarkTheme = useDarkTheme,
                onDismiss = { selectedCluster = null }
            )
        }
    }
}

@Composable
private fun CpuSectionTitle(text: String, color: Color) {
    Text(
        text = text, 
        style = MaterialTheme.typography.titleSmall, 
        color = color, 
        fontWeight = FontWeight.Bold, 
        modifier = Modifier.padding(start = 24.dp, top = 8.dp)
    )
}

@Composable
fun CleanInfoCard(
    icon: ImageVector, 
    title: String, 
    value: String, 
    subValue: String, 
    solidCardColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float,
    titleColor: Color,
    valueColor: Color,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    
    // FIX: Langsung pakai if di modifier, jangan assign ke variable dulu
    Box(
        modifier = if (isGlassActive) {
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(shape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness),
                        blurRadius = 30.dp,
                        noiseFactor = 0.08f,
                        tints = emptyList()
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                        )
                    ),
                    shape = shape
                )
                .clickable { onClick() }
        } else {
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(shape)
                .background(solidCardColor)
                .clickable { onClick() }
        }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor), 
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = valueColor)
            }
            Text(subValue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CpusetCard(
    cpuset: CpusetData, 
    viewModel: CpuGpuViewModel, 
    solidCardColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float,
    titleColor: Color,
    subtitleColor: Color,
    iconContainerColor: Color,
    iconTint: Color
) {
    var showDialog by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(24.dp)

    // FIX: Langsung pakai if di modifier
    Box(
        modifier = if (isGlassActive) {
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(shape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness),
                        blurRadius = 30.dp,
                        noiseFactor = 0.08f,
                        tints = emptyList()
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), 
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                        )
                    ),
                    shape = shape
                )
                .clickable { showDialog = true }
        } else {
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(shape)
                .background(solidCardColor)
                .clickable { showDialog = true }
        }
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Layers, null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cpuset.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = titleColor)
                Text("Cores: ${cpuset.value}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp), tint = subtitleColor)
        }
    }

    if (showDialog) {
        MultiSelectCpusetDialog(
            title = cpuset.name,
            currentValue = cpuset.value,
            onDismiss = { showDialog = false },
            onApply = { selected -> 
                viewModel.updateCpuset(cpuset.path, selected)
                showDialog = false
            }
        )
    }
}

@Composable
fun MultiSelectCpusetDialog(
    title: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onApply: (List<Int>) -> Unit
) {
    val maxCores = CpuGpuUtils.getMaxCoreCount()
    val initialSelection = remember { CpuGpuUtils.parseCpusetCores(currentValue) }
    val selectedCores = remember { mutableStateListOf<Int>().apply { addAll(initialSelection) } }
    
    val textColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { 
            Text(
                title, 
                fontWeight = FontWeight.Bold,
                color = textColor
            ) 
        },
        text = {
            Column {
                Text(
                    "Select active cores:", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = subTextColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(maxCores) { coreId ->
                        val isSelected = selectedCores.contains(coreId)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedCores.remove(coreId) else selectedCores.add(coreId)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isSelected, onCheckedChange = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "CPU $coreId", 
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { 
            Button(
                onClick = { onApply(selectedCores.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { 
                Text(
                    "Apply",
                    color = MaterialTheme.colorScheme.onPrimary
                ) 
            } 
        },
        dismissButton = { 
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) { 
                Text("Cancel") 
            } 
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuBottomSheet(
    cluster: CpuGpuViewModel.CPUState, 
    viewModel: CpuGpuViewModel, 
    containerColor: Color,
    isGlassActive: Boolean,
    useDarkTheme: Boolean,
    hazeState: HazeState,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }
    val govTunables by viewModel.govTunables.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(selectedTab) { 
        if (selectedTab == 1) viewModel.loadGovTunables(cluster.policyPath, cluster.gov) 
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss, 
        sheetState = sheetState, 
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = if (isGlassActive) Color.Transparent else containerColor, 
        dragHandle = null 
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) 
                .then(
                    if (isGlassActive) {
                        Modifier.hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    } else Modifier
                )
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.padding(top = 16.dp).width(48.dp).height(4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    ) {}
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = cluster.name, 
                    style = MaterialTheme.typography.headlineMedium, 
                    fontWeight = FontWeight.ExtraBold, 
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if(isGlassActive) 0.5f else 1f),
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        TabButton("Performance", selectedTab == 0, Modifier.weight(1f)) { 
                            selectedTab = 0 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        Spacer(Modifier.width(4.dp))
                        TabButton("Tuning", selectedTab == 1, Modifier.weight(1f)) { 
                            selectedTab = 1
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            item {
                                Text(
                                    "Core Control", 
                                    style = MaterialTheme.typography.titleSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha=0.6f)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        cluster.associatedCores.forEach { core ->
                                            CoreToggleItem(coreId = core.id, isOnline = core.isOnline, onToggle = { viewModel.toggleCore(core.id, core.isOnline) })
                                        }
                                    }
                                }
                            }
                            item {
                                PixelControlTile(
                                    label = "CPU Governor",
                                    value = cluster.gov,
                                    icon = Icons.Rounded.Speed,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if(isGlassActive) 0.6f else 1f),
                                    onClick = { }
                                ) {
                                    SelectionDialogButton(currentVal = cluster.gov, title = "Select Governor", options = cluster.availableGov, onSelected = { viewModel.updateGov(it, cluster.policyPath) })
                                }
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        PixelControlTile(label = "Min Freq", value = "${cluster.minFreq} MHz", icon = Icons.Rounded.ArrowDownward, color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if(isGlassActive) 0.6f else 1f), onClick = {}) {
                                            SelectionDialogButton(currentVal = cluster.minFreq, title = "Min Frequency", options = cluster.availableFreq, transformLabel = { "$it MHz" }, onSelected = { viewModel.updateFreq("min", it, cluster.policyPath) })
                                        }
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        PixelControlTile(label = "Max Freq", value = "${cluster.maxFreq} MHz", icon = Icons.Rounded.ArrowUpward, color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = if(isGlassActive) 0.6f else 1f), onClick = {}) {
                                            SelectionDialogButton(currentVal = cluster.maxFreq, title = "Max Frequency", options = cluster.availableFreq, transformLabel = { "$it MHz" }, onSelected = { viewModel.updateFreq("max", it, cluster.policyPath) })
                                        }
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(32.dp)) }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                            items(govTunables) { tunable ->
                                TunableItem(tunable = tunable, onSave = { newValue -> viewModel.applyTunable(tunable, newValue) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoreToggleItem(coreId: Int, isOnline: Boolean, onToggle: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF4CAF50) else Color.Red)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "CPU $coreId", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Switch(
            checked = isOnline,
            onCheckedChange = { 
                if (isOnline) {
                    showConfirm = true
                } else {
                    onToggle()
                }
            },
            enabled = coreId != 0 
        )
    }
    
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = { Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { 
                Text(
                    "Disable CPU $coreId?",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = { 
                Text(
                    "Disabling this core may reduce performance. Ensure other cores are active to handle system tasks.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onToggle()
                        showConfirm = false 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { 
                    Text(
                        "Disable",
                        color = MaterialTheme.colorScheme.onError
                    ) 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) { 
                    Text("Cancel") 
                }
            }
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
    val shadow = if (isSelected) 4.dp else 0.dp 

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = CircleShape,
        color = containerColor,
        shadowElevation = shadow
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PixelControlTile(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit 
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                }
                
                Column {
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = value.replace(" MHz", ""), 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun SelectionDialogButton(
    currentVal: String,
    title: String,
    options: List<String>,
    transformLabel: (String) -> String = { it },
    onSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp),
            title = { 
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                Box(
                    modifier = Modifier
                        .heightIn(max = 400.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface) 
                ) {
                    LazyColumn {
                        if (options.isEmpty()) {
                            item { 
                                Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        "No options", 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                }
                            }
                        } else {
                            items(options) { option ->
                                val label = transformLabel(option)
                                val isSelected = (label == currentVal || option == currentVal.replace(" MHz", ""))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelected(option)
                                            showDialog = false
                                        }
                                        .background(if(isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                        .padding(vertical = 16.dp, horizontal = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if(isSelected) {
                                        Spacer(Modifier.weight(1f))
                                        Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cancel", 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

@Composable
fun TunableItem(tunable: GovTunable, onSave: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(tunable.value) }
    
    // FIX: Extract color scheme di awal fungsi
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        onClick = { 
            editValue = tunable.value
            showDialog = true 
        },
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tunable.name, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold, 
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tunable.value, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Edit, null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = colorScheme.surfaceContainerHigh,
            title = { 
                Text(
                    text = tunable.name, 
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                ) 
            },
            text = {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    label = { 
                        Text(
                            "Value",
                            color = colorScheme.onSurfaceVariant
                        ) 
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surface,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(editValue)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) { 
                    Text(
                        "Apply",
                        color = colorScheme.onPrimary
                    ) 
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary)
                ) { 
                    Text("Cancel") 
                }
            }
        )
    }
}
