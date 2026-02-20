/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.fpsmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.HazeStyle

@Composable
fun ModernActionCard(
    title: String, 
    icon: ImageVector, 
    modifier: Modifier = Modifier,
    isGlassActive: Boolean = false,
    hazeState: HazeState? = null
) {
    val cardModifier = if (isGlassActive && hazeState != null) {
        modifier
            .height(100.dp)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = Color.White.copy(alpha = 0.2f),
                    blurRadius = 20.dp,
                    noiseFactor = 0.1f,
                    tints = emptyList(),
                    fallbackTint = dev.chrisbanes.haze.HazeTint(Color.Transparent)
                )
            )
    } else {
        modifier.height(100.dp)
    }
    
    val cardColors = if (isGlassActive) {
        CardDefaults.cardColors(containerColor = Color.Transparent)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    }

    Card(
        modifier = cardModifier,
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = null)
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
