/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.gpu.adreno.tabs

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.gpu.adreno.viewmodel.AdrenoViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun AdrenoFreqTab(
    state: AdrenoViewModel.FreqState,
    hazeState: HazeState,
    cardColor: Color,
    isGlassActive: Boolean,
    accentColor: Color,
    onUpdateMin: (String) -> Unit,
    onUpdateMax: (String) -> Unit,
    onUpdateGov: (String) -> Unit
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
        // Hero Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                color = accentColor.copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = accentColor
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "${state.currentFreq} MHz",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                        Text(
                            text = stringResource(R.string.adreno_current_frequency),
                            style = MaterialTheme.typography.bodyLarge,
                            color = accentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Frequency Limits Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardColor,
                modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        stringResource(R.string.adreno_freq_limits),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    FreqSelectorRow(
                        label = stringResource(R.string.adreno_minimum),
                        value = "${state.minFreq} MHz",
                        options = state.availableFreqs,
                        onSelect = onUpdateMin,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    FreqSelectorRow(
                        label = stringResource(R.string.adreno_maximum),
                        value = "${state.maxFreq} MHz",
                        options = state.availableFreqs.reversed(),
                        onSelect = onUpdateMax,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        // Governor Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = cardColor,
                modifier = Modifier.fillMaxWidth().then(if (isGlassActive) glassModifier else Modifier)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        stringResource(R.string.adreno_governor),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    if (state.availableGovs.isNotEmpty()) {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.availableGovs.forEachIndexed { index, gov ->
                                SegmentedButton(
                                    selected = state.governor == gov,
                                    onClick = { onUpdateGov(gov) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = state.availableGovs.size
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        gov.replace("_", " "),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (state.governor == gov) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${stringResource(R.string.adreno_current)}: ${state.governor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun FreqSelectorRow(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    color: Color,
    contentColor: Color
) {
    var showDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Surface(
            color = color,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.adreno_change),
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    stringResource(R.string.adreno_change),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
    
    if (showDialog) {
        FreqSelectionDialog(
            title = "$label ${stringResource(R.string.adreno_frequency)}",
            currentValue = value,
            options = options,
            onSelect = { 
                onSelect(it)
                showDialog = false 
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun FreqSelectionDialog(
    title: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { 
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(options) { freq ->
                    val isSelected = freq == currentValue.replace(" MHz", "")
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                               else MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { onSelect(freq) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                                              else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                "$freq MHz",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text(stringResource(R.string.adreno_cancel), fontWeight = FontWeight.Medium) 
            }
        }
    )
}
