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
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.soc.DashboardCard
import com.zuan.kernelmanager.ui.soc.RowSwitch
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

    val openMinFreq = rememberDialogState(false)
    val openMaxFreq = rememberDialogState(false)
    val openGov = rememberDialogState(false)
    val openAdreno = rememberDialogState(false)

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

            item {
                Text("Power & Boost", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        RowSwitch("GPU Throttling", state.gpuThrottling == "1") { viewModel.updateGPUThrottling(it) }
                        DialogTextButton(text = "Adreno Boost: ${state.adrenoBoost}", onClick = { openAdreno.visible = true })
                        
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
        }
    }

    // Dialogs
    AdrenoSelectionDialog(openMinFreq, hazeState, "GPU Min Freq", state.availableFreq) { viewModel.updateFreq("min", it) }
    AdrenoSelectionDialog(openMaxFreq, hazeState, "GPU Max Freq", state.availableFreq) { viewModel.updateFreq("max", it) }
    AdrenoSelectionDialog(openGov, hazeState, "GPU Governor", state.availableGov) { viewModel.updateGov(it) }
    AdrenoSelectionDialog(openAdreno, hazeState, "Adreno Boost", listOf("0", "1", "2", "3")) { viewModel.updateAdrenoBoost(it) }
}

@Composable
private fun AdrenoDivider() {
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
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
