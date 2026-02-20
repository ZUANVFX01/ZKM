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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.gpu.adreno.viewmodel.AdrenoViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun AdrenoAdvancedTab(
    state: AdrenoViewModel.AdvancedState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onToggleIdler: (Boolean) -> Unit,
    onUpdateIdlerParam: (String, String) -> Unit,
    onToggleSimpleGpu: (Boolean) -> Unit,
    onUpdateSimpleGpuParam: (String, String) -> Unit,
    onToggleKgsl: (String, Boolean) -> Unit,
    onUpdateKgslPwr: (String, String) -> Unit
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
        // KGSL Advanced Toggles
        if (state.hasKgsl3d0) {
            item {
                AdvancedCard(
                    title = stringResource(R.string.adreno_kgsl_advanced),
                    icon = Icons.Default.Memory,
                    color = accentColor.copy(alpha = 0.2f),
                    contentColor = accentColor,
                    cardColor = cardColor,
                    isGlassActive = isGlassActive,
                    glassModifier = glassModifier
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_force_no_nap),
                            subtitle = stringResource(R.string.adreno_no_nap_desc),
                            checked = state.forceNoNap,
                            accentColor = accentColor,
                            onCheckedChange = { onToggleKgsl("force_no_nap", it) }
                        )
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_force_clk_on),
                            subtitle = stringResource(R.string.adreno_clk_on_desc),
                            checked = state.forceClkOn,
                            accentColor = accentColor,
                            onCheckedChange = { onToggleKgsl("force_clk_on", it) }
                        )
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_force_bus_on),
                            subtitle = stringResource(R.string.adreno_bus_on_desc),
                            checked = state.forceBusOn,
                            accentColor = accentColor,
                            onCheckedChange = { onToggleKgsl("force_bus_on", it) }
                        )
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_bus_split),
                            subtitle = stringResource(R.string.adreno_bus_split_desc),
                            checked = state.busSplit,
                            accentColor = accentColor,
                            onCheckedChange = { onToggleKgsl("bus_split", it) }
                        )
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_throttling),
                            subtitle = stringResource(R.string.adreno_throttling_desc),
                            checked = state.throttling,
                            accentColor = accentColor,
                            onCheckedChange = { onToggleKgsl("throttling", it) }
                        )
                    }
                }
            }
            
            // Power Levels
            item {
                AdvancedCard(
                    title = stringResource(R.string.adreno_power_levels),
                    icon = Icons.Default.Bolt,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    cardColor = cardColor,
                    isGlassActive = isGlassActive,
                    glassModifier = glassModifier
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PwrLevelField(
                            label = stringResource(R.string.adreno_default),
                            currentValue = state.defaultPwrlevel,
                            accentColor = accentColor
                        ) { onUpdateKgslPwr("default_pwrlevel", it) }
                        PwrLevelField(
                            label = stringResource(R.string.adreno_maximum),
                            currentValue = state.maxPwrlevel,
                            accentColor = accentColor
                        ) { onUpdateKgslPwr("max_pwrlevel", it) }
                        PwrLevelField(
                            label = stringResource(R.string.adreno_thermal),
                            currentValue = state.thermalPwrlevel,
                            accentColor = accentColor
                        ) { onUpdateKgslPwr("thermal_pwrlevel", it) }
                    }
                }
            }
        }

        // Adreno Idler
        if (state.hasAdrenoIdler) {
            item {
                AdvancedCard(
                    title = stringResource(R.string.adreno_adreno_idler),
                    icon = Icons.Default.Speed,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    cardColor = cardColor,
                    isGlassActive = isGlassActive,
                    glassModifier = glassModifier
                ) {
                    Column {
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_enable_idler),
                            subtitle = stringResource(R.string.adreno_idler_desc),
                            checked = state.idlerActive,
                            accentColor = accentColor,
                            onCheckedChange = onToggleIdler
                        )
                        
                        if (state.idlerActive) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ParamDropdown(
                                    label = stringResource(R.string.adreno_idle_wait),
                                    current = state.idlerIdleWait,
                                    options = listOf("0", "10", "20", "30", "50", "99"),
                                    accentColor = accentColor,
                                    onSelect = { onUpdateIdlerParam("wait", it) }
                                )
                                ParamDropdown(
                                    label = stringResource(R.string.adreno_down_differential),
                                    current = state.idlerDownDiff,
                                    options = listOf("10", "20", "30", "50"),
                                    accentColor = accentColor,
                                    onSelect = { onUpdateIdlerParam("downdiff", it) }
                                )
                                ParamDropdown(
                                    label = stringResource(R.string.adreno_workload_threshold),
                                    current = state.idlerWorkload,
                                    options = listOf("1000", "2000", "5000", "7000", "10000"),
                                    accentColor = accentColor,
                                    onSelect = { onUpdateIdlerParam("workload", it) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Simple GPU
        if (state.hasSimpleGpu) {
            item {
                AdvancedCard(
                    title = stringResource(R.string.adreno_simple_gpu),
                    icon = Icons.Default.GraphicEq,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    cardColor = cardColor,
                    isGlassActive = isGlassActive,
                    glassModifier = glassModifier
                ) {
                    Column {
                        ExpressiveSwitchRow(
                            title = stringResource(R.string.adreno_enable_simple_gpu),
                            subtitle = stringResource(R.string.adreno_simple_gpu_desc),
                            checked = state.simpleGpuActive,
                            accentColor = accentColor,
                            onCheckedChange = onToggleSimpleGpu
                        )
                        
                        if (state.simpleGpuActive) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ParamDropdown(
                                    label = stringResource(R.string.adreno_laziness),
                                    current = state.simpleLaziness,
                                    options = listOf("0", "2", "4", "6", "8", "10"),
                                    accentColor = accentColor,
                                    onSelect = { onUpdateSimpleGpuParam("laziness", it) }
                                )
                                ParamDropdown(
                                    label = stringResource(R.string.adreno_ramp_threshold),
                                    current = state.simpleRampThreshold,
                                    options = listOf("0", "2", "4", "6", "8", "10"),
                                    accentColor = accentColor,
                                    onSelect = { onUpdateSimpleGpuParam("ramp", it) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AdvancedCard(
    title: String,
    icon: ImageVector,
    color: Color,
    contentColor: Color,
    cardColor: Color,
    isGlassActive: Boolean,
    glassModifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isGlassActive) cardColor else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = color,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun ExpressiveSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accentColor,
                checkedTrackColor = accentColor.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun PwrLevelField(label: String, currentValue: String, accentColor: Color, onApply: (String) -> Unit) {
    var text by remember(currentValue) { mutableStateOf(currentValue) }
    
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(
                onClick = { onApply(text) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = accentColor.copy(alpha = 0.2f),
                    contentColor = accentColor
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.adreno_save))
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ParamDropdown(
    label: String,
    current: String,
    options: List<String>,
    accentColor: Color,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box {
                TextButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = accentColor.copy(alpha = 0.2f),
                        contentColor = accentColor
                    )
                ) { 
                    Text(
                        current,
                        fontWeight = FontWeight.Bold
                    ) 
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    opt,
                                    fontWeight = if (opt == current) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            onClick = { onSelect(opt); expanded = false }
                        )
                    }
                }
            }
        }
    }
}
