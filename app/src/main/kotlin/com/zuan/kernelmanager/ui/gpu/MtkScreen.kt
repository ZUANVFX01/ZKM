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
import com.zuan.kernelmanager.utils.MtkUtils
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

    val openFixedFreq = rememberDialogState(false)
    val openMaxBound = rememberDialogState(false)

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
            item {
                Text(
                    "Frequency Control", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DashboardCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        DialogTextButton(
                            text = "Fixed Freq: ${state.currentFreq}", 
                            onClick = { openFixedFreq.visible = true }
                        )
                        
                        if (state.mtkFixedIndex != "-1") {
                             Spacer(modifier = Modifier.height(8.dp))
                             Button(
                                onClick = { viewModel.resetFixedFreq() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                             ) {
                                Text("Reset to Dynamic")
                             }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val maxBoundLabel = if (state.mtkMaxIndex == "-1") "Unlimited" else {
                             state.mtkFreqMap.entries.find { it.value.toIntOrNull() == state.mtkMaxIndex.toIntOrNull() }?.key + " MHz"
                        }
                        DialogTextButton(
                            text = "Max Limit: $maxBoundLabel", 
                            onClick = { openMaxBound.visible = true }
                        )
                    }
                }
            }

            item {
                Text(
                    "Features", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DashboardCard(
                     backgroundColor = MaterialTheme.colorScheme.surface,
                     contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        RowSwitch("FPSGO", state.isFpsGoEnabled) { viewModel.toggleFeature("fpsgo", it) }
                        MtkDivider()
                        RowSwitch("GED KPI (Power Save)", state.isGedKpiEnabled) { viewModel.toggleFeature("ged_kpi", it) }
                        
                        if (state.isPerfmgrEnabled || MtkUtils.getMtkPerfmgrPath() != null) {
                            MtkDivider()
                            RowSwitch("Perfmgr (FEAS)", state.isPerfmgrEnabled) { viewModel.toggleFeature("perfmgr", it) }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    MtkSelectionDialog(openFixedFreq, hazeState, "Lock Frequency", state.availableFreq) { viewModel.updateFixedFreq(it) }
    MtkSelectionDialog(openMaxBound, hazeState, "Max Frequency Limit", state.availableFreq) { viewModel.updateMaxFreq(it) }
}

@Composable
private fun MtkDivider() {
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
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