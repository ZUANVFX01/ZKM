/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class
)

package com.zuan.kernelmanager.ui.proces

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureInPictureAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.theme.ThemeMode
import com.zuan.kernelmanager.utils.ProcessData
import com.zuan.kernelmanager.utils.ProcessUtils
import com.zuan.kernelmanager.utils.SortType
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// --- VIEWMODEL ---
class ProcessManagerViewModel : ViewModel() {
    var processList by mutableStateOf<List<ProcessData>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var limitOption by mutableStateOf(20)
        private set
    var sortType by mutableStateOf(SortType.CPU)
        private set
    
    var userProcessCount by mutableStateOf(0)
        private set
    var systemProcessCount by mutableStateOf(0)
        private set

    fun setLimit(newLimit: Int) { 
        limitOption = newLimit
        refreshData() 
    }
    
    fun setSort(newSort: SortType) { 
        sortType = newSort
        refreshData() 
    }
    
    fun startMonitoring(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) { 
                updateData(context)
                delay(3000) 
            }
        }
    }
    
    fun refreshData() { 
        isLoading = true 
    }
    
    private fun updateData(context: android.content.Context) {
        val data = ProcessUtils.getTopProcesses(context, limitOption, sortType)
        
        var u = 0
        var s = 0
        val pm = context.packageManager
        
        data.forEach { p ->
            try {
                if (!p.packageName.contains(".")) {
                    s++
                } else {
                    val ai = pm.getApplicationInfo(p.packageName, 0)
                    if ((ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0) s++ else u++
                }
            } catch (e: Exception) { 
                s++ 
            }
        }

        viewModelScope.launch(Dispatchers.Main) { 
            processList = data
            userProcessCount = u
            systemProcessCount = s
            isLoading = false 
        }
    }
}

