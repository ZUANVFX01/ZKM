/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.mtk.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.gpu.mtk.viewmodel.MtkViewModel
import com.zuan.kernelmanager.utils.MtkUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun MtkDramTab(
    state: MtkViewModel.DramState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onSetDramFreq: (String) -> Unit
) {
    val glassModifier = if (isGlassActive) {
        Modifier.hazeEffect(
            state = hazeState,
            style = HazeStyle(
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                blurRadius = 20.dp,
                noiseFactor = 0.05f,
                tints = emptyList()
            )
        )
    } else Modifier

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!state.isAvailable) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                        Text(
                            stringResource(R.string.mtk_dram_unavailable),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${stringResource(R.string.mtk_dram_type)}: ${state.type}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (state.type == MtkUtils.DramType.MMDVFS) {
                            Text(
                                "Path: /proc/mmdvfs/dump_setting",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            return@LazyColumn
        }
        
        // DRAM Info Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Column {
                        Text(
                            stringResource(R.string.mtk_dram_controller),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            when(state.type) {
                                MtkUtils.DramType.MMDVFS -> stringResource(R.string.mtk_dram_mmdvfs)
                                MtkUtils.DramType.DEVFREQ -> stringResource(R.string.mtk_dram_devfreq)
                                MtkUtils.DramType.MTK_FLIPER -> stringResource(R.string.mtk_dram_fliper)
                                else -> stringResource(R.string.mtk_dram_unknown)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                        if (state.currentStep != "-1") {
                            Text(
                                "${stringResource(R.string.mtk_dram_current_step)}: ${state.currentStep}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        
        // Frequency Selection
        if (state.availableFreqs.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accentColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Column {
                                Text(
                                    stringResource(R.string.mtk_dram_force_frequency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.mtk_dram_lock_opp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableFreqs.forEach { freq ->
                                val isSelected = state.freqMap[freq] == state.currentStep
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetDramFreq(freq) },
                                    label = { 
                                        Text(
                                            freq,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                        
                        if (state.currentStep != "-1") {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onSetDramFreq("-1") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.mtk_dram_reset_dvfs), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}
