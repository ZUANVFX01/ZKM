/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.adreno.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
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
import com.zuan.kernelmanager.ui.gpu.adreno.viewmodel.AdrenoViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun AdrenoPowerTab(
    state: AdrenoViewModel.PowerState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onToggleThrottling: (Boolean) -> Unit,
    onUpdateBoost: (String) -> Unit
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // GPU Throttling Card
        if (state.hasGpuThrottling) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (state.gpuThrottling == "1") 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (state.gpuThrottling != "1" && isGlassActive) glassModifier else Modifier)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (state.gpuThrottling == "1") 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    accentColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    if (state.gpuThrottling == "1") Icons.Default.Warning else Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (state.gpuThrottling == "1") 
                                        MaterialTheme.colorScheme.onError 
                                    else 
                                        accentColor,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Column {
                                Text(
                                    stringResource(R.string.adreno_gpu_throttling),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.gpuThrottling == "1") 
                                        MaterialTheme.colorScheme.onErrorContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    if (state.gpuThrottling == "1") stringResource(R.string.adreno_performance_limited) 
                                    else stringResource(R.string.adreno_full_performance),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (state.gpuThrottling == "1") 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = state.gpuThrottling == "1",
                            onCheckedChange = onToggleThrottling,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.error,
                                checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                            )
                        )
                    }
                }
            }
        }
        
        // Adreno Boost Card
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
                                Icons.Default.Bolt,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Column {
                            Text(
                                stringResource(R.string.adreno_adreno_boost),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${stringResource(R.string.adreno_level)} ${state.adrenoBoost}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    val boostLevels = listOf(
                        "0" to stringResource(R.string.adreno_off),
                        "1" to stringResource(R.string.adreno_low),
                        "2" to stringResource(R.string.adreno_med),
                        "3" to stringResource(R.string.adreno_high)
                    )
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        boostLevels.forEachIndexed { index, (level, label) ->
                            val selected = state.adrenoBoost == level
                            SegmentedButton(
                                selected = selected,
                                onClick = { onUpdateBoost(level) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = boostLevels.size
                                ),
                                modifier = Modifier.weight(1f),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = accentColor,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.adreno_boost_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}
