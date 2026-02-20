/*
 * Original code from: Rem01Gaming (origami_kernel_manager)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.socmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    hazeState: HazeState,
    cardBg: Color, contentColor: Color, subContentColor: Color, activeColor: Color
) {
    val net by viewModel.net.collectAsStateWithLifecycle()

    val isCustomBg by settingsViewModel.isCustomBackground.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    val isGlassActive = isCustomBg && isHazeEnabled

    LazyColumn(
        modifier = Modifier.fillMaxSize(), 
        contentPadding = PaddingValues(bottom = 130.dp) 
    ) {
        item {
            SectionTitle(stringResource(R.string.tcp_congestion), subContentColor)
            GlassChipGroup(
                options = net.availableTcp,
                selected = net.tcpCongestion,
                activeColor = activeColor,
                cardBg = cardBg,
                contentColor = contentColor,
                subContentColor = subContentColor,
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardDarkness = cardDarkness,
                onSelected = { viewModel.updateValue(NetworkUtils.TCP_CONG, it) }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(stringResource(R.string.tcp_parameters), subContentColor)
            
            if (net.hasSyncookies) {
                GlassGridToggle(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.syn_cookies),
                    statusText = if(net.syncookies=="1") stringResource(R.string.enabled) else stringResource(R.string.disabled),
                    icon = Icons.Rounded.Cookie,
                    isActive = net.syncookies=="1",
                    activeColor = activeColor, cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                    isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
                ) { viewModel.updateValue(NetworkUtils.TCP_SYNCOOKIES, if(net.syncookies=="1") "0" else "1") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if(net.hasReuse) {
                    GlassGridToggle(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.tcp_reuse),
                        statusText = if(net.reuse=="1") stringResource(R.string.on) else stringResource(R.string.off),
                        icon = Icons.Rounded.Replay,
                        isActive = net.reuse=="1",
                        activeColor = activeColor, cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                        isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
                    ) { viewModel.updateValue(NetworkUtils.TCP_REUSE, if(net.reuse=="1") "0" else "1") }
                }
                if(net.hasFastOpen) {
                    GlassGridToggle(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.fast_open),
                        statusText = if(net.fastOpen!="0") stringResource(R.string.on) else stringResource(R.string.off),
                        icon = Icons.Rounded.FlashOn,
                        isActive = net.fastOpen!="0",
                        activeColor = activeColor, cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                        isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
                    ) { viewModel.updateValue(NetworkUtils.TCP_FASTOPEN, if(net.fastOpen=="0") "3" else "0") }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if(net.hasSack) {
                GlassGridToggle(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.tcp_sack),
                    statusText = if(net.sack=="1") stringResource(R.string.enabled) else stringResource(R.string.disabled),
                    icon = Icons.Rounded.DoneAll,
                    isActive = net.sack=="1",
                    activeColor = activeColor, cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                    isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
                ) { viewModel.updateValue(NetworkUtils.TCP_SACK, if(net.sack=="1") "0" else "1") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if(net.hasEcn) {
                GlassInfoCard(
                    title = stringResource(R.string.ecn),
                    value = net.ecn,
                    cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                    isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(stringResource(R.string.kernel_log), subContentColor)
            GlassInfoCard(
                title = stringResource(R.string.printk),
                value = net.printk,
                cardBg = cardBg, contentColor = contentColor, subContentColor = subContentColor,
                isGlassActive = isGlassActive, hazeState = hazeState, cardDarkness = cardDarkness
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GlassChipGroup(
    options: List<String>,
    selected: String,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float,
    onSelected: (String) -> Unit
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
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
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
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelected(option) },
                        label = { Text(option) },
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassGridToggle(
    modifier: Modifier = Modifier,
    title: String,
    statusText: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardDarkness: Float,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    
    val baseModifier = if (isGlassActive) {
        modifier
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
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                ),
                shape = shape
            )
            .clickable { onClick() }
    } else {
        modifier
            .clip(shape)
            .background(if (cardDarkness > 0 && contentColor == Color.White) Color.Black.copy(alpha = cardDarkness) else cardBg)
            .clickable { onClick() }
    }

    Card(
        modifier = baseModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeColor else subContentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) activeColor else subContentColor
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassInfoCard(
    title: String,
    value: String,
    cardBg: Color,
    contentColor: Color,
    subContentColor: Color,
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
                    colors = listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
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
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if (isGlassActive) Color.Transparent else cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
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
                color = subContentColor,
                maxLines = 1
            )
        }
    }
}