@Composable
fun ProcessManagerScreen(
    navController: NavController,
    hazeState: HazeState,
    viewModel: ProcessManagerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val isCollapsed by remember { derivedStateOf { scrollBehavior.state.collapsedFraction > 0.6f } }
    
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    var selectedProcess by remember { mutableStateOf<ProcessData?>(null) }
    var showManageDialog by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    // --- THEME STATES (Fixed pakai collectAsState bukan collectAsStateWithLifecycle) ---
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val bgType by settingsViewModel.bgType.collectAsState()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsState()
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    
    val cardDarkness by settingsViewModel.cardDarkness.collectAsState()
    val isDynamic by settingsViewModel.isDynamicColor.collectAsState()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsState()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsState()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsState()

    val isSystemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM_DEFAULT -> isSystemDark
    }

    val effectivePrimary = remember(isDynamic, themeColorName, isCustomColor, customPrimary) {
        when {
            isCustomColor -> Color(customPrimary)
            isDynamic -> Color.Unspecified
            else -> themeColorName.primary // Fixed: langsung akses primary dari themeColorName
        }
    }

    val finalPrimary = if (effectivePrimary == Color.Unspecified) {
        MaterialTheme.colorScheme.primary
    } else {
        effectivePrimary
    }

    val (tintedBackground, cardContainerColor) = generateThemedBackgroundColor(finalPrimary, useDarkTheme)

    val actualCardContainerColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> MaterialTheme.colorScheme.surface.copy(alpha = cardDarkness)
        else -> cardContainerColor
    }

    val ExpressiveCornerRadius = 32.dp
    
    val baseGlassModifier = if (isGlassActive) {
        Modifier
            .clip(RoundedCornerShape(ExpressiveCornerRadius))
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
                shape = RoundedCornerShape(ExpressiveCornerRadius)
            )
    } else {
        Modifier
    }

    val appBarHazeStyle = HazeMaterials.regular()

    fun toggleFloatingMode() {
        if (Settings.canDrawOverlays(context)) {
            val intent = Intent(context, FloatingProcessService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            showOverlayPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) { viewModel.startMonitoring(context) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { _ -> 
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isCustomBg) Color.Transparent else tintedBackground)
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 0f)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState, zIndex = 1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { 
                    Spacer(modifier = Modifier.height(headerHeightDp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding())) 
                }
                
                item {
                    SystemHealthCard(
                        processList = viewModel.processList, 
                        cardBg = actualCardContainerColor,
                        isGlassActive = isGlassActive,
                        glassModifier = baseGlassModifier
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1.4f)) {
                            ResourceUsageChart(
                                processList = viewModel.processList,
                                sortType = viewModel.sortType,
                                cardBg = actualCardContainerColor,
                                isGlassActive = isGlassActive,
                                glassModifier = baseGlassModifier
                            )
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            ProcessDistributionChart(
                                userCount = viewModel.userProcessCount,
                                systemCount = viewModel.systemProcessCount,
                                cardBg = actualCardContainerColor,
                                isGlassActive = isGlassActive,
                                glassModifier = baseGlassModifier
                            )
                        }
                    }
                }

                item {
                    SortAndFilterRow(
                        currentSort = viewModel.sortType,
                        currentLimit = viewModel.limitOption,
                        onSortChange = { viewModel.setSort(it) },
                        onLimitChange = { viewModel.setLimit(it) }
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.running_processes),
                        style = MaterialTheme.typography.titleSmall,
                        color = finalPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                if (viewModel.processList.isEmpty() && viewModel.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp), 
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = finalPrimary)
                        }
                    }
                } else {
                    items(viewModel.processList, key = { it.pid }) { process ->
                        ExpressiveProcessItem(
                            process = process,
                            sortType = viewModel.sortType,
                            onClick = { selectedProcess = process }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp, end = 24.dp),
                            color = if (isCustomBg) Color.White.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            LargeTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = isCollapsed,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + slideInVertically { it / 2 })
                                .togetherWith(fadeOut(animationSpec = tween(300)) + slideOutVertically { -it / 2 })
                        },
                        label = "AppBarTitle"
                    ) { collapsed ->
                        if (collapsed) {
                            Text(
                                stringResource(R.string.process_manager), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Column {
                                Text(
                                    stringResource(R.string.system), 
                                    style = MaterialTheme.typography.titleLarge, 
                                    color = if (isCustomBg) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    stringResource(R.string.processes), 
                                    style = MaterialTheme.typography.headlineMedium, 
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            stringResource(R.string.btn_cancel), 
                            tint = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { toggleFloatingMode() }) {
                        Icon(
                            imageVector = Icons.Outlined.PictureInPictureAlt,
                            contentDescription = stringResource(R.string.floating_mode),
                            tint = if (isCustomBg) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .onGloballyPositioned { headerHeightPx = it.size.height }
                    .hazeEffect(
                        state = hazeState,
                        style = appBarHazeStyle
                    )
            )
        }
    }

    if (selectedProcess != null) {
        ModernProcessDetailDialog(
            process = selectedProcess!!,
            onDismiss = { selectedProcess = null },
            onManageClick = { showManageDialog = true },
            onKillClick = { /* Kill Logic */ },
            onStopClick = { /* Stop Logic */ }
        )
    }

    if (showManageDialog && selectedProcess != null) {
        AppManageDialog(
            process = selectedProcess!!, 
            onDismiss = { showManageDialog = false }
        )
    }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            icon = { Icon(Icons.Outlined.PictureInPictureAlt, contentDescription = null) },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.overlay_permission_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayPermissionDialog = false
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }) { 
                    Text(stringResource(R.string.open_settings)) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) { 
                    Text(stringResource(R.string.btn_cancel)) 
                }
            }
        )
    }
}

