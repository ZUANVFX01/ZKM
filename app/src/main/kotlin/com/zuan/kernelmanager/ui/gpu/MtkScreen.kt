/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.gpu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.utils.MtkUtils
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.soc.DashboardCard
//import com.zuan.kernelmanager.ui.soc.RowSwitch
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun MtkScreen(
    navController: NavController,
    viewModel: MtkViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // GPU Dialogs
    val openFixedFreq = rememberDialogState(false)
    val openMaxBound = rememberDialogState(false)
    val openCciMode = rememberDialogState(false)
    val openPowerMode = rememberDialogState(false)
    val openSchedBoost = rememberDialogState(false)

    // DRAM Dialogs
    val openDramMin = rememberDialogState(false)
    val openDramMax = rememberDialogState(false)
    val openDramFixed = rememberDialogState(false)
    val openDramGov = rememberDialogState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MediaTek Settings") },
                navigationIcon = {
                     IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                     }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // === FREQUENCY CONTROL ===
            item {
                Text("Frequency Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        DialogTextButton(text = "Fixed Freq: ${state.currentFreq}", onClick = { openFixedFreq.visible = true })
                        
                        if (state.mtkFixedIndex != "-1") {
                             Spacer(modifier = Modifier.height(8.dp))
                             Button(
                                onClick = { viewModel.resetFixedFreq() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                             ) { Text("Reset to Dynamic") }
                        }
                        
                        val maxBoundLabel = if (state.mtkMaxIndex == "-1") "Unlimited" else {
                             state.mtkFreqMap.entries.find { it.value.toIntOrNull() == state.mtkMaxIndex.toIntOrNull() }?.key + " MHz"
                        }
                        DialogTextButton(text = "Max Limit: $maxBoundLabel", onClick = { openMaxBound.visible = true })
                    }
                }
            }

            // === DRAM / MEMORY CONTROLLER (NEW FEATURE) ===
            if (state.dramState.type != MtkUtils.DramType.NONE) {
                item {
                    Text("DRAM Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            
                            if (state.dramState.type == MtkUtils.DramType.DEVFREQ) {
                                // Standard Devfreq Interface
                                DialogTextButton(text = "Min Freq: ${formatDramDisplay(state.dramState.minFreq)}", onClick = { openDramMin.visible = true })
                                DialogTextButton(text = "Max Freq: ${formatDramDisplay(state.dramState.maxFreq)}", onClick = { openDramMax.visible = true })
                                DialogTextButton(text = "Governor: ${state.dramState.currentGov}", onClick = { openDramGov.visible = true })
                            } else {
                                // MTK Fixed (Fliper) Interface
                                val currentLabel = if (state.dramState.currentIndex != "N/A") "OPP ${state.dramState.currentIndex}" else "Unknown"
                                DialogTextButton(text = "Set Freq (NO DVFS): $currentLabel", onClick = { openDramFixed.visible = true })
                                Text(
                                    "Note: This uses direct OPP locking. It overrides Dynamic Voltage Frequency Scaling.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // === ORIGAMI ADVANCED FEATURES ===
            if (state.hasPpm || state.hasCciMode || state.hasPowerMode || state.hasSchedBoost) {
                item {
                    Text("Advanced Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            
                            if (state.hasPpm) {
                                RowSwitch("PPM (Perf & Power Management)", state.isPpmEnabled) { viewModel.toggleFeature("ppm", it) }
                                MtkDivider()
                            }
                            
                            if (state.hasCciMode) {
                                val label = if (state.cciMode == "1") "Performance" else "Normal"
                                DialogTextButton(text = "CCI Mode: $label", onClick = { openCciMode.visible = true })
                                MtkDivider()
                            }
                            
                            if (state.hasPowerMode) {
                                val label = when(state.powerMode) {
                                    "1" -> "Low Power"
                                    "2" -> "Make"
                                    "3" -> "Performance"
                                    else -> "Normal"
                                }
                                DialogTextButton(text = "CPU Power Mode: $label", onClick = { openPowerMode.visible = true })
                                MtkDivider()
                            }

                            if (state.hasSchedBoost) {
                                val label = when(state.schedBoost) {
                                    "1" -> "Foreground"
                                    "2" -> "Boost All"
                                    else -> "Disabled"
                                }
                                DialogTextButton(text = "Sched Boost: $label", onClick = { openSchedBoost.visible = true })
                            }
                        }
                    }
                }
            }

            // === GED FEATURES ===
            item {
                Text("GED Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        RowSwitch("FPSGO", state.isFpsGoEnabled) { viewModel.toggleFeature("fpsgo", it) }
                        MtkDivider()
                        RowSwitch("GED KPI (Power Save)", state.isGedKpiEnabled) { viewModel.toggleFeature("ged_kpi", it) }
                        MtkDivider()
                        RowSwitch("GED Smart Boost", state.isGedBoostEnabled) { viewModel.toggleFeature("ged_boost", it) }
                        MtkDivider()
                        RowSwitch("GED Game Mode", state.isGedGameMode) { viewModel.toggleFeature("ged_game", it) }
                        MtkDivider()
                        RowSwitch("GED GPU Boost", state.isGedGpuBoost) { viewModel.toggleFeature("ged_gpu_boost", it) }
                        
                        if (state.isPerfmgrEnabled) {
                            MtkDivider()
                            RowSwitch("Perfmgr (FEAS)", state.isPerfmgrEnabled) { viewModel.toggleFeature("perfmgr", it) }
                        }
                    }
                }
            }

            // === POWER POLICY ===
            item {
                Text("Power Policy (Override Limits)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Enable these to ignore hardware safety limits. Use at your own risk!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
                
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        RowSwitch("Ignore Thermal Protect", state.powerPolicy.ignoreThermal) { viewModel.togglePowerPolicy("thermal", it) }
                        MtkDivider()
                        RowSwitch("Ignore Low Battery Limit", state.powerPolicy.ignoreLowBatt) { viewModel.togglePowerPolicy("low_batt", it) }
                        MtkDivider()
                        RowSwitch("Ignore Battery % Limit", state.powerPolicy.ignoreLowBattPercent) { viewModel.togglePowerPolicy("low_batt_p", it) }
                        MtkDivider()
                        RowSwitch("Ignore Overcurrent", state.powerPolicy.ignoreOverCurrent) { viewModel.togglePowerPolicy("oc", it) }
                        MtkDivider()
                        RowSwitch("Ignore Power Budget", state.powerPolicy.ignorePbm) { viewModel.togglePowerPolicy("pbm", it) }
                    }
                }
            }
        }
    }

    // === DIALOGS ===
    MtkSelectionDialog(openFixedFreq, hazeState, "Lock Frequency", state.availableFreq) { viewModel.updateFixedFreq(it) }
    MtkSelectionDialog(openMaxBound, hazeState, "Max Frequency Limit", state.availableFreq) { viewModel.updateMaxFreq(it) }

    // Advanced Feature Dialogs
    if (state.hasCciMode) {
        val cciItems = listOf("Normal" to "0", "Performance" to "1")
        MtkMapDialog(openCciMode, hazeState, "CCI Mode", cciItems) { viewModel.setCciMode(it) }
    }
    
    if (state.hasPowerMode) {
        val pwrItems = listOf("Normal" to "0", "Low Power" to "1", "Make" to "2", "Performance" to "3")
        MtkMapDialog(openPowerMode, hazeState, "Power Mode", pwrItems) { viewModel.setPowerMode(it) }
    }
    
    if (state.hasSchedBoost) {
        val schedItems = listOf("Disabled" to "0", "Foreground" to "1", "Boost All" to "2")
        MtkMapDialog(openSchedBoost, hazeState, "Sched Boost", schedItems) { viewModel.setSchedBoost(it) }
    }

    // DRAM Dialogs
    MtkSelectionDialog(openDramMin, hazeState, "DRAM Min Freq", state.dramState.availableFreqsDisplay) { viewModel.setDramFreq(it, "min") }
    MtkSelectionDialog(openDramMax, hazeState, "DRAM Max Freq", state.dramState.availableFreqsDisplay) { viewModel.setDramFreq(it, "max") }
    MtkSelectionDialog(openDramFixed, hazeState, "DRAM Fixed Freq", state.dramState.availableFreqsDisplay) { viewModel.setDramFreq(it, "fixed") }
    MtkSelectionDialog(openDramGov, hazeState, "DRAM Governor", state.dramState.availableGovs) { viewModel.setDramGov(it) }
}

