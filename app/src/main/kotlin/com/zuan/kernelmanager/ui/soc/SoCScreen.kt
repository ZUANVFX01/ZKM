/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.soc

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.components.PinnedTopAppBar
import com.zuan.kernelmanager.ui.navigation.BottomNavigationBar
import com.zuan.kernelmanager.utils.SoCUtils
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.min
import kotlin.random.Random

// Custom Colors
private val DashboardDarkBg = Color(0xFF1E1E1E)
private val DashboardLightBg = Color(0xFFF5F5F5)
private val DashboardMintBg = Color(0xFFA7F3B8)
private val DashboardPurpleBg = Color(0xFFBFA8F7)
private val DashboardAccentGreen = Color(0xFF3DDB85)
private val DashboardTextDark = Color(0xFF1E1E1E)
private val DashboardTextLight = Color(0xFFF5F5F5)
private val DashboardCardShape = RoundedCornerShape(28.dp)

@Composable
fun SoCScreen(viewModel: SoCViewModel = viewModel(), navController: NavController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    val row1Height = screenHeight * 0.22f
    val row2Height = screenHeight * 0.38f
    val row3Height = screenHeight * 0.22f

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startJob()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopJob()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            PinnedTopAppBar(
                scrollBehavior = scrollBehavior,
                hazeState = hazeState
            )
        },
        bottomBar = { BottomNavigationBar(navController, hazeState = hazeState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Row 1: CPU & GPU Summary
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().height(row1Height)
                ) {
                    CPUSummaryCard(viewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                    GPUSummaryCard(viewModel, hazeState, navController, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }

            // Row 2: Clusters
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max) 
                ) {
                    ClusterStatusVerticalCard(
                        viewModel,
                        hazeState,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        ThermalVisualCard(
                            viewModel,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        BoostStatusCard(
                            viewModel,
                            hazeState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }

            // Row 3: Gauge
            item {
                PerformanceGaugeCard(viewModel, modifier = Modifier.fillMaxWidth().height(row3Height))
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// region === Dashboard Cards ===

@Composable
fun CPUSummaryCard(viewModel: SoCViewModel, modifier: Modifier = Modifier) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val cpuTemp by viewModel.cpuTemp.collectAsState()

    DashboardCard(
        backgroundColor = DashboardDarkBg,
        contentColor = DashboardTextLight,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "CPU Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    painter = painterResource(R.drawable.ic_cpu),
                    contentDescription = null,
                    tint = DashboardTextLight.copy(alpha = 0.7f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (cpuUsage == "N/A") "N/A" else cpuUsage,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heat),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = DashboardAccentGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (cpuTemp == "N/A") "N/A" else "$cpuTemp°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DashboardAccentGreen
                    )
                }
            }
        }
    }
}

@Composable
fun GPUSummaryCard(
    viewModel: SoCViewModel, 
    hazeState: HazeState, 
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val gpuTemp by viewModel.gpuTemp.collectAsState()
    val gpuState by viewModel.gpuState.collectAsState()
    
    DashboardCard(
        backgroundColor = DashboardLightBg,
        contentColor = DashboardTextDark,
        modifier = modifier.clickable { 
            if (gpuState.type == SoCUtils.GpuType.ADRENO) {
                navController.navigate(NavigationRoute.Adreno.route)
            } else if (gpuState.type == SoCUtils.GpuType.MEDIATEK_V2) {
                navController.navigate(NavigationRoute.Mtk.route)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "GPU",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                val displayTag = if(gpuState.type == SoCUtils.GpuType.MEDIATEK_V2 && gpuState.mtkFixedIndex == "-1") "Dynamic" 
                                 else "${gpuState.currentFreq} MHz"
                                 
                StatusChip(text = displayTag, color = DashboardTextDark)
            }

             Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (gpuUsage == "N/A") "N/A" else gpuUsage,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_heat),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (gpuTemp == "N/A") "N/A" else "$gpuTemp°C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
            ) {
                 val usageFloat = gpuUsage.replace("%", "").toFloatOrNull() ?: 0f
                 val animatedUsage by animateFloatAsState(targetValue = usageFloat / 100f, label = "gpu bar")
                 Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedUsage)
                        .background(DashboardAccentGreen)
                )
            }
        }
    }
}

