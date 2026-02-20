/*
 * Original code from: Rem01Gaming (origami_kernel_manager) and helloklf (vtools)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.mtk.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun MtkThermalTab(
    state: MtkViewModel.ThermalState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onToggleEara: (Boolean) -> Unit = {},
    onToggleEaraFake: (Boolean) -> Unit = {}
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
        // Hero Temperature Card
        item {
            val tempCelsius = state.gpuTemp / 1000
            val color = when {
                tempCelsius > 80 -> MaterialTheme.colorScheme.errorContainer
                tempCelsius > 60 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> accentColor.copy(alpha = 0.2f)
            }
            val contentColor = when {
                tempCelsius > 80 -> MaterialTheme.colorScheme.onErrorContainer
                tempCelsius > 60 -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> accentColor
            }
            
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = color,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = state.isThrottling,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError
                                )
                                Text(
                                    stringResource(R.string.mtk_thermal_throttling),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = contentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(
                            Icons.Default.Thermostat,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "$tempCelsius°C",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                    Text(
                        text = if (state.isThrottling) stringResource(R.string.mtk_temp_limit_exceeded) 
                               else stringResource(R.string.mtk_normal_temp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // EARA Controls
        if (state.hasEara) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.mtk_eara_thermal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.mtk_early_adaptation),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.earaEnabled,
                            onCheckedChange = onToggleEara
                        )
                    }
                }
            }
            
            if (state.earaEnabled) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.mtk_eara_fake_throttle),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    stringResource(R.string.mtk_fake_temp_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                            }
                            Switch(
                                checked = state.earaFakeThrottle,
                                onCheckedChange = onToggleEaraFake,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onErrorContainer,
                                    checkedTrackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Warning Card
        if (state.isThrottling) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            stringResource(R.string.mtk_throttling_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Info Text
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
            ) {
                Text(
                    stringResource(R.string.mtk_thermal_daemon_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}