@Composable
private fun MtkDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun MtkSelectionDialog(state: com.composables.core.DialogState, hazeState: dev.chrisbanes.haze.HazeState, title: String, items: List<String>, onSelect: (String) -> Unit) {
    DialogUnstyled(state = state, hazeState = hazeState, title = title, text = {
        LazyColumn { 
             items(items) { item -> 
                DialogTextButton(text = item, onClick = { onSelect(item); state.visible = false }) 
            } 
        }
    }, dismissButton = { TextButton(onClick = { state.visible = false }) { Text("Cancel") } })
}

@Composable
private fun MtkMapDialog(state: com.composables.core.DialogState, hazeState: dev.chrisbanes.haze.HazeState, title: String, items: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    DialogUnstyled(state = state, hazeState = hazeState, title = title, text = {
        LazyColumn { 
             items(items) { (label, value) -> 
                DialogTextButton(text = label, onClick = { onSelect(value); state.visible = false }) 
            } 
        }
    }, dismissButton = { TextButton(onClick = { state.visible = false }) { Text("Cancel") } })
}

private fun formatDramDisplay(raw: String): String {
    // Helper simple untuk display Hz -> MHz jika raw value berupa angka besar
    return try {
        val num = raw.toLong()
        if (num > 1000000) "${num / 1000000} MHz" else raw
    } catch (e: Exception) { raw }
}