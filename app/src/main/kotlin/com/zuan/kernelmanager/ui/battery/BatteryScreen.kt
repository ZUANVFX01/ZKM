/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.battery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.zuan.kernelmanager.utils.BatteryUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.sin

// Warna Custom
private val DashboardGreen = Color(0xFF1B4D3E)

@Composable
fun BatteryScreen(viewModel: BatteryViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val chargingState by viewModel.chargingState.collectAsState()
    val rvkernels = listOf("RvKernel-Alioth-v1.2")
    val hasThermalSconfig by viewModel.hasThermalSconfig.collectAsState()
    
    // State for Smart Cutoff
    val smartCutoffEnabled by viewModel.smartCutoffEnabled.collectAsState()
    val cutoffLimit by viewModel.cutoffLimit.collectAsState()
    
    // State for Battery Monitor
    val monitorEnabled by viewModel.monitorEnabled.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.initializeBatteryInfo(context)
                    viewModel.startJob()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.unregisterBatteryListeners(context)
                    viewModel.stopJob()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PinnedTopAppBar(scrollBehavior = scrollBehavior, hazeState = hazeState)
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
            // ROW 1: Level + Voltage
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        BatteryLevelSquareCard(viewModel)
                    }
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        VoltageSquareCard(viewModel)
                    }
                }
            }

            // ROW 2: AMPERAGE
            item {
                AmperageMeterCard(viewModel)
            }

            // ROW 3: ANALYTICS
            item {
                CapacityAnalyticsCard(viewModel)
            }
            
            // ROW 4: DETAILS
            item {
                BatteryDetailCard(viewModel)
            }

            // ROW 5: SYSTEM
            item {
                SystemActivityCard(viewModel)
            }

            // ROW 6: HEALTH
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        HealthCircularCard(viewModel)
                    }
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            SmallInfoCard(
                                icon = Icons.Outlined.Memory,
                                title = "Tech",
                                value = viewModel.batteryInfo.collectAsState().value.tech,
                                color = Color(0xFF3498DB)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            SmallInfoCard(
                                icon = Icons.Outlined.DeviceThermostat,
                                title = "Temp",
                                value = "${viewModel.batteryInfo.collectAsState().value.temp}",
                                color = Color(0xFFE67E22)
                            )
                        }
                    }
                }
            }
            
            // SMART CUT-OFF
            item {
               Spacer(modifier = Modifier.height(8.dp)) 
               SmartCutoffCard(
                   isEnabled = smartCutoffEnabled, 
                   onEnabledChange = { enabled -> 
                       viewModel.toggleSmartCutoff(context, enabled) 
                   },
                   cutOffLevel = cutoffLimit,
                   onCutOffLevelChange = { value -> 
                       viewModel.setCutoffLimit(context, value) 
                   },
                   onManualCut = { viewModel.manualCutCharge() },
                   onManualResume = { 
                       viewModel.manualResumeCharge(context) 
                   }
               )
            }
            
            // BATTERY MONITOR (NEW)
            item {
                BatteryMonitorCard(
                    isEnabled = monitorEnabled,
                    onEnabledChange = { viewModel.toggleMonitor(context, it) }
                )
            }

            // CONTROLS HEADER
            item {
                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            // THERMAL PROFILE
            if (hasThermalSconfig) {
                item { ThermalProfileDashboardItem(viewModel, hazeState) }
            }

            // FAST CHARGING
            if (chargingState.hasFastCharging) {
                item {
                    ControlSwitchCard(
                        title = "Force Fast Charging",
                        subtitle = "Bypass limitations",
                        checked = chargingState.isFastChargingChecked,
                        onCheckedChange = { viewModel.updateCharging(BatteryUtils.FAST_CHARGING, it) },
                        icon = painterResource(R.drawable.ic_electric_bolt)
                    )
                }
            }

            // BYPASS CHARGING
            if (rvkernels.any { chargingState.kernelVersion.contains(it) }) {
                item {
                    ControlSwitchCard(
                        title = "Bypass Charging",
                        subtitle = "Power directly from AC",
                        checked = chargingState.isBypassChargingChecked,
                        onCheckedChange = { viewModel.updateCharging(BatteryUtils.BYPASS_CHARGING, it) },
                        icon = painterResource(R.drawable.ic_battery_android_frame_shield)
                    )
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun BatteryMonitorCard(
    isEnabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    // Launcher untuk meminta izin notifikasi (Wajib untuk Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onEnabledChange(true)
            } else {
                onEnabledChange(false)
            }
        }
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer), 
                    contentAlignment = Alignment.Center
                ) {
                    // Gunakan icon visibility atau monitoring
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility), 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Active Monitor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEnabled) "Tracking stats in background" else "Enable to track drain/deep sleep",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { check ->
                    if (check) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permissionStatus = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                                onEnabledChange(true)
                            } else {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            onEnabledChange(true)
                        }
                    } else {
                        onEnabledChange(false)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}

