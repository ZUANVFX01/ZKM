/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.gpu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.soc.DashboardCard
//import com.zuan.kernelmanager.ui.soc.RowSwitch
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun AdrenoScreen(
    navController: NavController,
    viewModel: AdrenoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Dialog States Core
    val openMinFreq = rememberDialogState(false)
    val openMaxFreq = rememberDialogState(false)
    val openGov = rememberDialogState(false)
    val openAdrenoBoost = rememberDialogState(false)

    // Dialog States Idler
    val openIdlerWait = rememberDialogState(false)
    val openIdlerWorkload = rememberDialogState(false)

    // Dialog States Bus DCVS (Dynamic)
    val openBusDialog = rememberDialogState(false)
    var selectedBus by remember { mutableStateOf<AdrenoViewModel.BusState?>(null) }
    var isBusMinTarget by remember { mutableStateOf(true) } // true = min, false = max

    var pwrLevel by remember(state.defaultPwrlevel) { mutableStateOf(state.defaultPwrlevel) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adreno Settings") },
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
            // === CORE SETTINGS ===
            item {
                Text("Core Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        DialogTextButton(text = "Min Freq: ${state.minFreq} MHz", onClick = { openMinFreq.visible = true })
                        DialogTextButton(text = "Max Freq: ${state.maxFreq} MHz", onClick = { openMaxFreq.visible = true })
                        DialogTextButton(text = "Governor: ${state.gov}", onClick = { openGov.visible = true })
                    }
                }
            }

            // === POWER & BOOST ===
            item {
                Text("Power & Boost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        
                        // FIX: Changed 'text' to 'title'
                        if (state.hasGpuThrottling) {
                            RowSwitch(
                                title = "GPU Throttling", 
                                checked = state.gpuThrottling == "1", 
                                onCheckedChange = { viewModel.updateGPUThrottling(it) }
                            )
                            AdrenoDivider()
                        }
                        
                        DialogTextButton(text = "Adreno Boost: ${state.adrenoBoost}", onClick = { openAdrenoBoost.visible = true })
                        
                        if (state.maxPwrlevel != "N/A") {
                            AdrenoDivider()
                            Text("Default Power Level: $pwrLevel", style = MaterialTheme.typography.bodyMedium)
                            Slider(
                                value = pwrLevel.toFloatOrNull() ?: 0f,
                                onValueChange = { pwrLevel = it.toInt().toString() },
                                onValueChangeFinished = { viewModel.updateDefaultPwrlevel(pwrLevel) },
                                valueRange = (state.maxPwrlevel.toFloatOrNull()?:0f)..(state.minPwrlevel.toFloatOrNull()?:0f),
                                steps = 5
                            )
                        }
                    }
                }
            }

            // === BUS FREQUENCY (DCVS) - NEW FEATURE ===
            if (state.hasBusDcvs && state.busComponents.isNotEmpty()) {
                item {
                    Text("Bus Frequency (DCVS)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            state.busComponents.forEachIndexed { index, bus ->
                                Column {
                                    Text(bus.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        // Min Freq Button
                                        TextButton(onClick = { 
                                            selectedBus = bus
                                            isBusMinTarget = true
                                            openBusDialog.visible = true
                                        }) {
                                            Text("Min: ${bus.minFreq}")
                                        }
                                        // Max Freq Button
                                        TextButton(onClick = { 
                                            selectedBus = bus
                                            isBusMinTarget = false
                                            openBusDialog.visible = true
                                        }) {
                                            Text("Max: ${bus.maxFreq}")
                                        }
                                    }
                                    if (index < state.busComponents.lastIndex) AdrenoDivider()
                                }
                            }
                        }
                    }
                }
            }

            // === ADRENO IDLER ===
            if (state.hasAdrenoIdler) {
                item {
                    Text("Adreno Idler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            RowSwitch("Enable Adreno Idler", state.idlerActive) { viewModel.toggleIdler(it) }
                            if (state.idlerActive) {
                                AdrenoDivider()
                                DialogTextButton(text = "Idle Wait: ${state.idlerIdleWait}", onClick = { openIdlerWait.visible = true })
                                DialogTextButton(text = "Idle Workload: ${state.idlerWorkload}", onClick = { openIdlerWorkload.visible = true })
                            }
                        }
                    }
                }
            }

            // === SIMPLE GPU ALGO ===
            if (state.hasSimpleGpu) {
                item {
                    Text("Simple GPU Algorithm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            RowSwitch("Enable Simple GPU", state.simpleGpuActive) { viewModel.toggleSimpleGpu(it) }
                        }
                    }
                }
            }
        }
    }

    // === DIALOGS ===
    AdrenoSelectionDialog(openMinFreq, hazeState, "GPU Min Freq", state.availableFreq) { viewModel.updateFreq("min", it) }
    AdrenoSelectionDialog(openMaxFreq, hazeState, "GPU Max Freq", state.availableFreq) { viewModel.updateFreq("max", it) }
    AdrenoSelectionDialog(openGov, hazeState, "GPU Governor", state.availableGov) { viewModel.updateGov(it) }
    AdrenoSelectionDialog(openAdrenoBoost, hazeState, "Adreno Boost", listOf("0", "1", "2", "3")) { viewModel.updateAdrenoBoost(it) }
    
    AdrenoSelectionDialog(openIdlerWait, hazeState, "Idle Wait", listOf("0", "10", "20", "30", "50", "99")) { viewModel.updateIdlerParam("wait", it) }
    AdrenoSelectionDialog(openIdlerWorkload, hazeState, "Idle Workload", listOf("1000", "2000", "5000", "7000", "10000")) { viewModel.updateIdlerParam("workload", it) }

    // Dynamic Bus Dialog
    selectedBus?.let { bus ->
        val title = if (isBusMinTarget) "${bus.name} Min Freq" else "${bus.name} Max Freq"
        AdrenoSelectionDialog(openBusDialog, hazeState, title, bus.availableFreqs) { freq ->
            viewModel.updateBusFreq(bus.name, if (isBusMinTarget) "min" else "max", freq)
        }
    }
}

@Composable
private fun AdrenoDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun AdrenoSelectionDialog(state: com.composables.core.DialogState, hazeState: dev.chrisbanes.haze.HazeState, title: String, items: List<String>, onSelect: (String) -> Unit) {
    DialogUnstyled(state = state, hazeState = hazeState, title = title, text = {
        LazyColumn { 
            items(items) { item -> 
                DialogTextButton(text = item, onClick = { onSelect(item); state.visible = false }) 
            } 
        }
    }, dismissButton = { TextButton(onClick = { state.visible = false }) { Text("Cancel") } })
}
