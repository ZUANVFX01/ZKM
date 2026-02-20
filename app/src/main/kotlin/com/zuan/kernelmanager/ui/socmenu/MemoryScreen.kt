/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    hazeState: HazeState,
    cardBg: Color, contentColor: Color, subContentColor: Color, activeColor: Color
) {
    val mem by viewModel.mem.collectAsStateWithLifecycle()
    val zramState by viewModel.zram.collectAsStateWithLifecycle()
    val ioDevices by viewModel.ioDevices.collectAsStateWithLifecycle()

    val isCustomBg by settingsViewModel.isCustomBackground.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    val isGlassActive = isCustomBg && isHazeEnabled

    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(bottom = 130.dp)
    ) {
        item {
            SectionTitle(stringResource(R.string.zram_manager), subContentColor)
            ZramCard(
                state = zramState, 
                viewModel = viewModel, 
                activeColor = activeColor, 
                cardBg = cardBg, 
                contentColor = contentColor, 
                subContentColor = subContentColor,
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardDarkness = cardDarkness
            )
            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle(stringResource(R.string.io_scheduler), subContentColor)
        }

        if (ioDevices.isNotEmpty()) {
            items(ioDevices) { device ->
                IODeviceCard(
                    device = device,
                    viewModel = viewModel,
                    activeColor = activeColor, 
                    cardBg = cardBg,
                    contentColor = contentColor,
                    subContentColor = subContentColor,
                    isGlassActive = isGlassActive,
                    hazeState = hazeState,
                    cardDarkness = cardDarkness
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.no_storage_devices),
                    style = MaterialTheme.typography.bodyMedium,
                    color = subContentColor,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(stringResource(R.string.virtual_memory), subContentColor)
            
            if (mem.hasSwappiness) {
                SmartSlider(stringResource(R.string.swappiness), mem.swappiness, MemoryUtils.SWAPPINESS, viewModel, activeColor, cardBg, contentColor, subContentColor, isGlassActive, hazeState, cardDarkness)
                Spacer(modifier=Modifier.height(8.dp))
            }
            if (mem.hasVfsCachePressure) {
                SmartSlider(stringResource(R.string.vfs_cache_pressure), mem.vfsCachePressure, MemoryUtils.VFS_CACHE_PRESSURE, viewModel, activeColor, cardBg, contentColor, subContentColor, isGlassActive, hazeState, cardDarkness)
                Spacer(modifier=Modifier.height(8.dp))
            }
            if (mem.hasDirtyRatio) {
                SmartSlider(stringResource(R.string.dirty_ratio), mem.dirtyRatio, MemoryUtils.DIRTY_RATIO, viewModel, activeColor, cardBg, contentColor, subContentColor, isGlassActive, hazeState, cardDarkness)
                Spacer(modifier=Modifier.height(8.dp))
            }
            if (mem.hasDirtyBackgroundRatio) {
                SmartSlider(stringResource(R.string.dirty_bg_ratio), mem.dirtyBackgroundRatio, MemoryUtils.DIRTY_BACKGROUND_RATIO, viewModel, activeColor, cardBg, contentColor, subContentColor, isGlassActive, hazeState, cardDarkness)
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ZramCard(
    state: MemoryViewModel.ZramState,
    viewModel: MemoryViewModel,
    activeColor: Color, cardBg: Color, contentColor: Color, subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float
) {
    val shape = RoundedCornerShape(16.dp)
    
    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = cardDarkness),
                    blurRadius = 30.dp,
                    noiseFactor = 0.08f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (cardDarkness > 0 && contentColor == Color.White) Color.Black.copy(alpha = cardDarkness) else cardBg)
    }
    
    val containerColor = if (isGlassActive) Color.Transparent else cardBg

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else containerColor),
        shape = shape,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Memory, null, tint = activeColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.zram_status), style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.zram_info_format, state.sizeMb, state.activeAlgo), style = MaterialTheme.typography.bodySmall, color = subContentColor)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            var sliderVal by remember(state.sizeMb) { mutableFloatStateOf(state.sizeMb.toFloat()) }
            val maxZram = 20480f
            
            Text(stringResource(R.string.disk_size_mb, sliderVal.toInt()), color = contentColor, style = MaterialTheme.typography.labelMedium)
            Slider(
                value = sliderVal,
                onValueChange = { sliderVal = it },
                onValueChangeFinished = {
                    val snapped = (sliderVal.toInt() / 128) * 128
                    if (snapped != state.sizeMb) {
                         viewModel.applyZramChanges(snapped, state.activeAlgo)
                    }
                },
                valueRange = 0f..maxZram,
                steps = (maxZram / 128).toInt() - 1,
                colors = SliderDefaults.colors(thumbColor = activeColor, activeTrackColor = activeColor)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.compression_algorithm), color = contentColor, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.availableAlgos.forEach { algo ->
                    val isSelected = algo == state.activeAlgo
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.applyZramChanges(state.sizeMb, algo) },
                        label = { Text(algo) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = activeColor.copy(alpha = 0.2f),
                            selectedLabelColor = activeColor,
                            labelColor = contentColor,
                            containerColor = Color.Transparent
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = subContentColor.copy(alpha = 0.3f),
                            selectedBorderColor = activeColor
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun IODeviceCard(
    device: MemoryViewModel.IODeviceState,
    viewModel: MemoryViewModel,
    activeColor: Color, cardBg: Color, contentColor: Color, subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = cardDarkness),
                    blurRadius = 30.dp,
                    noiseFactor = 0.08f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (cardDarkness > 0 && contentColor == Color.White) Color.Black.copy(alpha = cardDarkness) else cardBg)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardBg),
        shape = shape,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Storage, null, tint = activeColor)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(device.name.uppercase(), style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.scheduler_format, device.activeScheduler), style = MaterialTheme.typography.bodySmall, color = subContentColor)
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = subContentColor
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = subContentColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(stringResource(R.string.select_scheduler), style = MaterialTheme.typography.labelMedium, color = subContentColor)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        device.availableSchedulers.forEach { sched ->
                            FilterChip(
                                selected = sched == device.activeScheduler,
                                onClick = { viewModel.setIOScheduler(device.name, sched) },
                                label = { Text(sched) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = activeColor, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(stringResource(R.string.tunables), style = MaterialTheme.typography.labelMedium, color = subContentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    IOTunableInput("nr_requests", device.nrRequests, device.name, viewModel, contentColor, activeColor)
                    IOTunableInput("read_ahead_kb", device.readAheadKb, device.name, viewModel, contentColor, activeColor)
                    IOTunableInput("rq_affinity", device.rqAffinity, device.name, viewModel, contentColor, activeColor)
                    IOTunableInput("rotational", device.rotational, device.name, viewModel, contentColor, activeColor)
                    IOTunableInput("add_random", device.addRandom, device.name, viewModel, contentColor, activeColor)
                    IOTunableInput("iostats", device.iostats, device.name, viewModel, contentColor, activeColor)
                }
            }
        }
    }
}