@Composable
fun ClusterStatusVerticalCard(viewModel: SoCViewModel, hazeState: HazeState, modifier: Modifier) {
    val hasBigCluster by viewModel.hasBigCluster.collectAsState()
    val hasPrimeCluster by viewModel.hasPrimeCluster.collectAsState()
    val cpu0State by viewModel.cpu0State.collectAsState()
    val bigClusterState by viewModel.bigClusterState.collectAsState()
    val primeClusterState by viewModel.primeClusterState.collectAsState()

    DashboardCard(backgroundColor = DashboardLightBg, contentColor = DashboardTextDark, modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                .fillMaxSize()
        ) {
            Text(
                "Clusters", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Medium, 
                modifier = Modifier.padding(bottom = 8.dp) 
            )
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .padding(start = 7.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray.copy(alpha = 0.5f))
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween, 
                    modifier = Modifier.fillMaxSize()
                ) {
                    ClusterItem("Little Cluster", cpu0State, R.drawable.ic_speed, false, viewModel, "little", hazeState)
                    
                    if (hasBigCluster) {
                        ClusterItem("Big Cluster", bigClusterState, R.drawable.ic_speed, false, viewModel, "big", hazeState)
                    }
                    
                    if (hasPrimeCluster) {
                        ClusterItem("Prime Cluster", primeClusterState, R.drawable.ic_rocket_launch, true, viewModel, "prime", hazeState)
                    }
                }
            }
        }
    }
}


@Composable
fun ThermalVisualCard(viewModel: SoCViewModel, modifier: Modifier) {
    val cpuTemp by viewModel.cpuTemp.collectAsState()
    val gpuTemp by viewModel.gpuTemp.collectAsState()
    val maxTempStr = listOf(cpuTemp, gpuTemp).mapNotNull { it.replace("°C", "").toIntOrNull() }.maxOrNull()?.toString() ?: "N/A"
    val tempValue = maxTempStr.toIntOrNull() ?: 40
    val normalizedTemp = ((tempValue - 30f) / (80f - 30f)).coerceIn(0f, 1f)
    val animatedTempProgress by animateFloatAsState(targetValue = normalizedTemp, animationSpec = tween(1000), label = "tempVisual")

    DashboardCard(backgroundColor = DashboardPurpleBg, contentColor = DashboardTextDark, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.3f)) {
                val path = Path(); val width = size.width; val height = size.height 
                path.moveTo(0f, height)
                var previousX = 0f; var previousY = height * (1f - (animatedTempProgress * 0.5f))
                for (i in 1..5) {
                    val x = (width / 5) * i; val variance = (1f - animatedTempProgress) * height * 0.2f
                    val y = height * (1f - animatedTempProgress) + (Random.nextFloat() - 0.5f) * variance
                    path.cubicTo(previousX + (x - previousX)/2f, previousY, previousX + (x - previousX)/2f, y, x, y)
                    previousX = x; previousY = y
                }
                path.lineTo(width, height); path.close()
                drawPath(path = path, color = Color.White.copy(alpha = 0.5f))
                drawPath(path = path, color = Color.White, style = Stroke(width = 2.dp.toPx()))
            }
            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Max Temp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Icon(painter = painterResource(R.drawable.ic_heat), contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Text(text = if (maxTempStr == "N/A") "N/A" else "$maxTempStr°C", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
            }
        }
    }
}

