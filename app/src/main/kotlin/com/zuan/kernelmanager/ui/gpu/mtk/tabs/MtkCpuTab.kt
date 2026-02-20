/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.mtk.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.gpu.mtk.viewmodel.MtkViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun MtkCpuTab(
    state: MtkViewModel.CpuMiscState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onSetCciMode: (String) -> Unit,
    onSetPowerMode: (String) -> Unit,
    onSetEemOffset: (String, String) -> Unit
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

    if (!state.isAvailable) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState),
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
                        Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Text(
                    stringResource(R.string.mtk_cpu_unavailable),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // CCI Card
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.DeveloperMode,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Column {
                            Text(
                                stringResource(R.string.mtk_cci_interconnect),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.mtk_cache_coherent),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("0" to stringResource(R.string.mtk_normal), 
                               "1" to stringResource(R.string.mtk_performance)).forEachIndexed { index, (val_, label) ->
                            SegmentedButton(
                                selected = state.cciMode == val_,
                                onClick = { onSetCciMode(val_) },
                                shape = SegmentedButtonDefaults.itemShape(index, 2),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    label,
                                    fontWeight = if (state.cciMode == val_) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.mtk_big_little_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Power Mode Card
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Column {
                            Text(
                                stringResource(R.string.mtk_cpu_power_mode),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${stringResource(R.string.mtk_current_mode)}: ${getPowerModeLabel(state.powerMode)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "0" to stringResource(R.string.mtk_normal),
                            "1" to stringResource(R.string.mtk_low_power),
                            "2" to stringResource(R.string.mtk_balance),
                            "3" to stringResource(R.string.mtk_performance)
                        ).forEach { (val_, label) ->
                            val selected = state.powerMode == val_
                            Surface(
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer 
                                       else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSetPowerMode(val_) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { onSetPowerMode(val_) }
                                    )
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // EEM Offsets
        if (state.eemOffsets.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.mtk_voltage_offset),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }
            
            items(
                items = state.eemOffsets,
                key = { it.first }
            ) { (detName, currentOffset) ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            detName.replace("EEM_DET_", ""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${stringResource(R.string.mtk_current_mode)}: $currentOffset (${stringResource(R.string.mtk_eem_unit)})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(Modifier.height(12.dp))
                        
                        var input by remember(detName) { mutableStateOf(currentOffset) }
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            label = { Text(stringResource(R.string.mtk_offset_value)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            trailingIcon = {
                                FilledIconButton(
                                    onClick = { onSetEemOffset(detName, input) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

private fun getPowerModeLabel(mode: String): String = when(mode) {
    "1" -> "Low Power"
    "2" -> "Balance"
    "3" -> "Performance"
    else -> "Normal"
}