@Composable
fun IOTunableInput(
    label: String,
    currentValue: String,
    devName: String,
    viewModel: MemoryViewModel,
    textColor: Color,
    activeColor: Color
) {
    var textState by remember(currentValue) { mutableStateOf(currentValue) }

    OutlinedTextField(
        value = textState,
        onValueChange = { textState = it },
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = activeColor,
            unfocusedBorderColor = textColor.copy(alpha = 0.3f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                viewModel.setIOTunable(devName, label, textState)
            }
        ),
        trailingIcon = {
            IconButton(onClick = { viewModel.setIOTunable(devName, label, textState) }) {
                 Text(stringResource(R.string.btn_set).uppercase(), fontSize = 10.sp, color = activeColor, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SmartSlider(
    title: String,
    systemValue: String,
    path: String,
    viewModel: MemoryViewModel,
    activeColor: Color, cardBg: Color, contentColor: Color, subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float
) {
    val sysFloat = systemValue.replace(Regex("[^0-9]"), "").toFloatOrNull() ?: 0f
    var localValue by remember(sysFloat) { mutableFloatStateOf(sysFloat) }
    
    GlassSliderCard(
        title = title,
        value = localValue.toInt().toString(), 
        activeColor = activeColor, 
        cardBg = cardBg, 
        contentColor = contentColor, 
        subContentColor = subContentColor,
        isGlassActive = isGlassActive,
        hazeState = hazeState,
        cardDarkness = cardDarkness,
        onSliderChange = { newValue -> localValue = newValue },
        onInteractionEnd = { finalValue -> viewModel.updateVmValue(path, finalValue.toInt().toString()) }
    )
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassSliderCard(
    title: String,
    value: String,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float,
    onSliderChange: (Float) -> Unit,
    onInteractionEnd: (Float) -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    val modifier = if (isGlassActive) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.Black.copy(alpha = cardDarkness),
                    blurRadius = 30.dp,
                    noiseFactor = 0.08f,
                    tints = emptyList()
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
    } else {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (cardDarkness > 0 && contentColor == Color.White) Color.Black.copy(alpha = cardDarkness) else cardBg)
    }

    val currentValueFloat = value.toFloatOrNull() ?: 0f

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = activeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = currentValueFloat,
                onValueChange = onSliderChange,
                onValueChangeFinished = { onInteractionEnd(currentValueFloat) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = activeColor,
                    activeTrackColor = activeColor,
                    inactiveTrackColor = subContentColor.copy(alpha = 0.2f)
                )
            )
        }
    }
}