@Composable
fun BoostStatusCard(viewModel: SoCViewModel, hazeState: HazeState, modifier: Modifier) {
    // Feature Flag (File exists?)
    val hasAdrenoBoost by viewModel.hasAdrenoBoost.collectAsState()
    val gpuState by viewModel.gpuState.collectAsState()
    
    val hasCpuSchedBoost by viewModel.hasCpuSchedBoostOnInput.collectAsState()
    val cpuSchedBoost by viewModel.cpuSchedBoostOnInput.collectAsState()
    
    // PERBAIKAN: Gunakan isAdrenoBoostActive, BUKAN adrenoBoost
    val isAdrenoBoostOn = hasAdrenoBoost && gpuState.isAdrenoBoostActive && gpuState.type == SoCUtils.GpuType.ADRENO
    val isCpuBoostOn = hasCpuSchedBoost && cpuSchedBoost == "1"
    
    val activeBoostCount = (if (isAdrenoBoostOn) 1 else 0) + (if (isCpuBoostOn) 1 else 0)
    val openBoostSettings = rememberDialogState(initiallyVisible = false)
    BoostConfigDialog(viewModel, hazeState, openBoostSettings)

    DashboardCard(backgroundColor = DashboardMintBg, contentColor = DashboardTextDark, modifier = modifier.clickable { openBoostSettings.visible = true }) {
         Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.2f)) {
            }
            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Active Boosts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Icon(painter = painterResource(R.drawable.ic_rocket_launch), contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                     Text(text = "$activeBoostCount Active", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(isCpuBoostOn) DashboardAccentGreen else Color.Gray.copy(0.3f)))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(isAdrenoBoostOn) DashboardAccentGreen else Color.Gray.copy(0.3f)))
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceGaugeCard(viewModel: SoCViewModel, modifier: Modifier) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val cpuVal = cpuUsage.replace("%", "").toFloatOrNull() ?: 0f
    val gpuVal = gpuUsage.replace("%", "").toFloatOrNull() ?: 0f
    val avgUsage = (cpuVal + gpuVal) / 2f
    val animatedUsage by animateFloatAsState(targetValue = avgUsage / 100f, animationSpec = tween(1000, easing = LinearEasing), label = "gauge")

    DashboardCard(backgroundColor = DashboardMintBg, contentColor = DashboardTextDark, modifier = modifier.height(180.dp)) {
        Row(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight().weight(0.45f)) {
                Text("System Load", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${avgUsage.toInt()}%", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontSize = 42.sp))
                Text("Avg. CPU + GPU", style = MaterialTheme.typography.labelLarge, color = DashboardTextDark.copy(alpha = 0.6f))
            }
            Box(modifier = Modifier.weight(0.55f).fillMaxHeight().aspectRatio(1f).drawBehind {
                val strokeWidth = 24.dp.toPx(); val radius = size.minDimension / 2 - strokeWidth
                val center = Offset(size.width / 2, size.height / 2); val startAngle = 150f; val sweepAngle = 240f
                drawArc(color = Color.Black.copy(alpha = 0.1f), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawArc(color = DashboardTextDark, startAngle = startAngle, sweepAngle = sweepAngle * animatedUsage, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }, contentAlignment = Alignment.Center) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CPU: ${(cpuVal).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DashboardTextDark.copy(alpha = 0.7f))
                    Text("GPU: ${(gpuVal).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DashboardTextDark.copy(alpha = 0.7f))
                 }
            }
        }
    }
}
// endregion

