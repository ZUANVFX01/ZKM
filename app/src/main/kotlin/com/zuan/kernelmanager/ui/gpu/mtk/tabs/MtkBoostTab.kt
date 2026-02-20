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
import androidx.compose.ui.graphics.vector.ImageVector
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
fun MtkBoostTab(
    state: MtkViewModel.BoostState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onToggleFeature: (String, Boolean) -> Unit
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                stringResource(R.string.mtk_gaming_performance),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        // FPSGO
        item {
            BoostCard(
                title = stringResource(R.string.mtk_fpsgo_title),
                subtitle = stringResource(R.string.mtk_fpsgo_subtitle),
                icon = Icons.Default.Games,
                checked = state.isFpsGoEnabled,
                onCheckedChange = { onToggleFeature("fpsgo", it) },
                color = accentColor.copy(alpha = 0.2f),
                contentColor = accentColor,
                cardColor = cardColor,
                isGlassActive = isGlassActive,
                glassModifier = glassModifier
            )
        }
        
        // GED Game Mode
        item {
            BoostCard(
                title = stringResource(R.string.mtk_ged_game_mode_title),
                subtitle = stringResource(R.string.mtk_ged_game_mode_subtitle),
                icon = Icons.Default.SportsEsports,
                checked = state.isGedGameMode,
                onCheckedChange = { onToggleFeature("ged_game", it) },
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                cardColor = cardColor,
                isGlassActive = isGlassActive,
                glassModifier = glassModifier
            )
        }
        
        // Touch Boost
        item {
            BoostCard(
                title = stringResource(R.string.mtk_touch_boost_title),
                subtitle = stringResource(R.string.mtk_touch_boost_subtitle),
                icon = Icons.Default.TouchApp,
                checked = state.isTouchBoostEnabled,
                onCheckedChange = { onToggleFeature("touch_boost", it) },
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                cardColor = cardColor,
                isGlassActive = isGlassActive,
                glassModifier = glassModifier
            )
        }
        
        // Sched Boost
        item {
            BoostCard(
                title = stringResource(R.string.mtk_sched_boost_title),
                subtitle = stringResource(R.string.mtk_sched_boost_subtitle),
                icon = Icons.Default.Schedule,
                checked = state.isSchedBoostEnabled,
                onCheckedChange = { onToggleFeature("sched_boost", it) },
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                cardColor = cardColor,
                isGlassActive = isGlassActive,
                glassModifier = glassModifier
            )
        }
        
        // Gaming Preset Card
        item {
            Spacer(Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = accentColor.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = accentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Column {
                            Text(
                                stringResource(R.string.mtk_gaming_mode_preset),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Text(
                                stringResource(R.string.mtk_gaming_mode_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = accentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            onToggleFeature("fpsgo", true)
                            onToggleFeature("ged_game", true)
                            onToggleFeature("touch_boost", true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Games, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.mtk_activate_gaming_mode),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun BoostCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color,
    contentColor: Color,
    cardColor: Color,
    isGlassActive: Boolean,
    glassModifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isGlassActive) cardColor else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = color,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