@Composable
fun SmartCutoffCard(
    isEnabled: Boolean = false,
    onEnabledChange: (Boolean) -> Unit = {},
    cutOffLevel: Float = 80f,
    onCutOffLevelChange: (Float) -> Unit = {},
    onManualCut: () -> Unit = {},
    onManualResume: () -> Unit = {}
) {
    var localEnabled by remember(isEnabled) { mutableStateOf(isEnabled) }
    var localSliderValue by remember(cutOffLevel) { mutableFloatStateOf(cutOffLevel) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_battery_android_frame_shield), 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Smart Cut-off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (localEnabled) "Monitoring active" else "Feature disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (localEnabled) DashboardGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = localEnabled,
                    onCheckedChange = { 
                        localEnabled = it
                        onEnabledChange(it) 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DashboardGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Limit Threshold",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${localSliderValue.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DashboardGreen
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = localSliderValue,
                    onValueChange = { 
                        localSliderValue = it
                        onCutOffLevelChange(it)
                    },
                    valueRange = 10f..100f,
                    steps = 8, 
                    colors = SliderDefaults.colors(
                        thumbColor = DashboardGreen,
                        activeTrackColor = DashboardGreen,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = localEnabled
                )
                
                Text(
                    text = "Power input will be suspended when battery level reaches ${localSliderValue.toInt()}%.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Manual Trigger",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onManualCut,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_electric_bolt),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cut Charge")
                }

                OutlinedButton(
                    onClick = onManualResume,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DashboardGreen),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DashboardGreen
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_bolt_circle), 
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resume")
                }
            }
        }
    }
}

@Composable
fun AmperageMeterCard(viewModel: BatteryViewModel) {
    val info by viewModel.batteryInfo.collectAsState()
    val isCharging = info.status.contains("Charging", ignoreCase = true) || info.status.contains("Full", ignoreCase = true)
    
    val meterColor = if (isCharging) Color(0xFF2ECC71) else Color(0xFFE74C3C)
    val currentAbs = kotlin.math.abs(info.currentNow)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_electric_bolt), contentDescription = null, tint = meterColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Amperage Meter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currentAbs",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = meterColor
                )
                Text(
                    text = " mA",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AmperageStatItem("Min", "${info.currentMin} mA")
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
                AmperageStatItem("Max", "${info.currentMax} mA")
            }
        }
    }
}

@Composable
fun AmperageStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun BatteryDetailCard(viewModel: BatteryViewModel) {
    val info by viewModel.batteryInfo.collectAsState()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Battery Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            DetailRow("Status", info.status, icon = Icons.Outlined.Bolt)
            DetailRow("Design Capacity", info.designCapacity, icon = painterResource(R.drawable.ic_battery_android_frame_full))
            DetailRow("Charging Cycles", info.cycleCount, icon = painterResource(R.drawable.ic_cycle))
            DetailRow("Charging Type", info.chargeType, icon = painterResource(R.drawable.ic_bolt_circle))
            
            val speedText = String.format("%.2f W", info.wattage)
            DetailRow("Charging Speed", speedText, icon = Icons.Default.Speed)

            DetailRow("Health Test", info.calculatedHealth, icon = painterResource(R.drawable.ic_medical_services))
        }
    }
}

