/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.fpsmanager

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.services.FpsOverlayService
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FpsManagerOverlayContent(
    cardBg: Color, 
    contentColor: Color, 
    subContentColor: Color, 
    activeColor: Color, 
    elevation: Dp,
    isGlassActive: Boolean = false,
    hazeState: HazeState,
    glassModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isOverlayEnabled by remember { mutableStateOf(FpsOverlayService.isRunning) }
    
    var selectedStyle by remember { mutableStateOf(0) }
    var androidOrientation by remember { mutableStateOf(0) }
    var selectedColorHex by remember { mutableStateOf("#00FF00") }
    var textSize by remember { mutableStateOf(16f) }
    var widthScale by remember { mutableStateOf(1f) }
    var bgAlpha by remember { mutableStateOf(0.6f) }
    
    var showFps by remember { mutableStateOf(true) }
    var showCpu by remember { mutableStateOf(true) }
    var showWatts by remember { mutableStateOf(true) }
    var showTemp by remember { mutableStateOf(true) }
    var showRam by remember { mutableStateOf(true) }
    var showRender by remember { mutableStateOf(false) }
    
    var showColorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) { isOverlayEnabled = FpsOverlayService.isRunning; delay(1000) }
    }

    fun updateService(pos: String? = null) {
        if (isOverlayEnabled) {
            val intent = Intent(context, FpsOverlayService::class.java).apply {
                putExtra("STYLE", selectedStyle)
                putExtra("ORIENTATION", androidOrientation)
                putExtra("COLOR", selectedColorHex)
                putExtra("SIZE", textSize)
                putExtra("WIDTH_SCALE", widthScale)
                putExtra("ALPHA", bgAlpha)
                putExtra("SHOW_FPS", showFps)
                putExtra("SHOW_CPU", showCpu)
                putExtra("SHOW_WATTS", showWatts)
                putExtra("SHOW_TEMP", showTemp)
                putExtra("SHOW_RAM", showRam)
                putExtra("SHOW_RENDER", showRender)
                if (pos != null) putExtra("POSITION", pos)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), modifier = Modifier.fillMaxSize()) {
        item {
            OverlayMasterSwitch(
                isOverlayEnabled, 
                activeColor, 
                contentColor, 
                subContentColor,
                isGlassActive,
                glassModifier
            ) { isChecked ->
                if (isChecked) {
                    if (!Settings.canDrawOverlays(context)) {
                        Toast.makeText(context, "Grant Overlay Permission", Toast.LENGTH_LONG).show()
                        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                    } else {
                        updateService("TopLeft")
                        isOverlayEnabled = true
                    }
                } else {
                    context.stopService(Intent(context, FpsOverlayService::class.java))
                    isOverlayEnabled = false
                }
            }
        }

        item {
            val alpha by animateFloatAsState(if (isOverlayEnabled) 1f else 0.5f, label = "alpha")
            Column(modifier = Modifier.alpha(alpha).padding(top = 24.dp)) {
                
                SectionTitle(stringResource(R.string.fps_overlay_style), activeColor)
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyleChip(stringResource(R.string.fps_style_android), 0, selectedStyle, activeColor) { selectedStyle = 0; updateService() }
                    StyleChip(stringResource(R.string.fps_style_pc), 1, selectedStyle, activeColor) { selectedStyle = 1; updateService() }
                    StyleChip(stringResource(R.string.fps_style_mini), 2, selectedStyle, activeColor) { selectedStyle = 2; updateService() }
                }

                AnimatedVisibility(selectedStyle == 1) {
                    Column(Modifier.padding(top = 16.dp)) {
                        SectionTitle(stringResource(R.string.fps_pc_options), activeColor)
                        Row(Modifier.padding(horizontal = 16.dp)) {
                            MetricChip(stringResource(R.string.fps_show_render), showRender, activeColor) { showRender = it; updateService() }
                        }
                    }
                }

                AnimatedVisibility(selectedStyle == 0) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        SectionTitle(stringResource(R.string.fps_orientation), activeColor)
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StyleChip(stringResource(R.string.fps_vertical), 0, androidOrientation, activeColor) { androidOrientation = 0; updateService() }
                            StyleChip(stringResource(R.string.fps_horizontal), 1, androidOrientation, activeColor) { androidOrientation = 1; updateService() }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle(stringResource(R.string.fps_metrics), activeColor)
                FlowRow(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricChip(stringResource(R.string.fps_metric_fps), showFps, activeColor) { showFps = it; updateService() }
                    MetricChip(stringResource(R.string.fps_metric_cpu), showCpu, activeColor) { showCpu = it; updateService() }
                    MetricChip(stringResource(R.string.fps_metric_ram), showRam, activeColor) { showRam = it; updateService() }
                    MetricChip(stringResource(R.string.fps_metric_watts), showWatts, activeColor) { showWatts = it; updateService() }
                    MetricChip(stringResource(R.string.fps_metric_temp), showTemp, activeColor) { showTemp = it; updateService() }
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle(stringResource(R.string.fps_adjustments), activeColor)
                
                if (isGlassActive) {
                    Box(
                        modifier = glassModifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            AppearanceSlider(Icons.Default.FormatSize, stringResource(R.string.fps_text_size), textSize, 10f..40f, "${textSize.toInt()} sp", activeColor, { textSize = it }, { updateService() })
                            Spacer(Modifier.height(16.dp))
                            AppearanceSlider(Icons.Default.AspectRatio, stringResource(R.string.fps_width_scale), widthScale, 0.8f..2.5f, String.format("%.1fx", widthScale), activeColor, { widthScale = it }, { updateService() })
                            Spacer(Modifier.height(16.dp))
                            AppearanceSlider(Icons.Default.Opacity, stringResource(R.string.fps_opacity), bgAlpha, 0f..1f, "${(bgAlpha * 100).toInt()}%", activeColor, { bgAlpha = it }, { updateService() })
                        }
                    }
                } else {
                    Card(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp), 
                        shape = RoundedCornerShape(24.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            AppearanceSlider(Icons.Default.FormatSize, stringResource(R.string.fps_text_size), textSize, 10f..40f, "${textSize.toInt()} sp", activeColor, { textSize = it }, { updateService() })
                            Spacer(Modifier.height(16.dp))
                            AppearanceSlider(Icons.Default.AspectRatio, stringResource(R.string.fps_width_scale), widthScale, 0.8f..2.5f, String.format("%.1fx", widthScale), activeColor, { widthScale = it }, { updateService() })
                            Spacer(Modifier.height(16.dp))
                            AppearanceSlider(Icons.Default.Opacity, stringResource(R.string.fps_opacity), bgAlpha, 0f..1f, "${(bgAlpha * 100).toInt()}%", activeColor, { bgAlpha = it }, { updateService() })
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                SectionTitle(stringResource(R.string.fps_color_theme), if (selectedStyle == 0) activeColor else Color.Gray)
                if (selectedStyle != 0) {
                     Text("  *${stringResource(R.string.fps_color_android_only)}", color = subContentColor, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = { showColorDialog = true }, enabled = isOverlayEnabled && selectedStyle == 0) {
                        Icon(Icons.Default.Palette, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.fps_custom_color))
                    }
                    Spacer(Modifier.width(16.dp))
                    val presets = listOf("#00FF00" to Color.Green, "#FF0000" to Color.Red, "#FFFF00" to Color.Yellow, "#00FFFF" to Color.Cyan)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(presets) { (hex, color) ->
                            ColorPresetCircle(color, selectedColorHex == hex && selectedStyle == 0) {
                                if (selectedStyle == 0) { selectedColorHex = hex; updateService() }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showColorDialog) {
        SimpleColorPickerDialog(
            initialColor = try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch(e:Exception){ Color.Green },
            onDismiss = { showColorDialog = false },
            activeColor = activeColor, 
            cardBg = cardBg, 
            contentColor = contentColor,
            onColorSelected = { color -> selectedColorHex = String.format("#%06X", (0xFFFFFF and color.toArgb())); showColorDialog = false; updateService() }
        )
    }
}

@Composable
fun StyleChip(label: String, value: Int, selectedValue: Int, activeColor: Color, onClick: () -> Unit) {
    FilterChip(
        selected = value == selectedValue,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = activeColor.copy(alpha = 0.2f), selectedLabelColor = activeColor),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = value == selectedValue, selectedBorderColor = activeColor)
    )
}

@Composable
fun OverlayMasterSwitch(
    isEnabled: Boolean, 
    activeColor: Color, 
    contentColor: Color, 
    subContentColor: Color,
    isGlassActive: Boolean = false,
    glassModifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {
    val containerColor = if (isEnabled) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer
    
    val switchContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                        contentDescription = null, 
                        tint = if (isEnabled) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = stringResource(R.string.fps_overlay_active), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                    Text(text = if (isEnabled) stringResource(R.string.fps_overlay_showing) else stringResource(R.string.fps_overlay_tap_enable), style = MaterialTheme.typography.bodyMedium, color = subContentColor)
                }
            }
            Switch(
                checked = isEnabled, 
                onCheckedChange = onToggle, 
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, 
                    checkedTrackColor = activeColor, 
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline, 
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    }
    
    if (isGlassActive) {
        Box(
            modifier = glassModifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
        ) {
            switchContent()
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), 
            shape = RoundedCornerShape(28.dp), 
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            switchContent()
        }
    }
}

@Composable
fun MetricChip(label: String, selected: Boolean, activeColor: Color, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected, 
        onClick = { onToggle(!selected) }, 
        label = { Text(label) }, 
        leadingIcon = if (selected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null, 
        shape = RoundedCornerShape(12.dp), 
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = activeColor.copy(alpha = 0.2f), 
            selectedLabelColor = activeColor, 
            selectedLeadingIconColor = activeColor
        ), 
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, 
            selected = selected, 
            selectedBorderColor = activeColor, 
            disabledBorderColor = Color.Transparent
        )
    )
}

