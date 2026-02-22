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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.gpu.mtk.viewmodel.MtkViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MtkCpuTab(
    state: MtkViewModel.CpuMiscState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onSetCci: (String) -> Unit,
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // ==========================================
        // FALLBACK JIKA DEVICE MODERN / GKI (TIDAK ADA NODE)
        // ==========================================
        if (!state.isAvailable) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                Icons.Default.Memory,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Generic CPU Managed",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Perangkat ini menggunakan kernel modern (GKI). Kontrol Governor dan Frekuensi CPU Mediatek secara otomatis dikelola oleh subsistem Linux standar.\n\nSilakan gunakan menu CPU Dashboard utama untuk mengatur Governor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } 
        // ==========================================
        // UI KHUSUS MTK LAMA (HELIO G90T DLL)
        // ==========================================
        else {
            // CCI Mode & Power Mode
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "CPU Power Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))
                        
                        // CCI Mode
                        Text(
                            "CCI Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.cciMode == "0",
                                onClick = { onSetCci("0") },
                                label = { Text("Normal") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accentColor, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                            FilterChip(
                                selected = state.cciMode == "1",
                                onClick = { onSetCci("1") },
                                label = { Text("Performance") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accentColor, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        
                        // Power Mode
                        Text(
                            "Power Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("0" to "Normal", "1" to "Low Power", "2" to "Balance", "3" to "Performance").forEach { (mode, label) ->
                                FilterChip(
                                    selected = state.powerMode == mode,
                                    onClick = { onSetPowerMode(mode) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accentColor, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                                )
                            }
                        }
                    }
                }
            }
            
            // EEM Offsets
            if (state.eemOffsets.isNotEmpty()) {
                item {
                    Text(
                        "EEM Voltage Offsets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                
                items(state.eemOffsets) { (detName, currentOffset) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = accentColor)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    detName.replace("EEM_DET_", "CPU "),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            
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