@Composable
fun DetailRow(title: String, value: String, icon: Any) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            if (icon is ImageVector) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
                Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.SemiBold, 
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
fun CapacityAnalyticsCard(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    
    val levelRatio = (batteryInfo.level.removeSuffix("%").toFloatOrNull() ?: 0f) / 100f
    
    val currentCap = batteryInfo.maximumCapacity.filter { it.isDigit() }.toFloatOrNull() ?: 0f
    val designCap = batteryInfo.designCapacity.filter { it.isDigit() }.toFloatOrNull() 
        ?: batteryInfo.manualDesignCapacity.toFloat().takeIf { it > 0 } ?: 5000f
    val capRatio = if (designCap > 0) (currentCap / designCap).coerceIn(0f, 1f) else 0f
    
    val tempVal = batteryInfo.temp.replace(" °C", "").toFloatOrNull() ?: 30f
    val tempRatio = (tempVal / 60f).coerceIn(0.1f, 1f) 

    val barValues = listOf(levelRatio, capRatio, tempRatio)
    val barLabels = listOf("Level", "Health", "Temp")
    val barColors = listOf(DashboardGreen, Color(0xFF3498DB), Color(0xFFE67E22))

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().height(200.dp) 
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Realtime Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            
            Row(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp), 
                verticalAlignment = Alignment.Bottom 
            ) {
                barValues.forEachIndexed { index, ratio ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textValue = when(index) {
                            0 -> "${(ratio * 100).toInt()}%"
                            1 -> if(capRatio > 0) "${(ratio * 100).toInt()}%" else "N/A"
                            2 -> "${tempVal.toInt()}°C"
                            else -> ""
                        }
                        
                        Text(text = textValue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp) 
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh), 
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val animatedHeight by animateFloatAsState(targetValue = ratio, animationSpec = tween(1000))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f)) 
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(barColors[index])
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = barLabels[index], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemActivityCard(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val uptime by viewModel.uptime.collectAsState()

    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().height(140.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().align(Alignment.BottomCenter).height(80.dp).clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))) {
                val wavePath = Path()
                val width = size.width
                val height = size.height
                val midHeight = height / 2

                wavePath.moveTo(0f, midHeight)

                for (x in 0..width.toInt() step 10) {
                    val xPos = x.toFloat()
                    val yPos = (midHeight) + 
                               (20.dp.toPx() * sin((xPos / 100f) + phase)) * 0.5f +
                               (10.dp.toPx() * sin((xPos / 40f) + phase * 2)) * 0.3f 

                    wavePath.lineTo(xPos, yPos)
                }
                
                wavePath.lineTo(width, height)
                wavePath.lineTo(0f, height)
                wavePath.close()

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DashboardGreen.copy(alpha = 0.3f),
                            DashboardGreen.copy(alpha = 0.05f)
                        )
                    )
                )
                
                drawPath(
                    path = wavePath,
                    color = DashboardGreen,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DashboardGreen))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Uptime",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uptime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Text(
                            text = "Deep Sleep",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(painter = painterResource(R.drawable.ic_nightlight), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = batteryInfo.deepSleep,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryLevelSquareCard(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = DashboardGreen), modifier = Modifier.fillMaxSize().clickable { clipboardManager.setText(AnnotatedString(batteryInfo.level)) }) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(imageVector = Icons.Default.ArrowOutward, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.TopEnd).size(24.dp))
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(painter = painterResource(R.drawable.ic_battery_android_frame_full), contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                Text(text = batteryInfo.level, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "Battery Level", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun VoltageSquareCard(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Icon(imageVector = Icons.Default.ArrowOutward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.TopEnd).size(24.dp))
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Icon(imageVector = Icons.Outlined.Bolt, contentDescription = null, tint = Color(0xFFF1C40F), modifier = Modifier.size(32.dp).padding(bottom = 8.dp))
                Text(text = batteryInfo.voltage.removeSuffix(" mV"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Voltage (mV)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun HealthCircularCard(viewModel: BatteryViewModel) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()
    val healthText = batteryInfo.health
    val healthPercent = when(healthText.lowercase()) { "good" -> 1f; "fair" -> 0.7f; "poor" -> 0.4f; else -> 0.9f }
    val animatedProgress by animateFloatAsState(targetValue = healthPercent, animationSpec = tween(1500))

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.size(100.dp)) {
                drawArc(color = Color.LightGray.copy(alpha = 0.2f), startAngle = 140f, sweepAngle = 260f, useCenter = false, style = Stroke(width = 30f, cap = StrokeCap.Round))
                drawArc(color = DashboardGreen, startAngle = 140f, sweepAngle = 260f * animatedProgress, useCenter = false, style = Stroke(width = 30f, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = healthText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Condition", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SmallInfoCard(icon: ImageVector, title: String, value: String, color: Color) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ControlSwitchCard(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.painter.Painter) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
                    Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DashboardGreen, uncheckedThumbColor = Color.White, uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant))
        }
    }
}

@Composable
fun ThermalProfileDashboardItem(viewModel: BatteryViewModel, hazeState: HazeState) {
    val openTPD = rememberDialogState(initiallyVisible = false)
    val thermalSconfig by viewModel.thermalSconfig.collectAsState()
    val profileName = remember(thermalSconfig) { when (thermalSconfig) { "0" -> "Default"; "10" -> "Benchmark"; "11" -> "Browser"; "12" -> "Camera"; "8" -> "Dialer"; "13" -> "Gaming"; "14" -> "Streaming"; else -> "Unknown" } }
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth().clickable { openTPD.visible = true }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3E0)), contentAlignment = Alignment.Center) {
                    Icon(painter = painterResource(R.drawable.ic_battery_profile), contentDescription = null, tint = Color(0xFFE67E22))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Thermal Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Active: $profileName", style = MaterialTheme.typography.bodySmall, color = DashboardGreen)
                }
            }
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "Change", modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    DialogUnstyled(state = openTPD, hazeState = hazeState, title = "Select Profile", text = {
            Column {
                val options = listOf(Triple("0", "Default", painterResource(R.drawable.ic_cool)), Triple("13", "Gaming", Icons.Default.SportsEsports), Triple("10", "Benchmark", Icons.Default.Speed), Triple("14", "Streaming", Icons.Default.Videocam), Triple("12", "Camera", Icons.Default.PhotoCamera), Triple("11", "Browser", Icons.Default.Language), Triple("8", "Dialer", Icons.Default.Call))
                options.forEach { (id, name, icon) -> DialogTextButton(icon = icon, text = name, onClick = { viewModel.updateThermalSconfig(id); openTPD.visible = false }) }
            }
        }, confirmButton = { TextButton(onClick = { openTPD.visible = false }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) } })
}