private fun generateThemedBackgroundColor(primaryColor: Color, isDark: Boolean): Pair<Color, Color> {
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

// ==========================================
// CHARTS COMPONENTS
// ==========================================

@Composable
fun ResourceUsageChart(
    processList: List<ProcessData>, 
    sortType: SortType, 
    cardBg: Color,
    isGlassActive: Boolean = false,
    glassModifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)
    
    val baseModifier = Modifier.fillMaxWidth().height(180.dp)
    
    Surface(
        modifier = if (isGlassActive) baseModifier.then(glassModifier) else baseModifier,
        shape = cardShape,
        color = if (isGlassActive) Color.Transparent else cardBg,
        tonalElevation = if (isGlassActive) 0.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (sortType == SortType.CPU) stringResource(R.string.top_cpu_usage) else stringResource(R.string.top_ram_usage),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val topProcesses = processList.take(5)
            val maxValue = topProcesses.maxOfOrNull { 
                if (sortType == SortType.CPU) it.cpu.replace("%", "").toFloatOrNull() ?: 0f
                else it.res.replace("M", "").replace("K", "").toFloatOrNull() ?: 0f
            } ?: 1f

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topProcesses.forEach { process ->
                    val rawVal = if (sortType == SortType.CPU) {
                        process.cpu.replace("%", "").toFloatOrNull() ?: 0f
                    } else {
                        process.res.replace("M", "").replace("K", "").toFloatOrNull() ?: 0f
                    }
                    val progress = (rawVal / maxValue).coerceIn(0f, 1f)
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = spring(dampingRatio = 0.7f),
                        label = "bar"
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = process.appName,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(50.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (sortType == SortType.CPU) {
                                            MaterialTheme.colorScheme.primary 
                                        } else {
                                            MaterialTheme.colorScheme.tertiary
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessDistributionChart(
    userCount: Int, 
    systemCount: Int, 
    cardBg: Color,
    isGlassActive: Boolean = false,
    glassModifier: Modifier = Modifier
) {
    val total = userCount + systemCount
    val userRatio = if (total > 0) userCount.toFloat() / total else 0f
    
    val animatedUserRatio by animateFloatAsState(
        targetValue = userRatio,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "donut"
    )

    val cardShape = RoundedCornerShape(32.dp)
    val baseModifier = Modifier.fillMaxWidth().height(180.dp)
    
    Surface(
        modifier = if (isGlassActive) baseModifier.then(glassModifier) else baseModifier,
        shape = cardShape,
        color = if (isGlassActive) Color.Transparent else cardBg,
        tonalElevation = if (isGlassActive) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                val userColor = MaterialTheme.colorScheme.secondary
                val systemColor = MaterialTheme.colorScheme.surfaceVariant
                
                Canvas(modifier = Modifier.size(80.dp)) {
                    val strokeWidth = 12.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    
                    drawCircle(
                        color = systemColor,
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    
                    drawArc(
                        color = userColor,
                        startAngle = -90f,
                        sweepAngle = animatedUserRatio * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                        size = Size(radius * 2, radius * 2)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = total.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(R.string.total), fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendDot(stringResource(R.string.user), userCount, MaterialTheme.colorScheme.secondary)
                LegendDot(stringResource(R.string.sys), systemCount, MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
fun LegendDot(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(text = label, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun SystemHealthCard(
    processList: List<ProcessData>, 
    cardBg: Color,
    isGlassActive: Boolean = false,
    glassModifier: Modifier = Modifier
) {
    val topCpu = processList.maxByOrNull { 
        it.cpu.replace("%","").toFloatOrNull() ?: 0f 
    }
    val totalProcs = processList.size

    val cardShape = RoundedCornerShape(32.dp)
    
    val finalModifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .then(glassModifier)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    }

    Surface(
        modifier = finalModifier,
        shape = cardShape,
        color = if (isGlassActive) Color.Transparent else cardBg,
        tonalElevation = if (isGlassActive) 0.dp else 1.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    stringResource(R.string.system_health), 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.monitoring_threads, totalProcs), 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (topCpu != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.top_load, topCpu.appName, topCpu.cpu), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.primary, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SortAndFilterRow(
    currentSort: SortType,
    currentLimit: Int,
    onSortChange: (SortType) -> Unit,
    onLimitChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentSort == SortType.CPU,
            onClick = { onSortChange(SortType.CPU) },
            label = { Text(stringResource(R.string.cpu_usage)) },
            leadingIcon = { 
                if (currentSort == SortType.CPU) {
                    Icon(Icons.Default.Speed, null, modifier = Modifier.size(16.dp)) 
                }
            },
            shape = RoundedCornerShape(50)
        )
        FilterChip(
            selected = currentSort == SortType.RES,
            onClick = { onSortChange(SortType.RES) },
            label = { Text(stringResource(R.string.memory)) },
            leadingIcon = { 
                if (currentSort == SortType.RES) {
                    Icon(Icons.Default.Memory, null, modifier = Modifier.size(16.dp)) 
                }
            },
            shape = RoundedCornerShape(50)
        )
        FilterChip(
            selected = false,
            onClick = { 
                val nextLimit = if (currentLimit == 10) 20 else if (currentLimit == 20) 50 else 10
                onLimitChange(nextLimit) 
            },
            label = { Text(stringResource(R.string.limit_format, currentLimit)) },
            leadingIcon = { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp)) },
            shape = RoundedCornerShape(50),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }
}

@Composable
fun ExpressiveProcessItem(
    process: ProcessData, 
    sortType: SortType, 
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 8.dp),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = { 
            Text(
                process.appName, 
                style = MaterialTheme.typography.bodyLarge, 
                fontWeight = FontWeight.SemiBold, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            ) 
        },
        supportingContent = { 
            Text(
                process.packageName, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            ) 
        },
        leadingContent = {
            Surface(
                shape = RoundedCornerShape(16.dp), 
                color = MaterialTheme.colorScheme.surfaceContainerHighest, 
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { 
                    AppIcon(drawable = process.icon, modifier = Modifier.size(32.dp)) 
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                val primaryValue = if (sortType == SortType.CPU) process.cpu else process.res
                val primaryLabel = if (sortType == SortType.CPU) stringResource(R.string.cpu) else stringResource(R.string.ram)
                Text(
                    text = primaryValue, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$primaryLabel • PID ${process.pid}", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}

@Composable
fun ModernProcessDetailDialog(
    process: ProcessData, 
    onDismiss: () -> Unit, 
    onManageClick: () -> Unit, 
    onKillClick: () -> Unit, 
    onStopClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface), 
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(drawable = process.icon, modifier = Modifier.size(48.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    process.appName, 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.Bold, 
                    textAlign = TextAlign.Center
                )
                Text(
                    process.packageName, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.primary, 
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoBadge(stringResource(R.string.cpu), process.cpu)
                    InfoBadge(stringResource(R.string.ram), process.res)
                    InfoBadge(stringResource(R.string.pid), process.pid)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = onManageClick, 
                        modifier = Modifier.weight(1f), 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Settings, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.manage))
                    }
                    Button(
                        onClick = onKillClick, 
                        modifier = Modifier.weight(1f), 
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), 
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.kill))
                    }
                }
                TextButton(
                    onClick = onStopClick, 
                    modifier = Modifier.fillMaxWidth()
                ) { 
                    Text(stringResource(R.string.force_stop), color = MaterialTheme.colorScheme.outline) 
                }
            }
        }
    }
}

@Composable
fun InfoBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AppIcon(drawable: android.graphics.drawable.Drawable?, modifier: Modifier = Modifier) {
    if (drawable != null) {
        val bitmap = remember(drawable) { drawable.toBitmap().asImageBitmap() }
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    } else {
        Icon(
            Icons.Default.FilterList, 
            null, 
            modifier = modifier, 
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AppManageDialog(process: ProcessData, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var verName by remember { mutableStateOf("") }
    var verCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(process.packageName) {
        try {
            val pInfo = context.packageManager.getPackageInfo(process.packageName, 0)
            verName = pInfo.versionName ?: context.getString(R.string.unknown)
            verCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toString()
            } else {
                pInfo.versionCode.toString()
            }
            isLoading = false
        } catch (e: Exception) { 
            verName = context.getString(R.string.not_found)
            verCode = "-"
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    stringResource(R.string.app_details), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow(stringResource(R.string.version), verName)
                        DetailRow(stringResource(R.string.build), verCode)
                        DetailRow(stringResource(R.string.package_name), process.packageName)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                ) { 
                    Text(stringResource(R.string.btn_close)) 
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value, 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Medium, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}