// region === Components & Config Dialogs ===
@Composable
fun ClusterItem(title: String, state: SoCViewModel.CPUState, icon: Int, isActive: Boolean, viewModel: SoCViewModel, clusterName: String, hazeState: HazeState) {
    val openConfigDialog = rememberDialogState(initiallyVisible = false)
    ClusterConfigDialog(title, state, viewModel, clusterName, hazeState, openConfigDialog)
    
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { openConfigDialog.visible = true }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
             Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isActive) DashboardAccentGreen else Color.LightGray))
            if (isActive) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(painter = painterResource(icon), contentDescription = null, tint = if (isActive) DashboardTextDark else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isActive) DashboardTextDark else Color.Gray)
            Text("${state.currentFreq} MHz · ${state.gov}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}


@Composable
fun ClusterConfigDialog(title: String, state: SoCViewModel.CPUState, viewModel: SoCViewModel, clusterName: String, hazeState: HazeState, dialogState: com.composables.core.DialogState) {
    val openMinFreq = rememberDialogState(false); val openMaxFreq = rememberDialogState(false); val openGov = rememberDialogState(false)
    DialogUnstyled(state = dialogState, hazeState = hazeState, title = "$title Settings", text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DialogTextButton(text = "Min Freq: ${state.minFreq} MHz", onClick = { openMinFreq.visible = true })
            DialogTextButton(text = "Max Freq: ${state.maxFreq} MHz", onClick = { openMaxFreq.visible = true })
            DialogTextButton(text = "Governor: ${state.gov}", onClick = { openGov.visible = true })
        }
    }, dismissButton = { TextButton(onClick = { dialogState.visible = false }) { Text("Close") } })
    SelectionDialog(openMinFreq, hazeState, "Select Min Freq", state.availableFreq) { viewModel.updateFreq("min", it, clusterName) }
    SelectionDialog(openMaxFreq, hazeState, "Select Max Freq", state.availableFreq) { viewModel.updateFreq("max", it, clusterName) }
    SelectionDialog(openGov, hazeState, "Select Governor", state.availableGov) { viewModel.updateGov(it, clusterName) }
}

@Composable
fun RowSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.SpaceBetween, 
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            modifier = Modifier.scale(0.8f) 
        )
    }
}

@Composable
fun BoostConfigDialog(viewModel: SoCViewModel, hazeState: HazeState, dialogState: com.composables.core.DialogState) {
    val openInputBoost = rememberDialogState(false)
    val cpuInputBoostMs by viewModel.cpuInputBoostMs.collectAsState()
    val cpuSchedBoost by viewModel.cpuSchedBoostOnInput.collectAsState()

    DialogUnstyled(
        state = dialogState,
        hazeState = hazeState,
        title = "Boost Configuration",
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogTextButton(text = "Input Boost: ${cpuInputBoostMs}ms", onClick = { openInputBoost.visible = true })
                RowSwitch("Sched Boost on Input", cpuSchedBoost == "1") { viewModel.updateCpuSchedBoostOnInput(it) }
            }
        },
        dismissButton = { TextButton(onClick = { dialogState.visible = false }) { Text("Close") } }
    )

    var value by remember { mutableStateOf(cpuInputBoostMs) }
    DialogUnstyled(
        state = openInputBoost,
        hazeState = hazeState,
        title = "Set Input Boost (ms)",
        text = {
            OutlinedTextField(
                value = value, onValueChange = { value = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.updateCpuInputBoostMs(value); openInputBoost.visible = false })
            )
        },
        confirmButton = { TextButton(onClick = { viewModel.updateCpuInputBoostMs(value); openInputBoost.visible = false }) { Text("Set") } },
        dismissButton = { TextButton(onClick = { openInputBoost.visible = false }) { Text("Cancel") } }
    )
}

@Composable
fun SelectionDialog(state: com.composables.core.DialogState, hazeState: HazeState, title: String, items: List<String>, onSelect: (String) -> Unit) {
    DialogUnstyled(state = state, hazeState = hazeState, title = title, text = {
        LazyColumn { items(items) { item -> DialogTextButton(text = item, onClick = { onSelect(item); state.visible = false }) } }
    }, dismissButton = { TextButton(onClick = { state.visible = false }) { Text("Cancel") } })
}

@Composable
fun DashboardCard(backgroundColor: Color, contentColor: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier, shape = DashboardCardShape, colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) { content() }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}
// endregion
