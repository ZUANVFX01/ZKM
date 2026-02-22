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
import androidx.compose.material.icons.filled.*
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MtkFreqTab(
    state: MtkViewModel.FreqState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onSetMin: (String) -> Unit,
    onSetMax: (String) -> Unit,
    onToggleDvfs: (Boolean) -> Unit,
    onLockFreq: (String) -> Unit,
    onUnlockFreq: () -> Unit,
    // Tambahan Callback untuk Mode Devfreq (GKI)
    onSetDevfreqMin: (String) -> Unit = {},
    onSetDevfreqMax: (String) -> Unit = {},
    onSetGovernor: (String) -> Unit = {}
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
        // UI KHUSUS MODE DEVFREQ (GKI MODERN)
        // ==========================================
        if (state.isDevfreq) {
            
            // Hero Card Current Freq
            item {
                CurrentFreqHeroCard(
                    currentFreq = state.currentFreq,
                    isLocked = false,
                    accentColor = accentColor
                )
            }

            // Governor Selector Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = accentColor)
                            Spacer(Modifier.width(12.dp))
                            Text("GPU Governor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableGovernors.forEach { gov ->
                                val isSelected = state.governor == gov
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetGovernor(gov) },
                                    label = { Text(gov.uppercase()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accentColor,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Min/Max Limits Devfreq Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Frequency Limits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(20.dp))
                        
                        DevfreqLimitSelector(
                            label = "Minimum Frequency",
                            currentValue = state.minFreq,
                            availableFreqs = state.availableFreqs,
                            onSelect = onSetDevfreqMin,
                            accentColor = accentColor
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        DevfreqLimitSelector(
                            label = "Maximum Frequency",
                            currentValue = state.maxFreq,
                            availableFreqs = state.availableFreqs,
                            onSelect = onSetDevfreqMax,
                            accentColor = accentColor
                        )
                    }
                }
            }
        } 
        
        // ==========================================
        // UI KHUSUS MODE GED/LEGACY (MTK LAMA)
        // ==========================================
        else {
            // DVFS Warning Card
            if (state.isDvfsEnabled && !state.isLocked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onError, modifier = Modifier.padding(10.dp))
                            }
                            Column {
                                Text(stringResource(R.string.mtk_dvfs_enabled_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(stringResource(R.string.mtk_dvfs_enabled_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
            
            // DVFS Toggle Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (state.isDvfsEnabled) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Bolt, null, tint = if (state.isDvfsEnabled) accentColor else MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(12.dp))
                            }
                            Column {
                                Text(stringResource(R.string.mtk_gpu_dvfs), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(if (state.isDvfsEnabled) stringResource(R.string.mtk_dynamic_scaling_on) else stringResource(R.string.mtk_static_frequency), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(checked = state.isDvfsEnabled, onCheckedChange = onToggleDvfs)
                    }
                }
            }
            
            // Hero Card Current Freq
            item {
                CurrentFreqHeroCard(
                    currentFreq = if (state.isLocked) state.lockedIndex else state.currentFreq,
                    isLocked = state.isLocked,
                    accentColor = accentColor
                )
            }
            
            // Lock Frequency Section
            if (!state.isDvfsEnabled || state.isLocked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Surface(shape = RoundedCornerShape(12.dp), color = accentColor.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.Lock, null, tint = accentColor, modifier = Modifier.padding(8.dp))
                                }
                                Column {
                                    Text(stringResource(R.string.mtk_lock_frequency), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text(stringResource(R.string.mtk_select_opp_index), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.availableFreqs.forEach { freq ->
                                    val isSelected = state.isLocked && state.freqMap[freq] == state.lockedIndex
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { if (state.isLocked && isSelected) onUnlockFreq() else onLockFreq(freq) },
                                        label = { Text("$freq MHz", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp)) } } else null,
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accentColor, selectedLabelColor = MaterialTheme.colorScheme.onPrimary, selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary),
                                        modifier = Modifier.height(40.dp)
                                    )
                                }
                            }
                            
                            if (state.isLocked) {
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(onClick = onUnlockFreq, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.LockOpen, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.mtk_unlock_frequency), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
            
            // Min/Max Limits (Legacy)
            if (state.isDvfsEnabled && !state.isLocked) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cardColor,
                        modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(stringResource(R.string.mtk_dvfs_limits), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(20.dp))
                            
                            LimitSelector(
                                label = stringResource(R.string.mtk_minimum_boost),
                                currentValue = state.currentMinIndex,
                                availableFreqs = state.availableFreqs,
                                freqMap = state.freqMap,
                                onSelect = onSetMin
                            )
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            
                            LimitSelector(
                                label = stringResource(R.string.mtk_maximum_limit),
                                currentValue = state.currentMaxIndex,
                                availableFreqs = state.availableFreqs,
                                freqMap = state.freqMap,
                                onSelect = onSetMax
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

// === KOMPONEN UI TAMBAHAN ===

@Composable
private fun CurrentFreqHeroCard(currentFreq: String, isLocked: Boolean, accentColor: Color) {
    val currentLabel = if (currentFreq == "N/A" || currentFreq == "Dynamic") "Dynamic" else if(currentFreq.contains("MHz")) currentFreq else "$currentFreq MHz"
    val color = if (isLocked) MaterialTheme.colorScheme.errorContainer else accentColor.copy(alpha = 0.2f)
    val contentColor = if (isLocked) MaterialTheme.colorScheme.onErrorContainer else accentColor
    
    Surface(shape = RoundedCornerShape(28.dp), color = color, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(16.dp), color = contentColor.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                Icon(if (isLocked) Icons.Default.Lock else Icons.Default.Speed, null, tint = contentColor, modifier = Modifier.padding(14.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(currentLabel, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = contentColor)
            Text(if (isLocked) stringResource(R.string.mtk_freq_locked) else stringResource(R.string.mtk_current_frequency), style = MaterialTheme.typography.bodyLarge, color = contentColor.copy(alpha = 0.8f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DevfreqLimitSelector(
    label: String,
    currentValue: String,
    availableFreqs: List<String>,
    onSelect: (String) -> Unit,
    accentColor: Color
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(if (currentValue.isEmpty()) "Unknown" else "$currentValue MHz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            availableFreqs.take(6).forEach { freq ->
                FilterChip(
                    selected = currentValue == freq,
                    onClick = { onSelect(freq) },
                    label = { Text("$freq MHz") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LimitSelector(
    label: String,
    currentValue: String,
    availableFreqs: List<String>,
    freqMap: Map<String, String>,
    onSelect: (String) -> Unit
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(if (currentValue == "-1") stringResource(R.string.mtk_dynamic) else "OPP Index: $currentValue", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = currentValue == "-1",
                onClick = { onSelect("-1") },
                label = { Text(stringResource(R.string.mtk_dynamic)) },
                modifier = Modifier.height(36.dp)
            )
            availableFreqs.take(4).forEach { freq ->
                val idx = freqMap[freq]
                FilterChip(
                    selected = currentValue == idx,
                    onClick = { onSelect(freq) },
                    label = { Text(freq) },
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}