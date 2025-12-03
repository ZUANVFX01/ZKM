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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun GenericGpuScreen(
    navController: NavController,
    viewModel: GenericGpuViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val openMinFreq = rememberDialogState(false)
    val openMaxFreq = rememberDialogState(false)
    val openGov = rememberDialogState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPU Settings") },
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
            if (!state.isSupported) {
                item {
                    Text("No supported GPU interface found.", color = MaterialTheme.colorScheme.error)
                }
            } else {
                item {
                    Text("Frequency Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    DashboardCard(backgroundColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            // Info Current Freq
                            Text(
                                text = "Current: ${state.curFreq} MHz",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            
                            DialogTextButton(text = "Min Freq: ${state.minFreq} MHz", onClick = { openMinFreq.visible = true })
                            DialogTextButton(text = "Max Freq: ${state.maxFreq} MHz", onClick = { openMaxFreq.visible = true })
                            DialogTextButton(text = "Governor: ${state.gov}", onClick = { openGov.visible = true })
                        }
                    }
                }
                
                item {
                    Text("Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Path: ${state.gpuPath}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Dialogs
    GenericSelectionDialog(openMinFreq, hazeState, "Set Min Frequency", state.availableFreqs) { viewModel.updateFreq("min", it) }
    GenericSelectionDialog(openMaxFreq, hazeState, "Set Max Frequency", state.availableFreqs) { viewModel.updateFreq("max", it) }
    GenericSelectionDialog(openGov, hazeState, "Set Governor", state.availableGovs) { viewModel.updateGov(it) }
}

@Composable
private fun GenericSelectionDialog(state: com.composables.core.DialogState, hazeState: dev.chrisbanes.haze.HazeState, title: String, items: List<String>, onSelect: (String) -> Unit) {
    DialogUnstyled(state = state, hazeState = hazeState, title = title, text = {
        LazyColumn { 
            items(items) { item -> 
                DialogTextButton(text = item, onClick = { onSelect(item); state.visible = false }) 
            } 
        }
    }, dismissButton = { TextButton(onClick = { state.visible = false }) { Text("Cancel") } })
}