@Composable
fun AppearanceSlider(
    icon: ImageVector, 
    label: String, 
    value: Float, 
    valueRange: ClosedFloatingPointRange<Float>, 
    displayValue: String, 
    activeColor: Color, 
    onValueChange: (Float) -> Unit, 
    onFinished: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween, 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) { 
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(displayValue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = activeColor)
        }
        Slider(
            value = value, 
            onValueChange = onValueChange, 
            onValueChangeFinished = onFinished, 
            valueRange = valueRange, 
            colors = SliderDefaults.colors(
                thumbColor = activeColor, 
                activeTrackColor = activeColor, 
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun ColorPresetCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp, 
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent, 
                shape = CircleShape
            )
    )
}

@Composable
fun SimpleColorPickerDialog(
    initialColor: Color, 
    onDismiss: () -> Unit, 
    activeColor: Color, 
    cardBg: Color, 
    contentColor: Color, 
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }
    val currentColor = Color(red, green, blue)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = cardBg), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.fps_custom_color), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = contentColor)
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(currentColor).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)))
                Spacer(modifier = Modifier.height(24.dp))
                ColorSlider(stringResource(R.string.fps_color_red), red, Color.Red) { red = it }
                ColorSlider(stringResource(R.string.fps_color_green), green, Color.Green) { green = it }
                ColorSlider(stringResource(R.string.fps_color_blue), blue, Color.Blue) { blue = it }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { 
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), color = contentColor) } 
                    Button(onClick = { onColorSelected(currentColor) }, colors = ButtonDefaults.buttonColors(containerColor = activeColor)) { 
                        Text(stringResource(R.string.btn_apply)) 
                    } 
                }
            }
        }
    }
}

@Composable
fun ColorSlider(label: String, value: Float, color: Color, enabled: Boolean = true, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
        Slider(
            value = value, 
            onValueChange = onValueChange, 
            enabled = enabled, 
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color), 
            modifier = Modifier.weight(1f)
        )
    }
}
