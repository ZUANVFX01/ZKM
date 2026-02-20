/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke

// --- SHARED UI COMPONENTS ---

@Composable
fun SettingsItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String, 
    onClick: () -> Unit, 
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String, 
    checked: Boolean, 
    onCheckedChange: (Boolean) -> Unit, 
    accentColor: Color, 
    iconTint: Color, 
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange, 
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, 
                checkedTrackColor = accentColor, 
                checkedBorderColor = accentColor
            )
        )
    }
}

@Composable
fun RowScope.BgTypeButton(type: BgType, icon: ImageVector, selected: BgType, onClick: (BgType) -> Unit) {
    val isSelected = type == selected
    Box(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent)
            .clickable { onClick(type) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun RowScope.ExpressiveThemeCard(index: Int, title: String, selectedIndex: Int, onClick: (Int) -> Unit) {
    val isSelected = index == selectedIndex
    Box(
        modifier = Modifier
            .weight(1f)
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick(index) }
    ) {
        ExpressiveWallpaperCanvas(themeId = index)
        Box(modifier = Modifier.fillMaxSize().border(2.dp, if(isSelected) Color.White else Color.Transparent, RoundedCornerShape(16.dp)))
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp), tint = Color.Black)
            }
        }
    }
}

// [BARU] Advanced Color Picker Dialog dengan HSV
@Composable
fun AdvancedColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var value by remember { mutableFloatStateOf(1f) }
    
    // Convert initial color to HSV
    LaunchedEffect(initialColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (initialColor.red * 255).toInt(),
            (initialColor.green * 255).toInt(),
            (initialColor.blue * 255).toInt(),
            hsv
        )
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }
    
    val currentColor = Color.hsv(hue, saturation, value)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Color", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Color Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Hue Slider (Rainbow)
                Text("Hue", style = MaterialTheme.typography.labelMedium)
                HueSlider(
                    hue = hue,
                    onHueChange = { hue = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Saturation Slider
                Text("Saturation", style = MaterialTheme.typography.labelMedium)
                SaturationSlider(
                    saturation = saturation,
                    hue = hue,
                    value = value,
                    onSaturationChange = { saturation = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Value/Brightness Slider
                Text("Brightness", style = MaterialTheme.typography.labelMedium)
                ValueSlider(
                    value = value,
                    hue = hue,
                    saturation = saturation,
                    onValueChange = { value = it },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // RGB Values
                Text(
                    "RGB: ${(currentColor.red * 255).toInt()}, ${(currentColor.green * 255).toInt()}, ${(currentColor.blue * 255).toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { 
            Button(
                onClick = { onColorSelected(currentColor) },
                colors = ButtonDefaults.buttonColors(containerColor = currentColor)
            ) { 
                Text("Apply", color = if (currentColor.luminance() > 0.5f) Color.Black else Color.White) 
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") } 
        }
    )
}

@Composable
fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.height(40.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Red, Color.Yellow, Color.Green, 
                    Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                )
            )
            drawRect(brush = gradientBrush)
        }
        Slider(
            value = hue,
            onValueChange = onHueChange,
            valueRange = 0f..360f,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
fun SaturationSlider(
    saturation: Float,
    hue: Float,
    value: Float,
    onSaturationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = Color.hsv(hue, 0f, value)
    val endColor = Color.hsv(hue, 1f, value)
    
    Box(modifier = modifier.height(40.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(startColor, endColor)
            )
            drawRect(brush = gradientBrush)
        }
        Slider(
            value = saturation,
            onValueChange = onSaturationChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ValueSlider(
    value: Float,
    hue: Float,
    saturation: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = Color.hsv(hue, saturation, 0f)
    val endColor = Color.hsv(hue, saturation, 1f)
    
    Box(modifier = modifier.height(40.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(startColor, endColor)
            )
            drawRect(brush = gradientBrush)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxSize(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            )
        )
    }
}

// [BARU] Simple RGB Color Picker untuk quick select
@Composable
fun SimpleColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val currentColor = Color(red, green, blue)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Custom Color") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                        .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Red: ${(red * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = red, 
                    onValueChange = { red = it }, 
                    colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                )
                Text("Green: ${(green * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = green, 
                    onValueChange = { green = it }, 
                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                )
                Text("Blue: ${(blue * 255).toInt()}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = blue, 
                    onValueChange = { blue = it }, 
                    colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                )
            }
        },
        confirmButton = { 
            Button(onClick = { onColorSelected(currentColor) }) { Text("Apply") } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") } 
        }
    )
}

// [BARU] Weather Effect Selector Component
@Composable
fun WeatherEffectSelector(
    selectedEffect: WeatherEffect,
    onEffectSelected: (WeatherEffect) -> Unit,
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Weather Effect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Effect buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherEffectButton(
                effect = WeatherEffect.NONE,
                label = "None",
                isSelected = selectedEffect == WeatherEffect.NONE,
                onClick = { onEffectSelected(WeatherEffect.NONE) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                effect = WeatherEffect.FOG,
                label = "Fog",
                isSelected = selectedEffect == WeatherEffect.FOG,
                onClick = { onEffectSelected(WeatherEffect.FOG) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                effect = WeatherEffect.RAIN,
                label = "Rain",
                isSelected = selectedEffect == WeatherEffect.RAIN,
                onClick = { onEffectSelected(WeatherEffect.RAIN) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WeatherEffectButton(
                effect = WeatherEffect.SNOW,
                label = "Snow",
                isSelected = selectedEffect == WeatherEffect.SNOW,
                onClick = { onEffectSelected(WeatherEffect.SNOW) },
                modifier = Modifier.weight(1f)
            )
            WeatherEffectButton(
                effect = WeatherEffect.SUN_RAYS,
                label = "Sun",
                isSelected = selectedEffect == WeatherEffect.SUN_RAYS,
                onClick = { onEffectSelected(WeatherEffect.SUN_RAYS) },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        
        // Intensity slider
        if (selectedEffect != WeatherEffect.NONE) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Intensity: ${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = intensity,
                onValueChange = onIntensityChange,
                valueRange = 0.1f..1f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun WeatherEffectButton(
    effect: WeatherEffect,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// [BARU] Background Adjustments Component
@Composable
fun BackgroundAdjustments(
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    blurStrength: Float,
    onBlurStrengthChange: (Float) -> Unit,
    isBlurEnabled: Boolean,
    onBlurEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Background Adjustments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Saturation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Saturation",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${(saturation * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = saturation,
            onValueChange = onSaturationChange,
            valueRange = 0f..2f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Blur toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Blur Background",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Apply blur to wallpaper",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isBlurEnabled,
                onCheckedChange = onBlurEnabledChange
            )
        }
        
        // Blur strength
        if (isBlurEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Blur Strength",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${blurStrength.toInt()}dp",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = blurStrength,
                onValueChange = onBlurStrengthChange,
                valueRange = 5f..50f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- CANVAS ART ---
val ExpressiveThemeNames = listOf("Minty Fresh", "Ocean Depth", "Berry Smooth", "Earth Tones")

@Composable
fun ExpressiveWallpaperCanvas(themeId: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        when (themeId) {
            0 -> { // MINTY
                drawRect(Color(0xFFE0F2F1))
                drawCircle(Color(0xFF263238), radius = width * 0.6f, center = Offset(0f, height))
                drawCircle(Color(0xFF00897B).copy(alpha=0.8f), radius = width * 0.5f, center = Offset(width * 0.2f, height * 0.6f))
                drawCircle(Color(0xFFA5D6A7).copy(alpha=0.6f), radius = width * 0.7f, center = Offset(width, 0f))
            }
            1 -> { // OCEAN
                drawRect(Color(0xFFE3F2FD))
                drawCircle(Color(0xFF0D47A1), radius = width * 0.3f, center = Offset(width * 0.8f, height * 0.2f))
                drawCircle(Color(0xFF42A5F5).copy(alpha=0.7f), radius = width * 0.8f, center = Offset(0f, height * 0.5f))
                drawCircle(Color(0xFF1565C0), radius = width * 0.5f, center = Offset(width, height))
            }
            2 -> { // BERRY
                drawRect(Color(0xFFFCE4EC))
                drawOval(Color(0xFF7B1FA2), topLeft = Offset(-100f, height * 0.6f), size = Size(width, height * 0.6f))
                drawCircle(Color(0xFFF48FB1), radius = width * 0.6f, center = Offset(width, 0f))
                drawCircle(Color(0xFF4A148C), radius = width * 0.25f, center = Offset(width * 0.8f, height * 0.2f))
            }
            3 -> { // EARTH
                drawRect(Color(0xFFFFF3E0))
                drawCircle(Color(0xFF3E2723), radius = width * 0.6f, center = Offset(width * 0.2f, height))
                drawCircle(Color(0xFFEF6C00).copy(alpha=0.8f), radius = width * 0.7f, center = Offset(width, 0f))
                drawCircle(Color(0xFFFFCC80), radius = width * 0.4f, center = Offset(0f, height * 0.4f))
            }
        }
    }
}

// [BARU] Color Preset Grid untuk banyak warna
@Composable
fun ColorPresetGrid(
    selectedColor: AppThemeColor?,
    isDynamic: Boolean,
    isCustomColor: Boolean,
    customColor: Color?,
    onColorSelected: (AppThemeColor) -> Unit,
    onDynamicSelected: () -> Unit,
    onCustomColorSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Color Palette",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Dynamic color option
        DynamicColorOption(
            isSelected = isDynamic,
            onClick = onDynamicSelected
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Custom color option
        CustomColorOption(
            isSelected = isCustomColor,
            customColor = customColor ?: Color(0xFF4A6595),
            onClick = onCustomColorSelected
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Preset colors grid
        val rows = availableColors.chunked(4)
        rows.forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowColors.forEach { color ->
                    val isSelected = !isDynamic && !isCustomColor && selectedColor?.name == color.name
                    ColorPresetItem(
                        color = color,
                        isSelected = isSelected,
                        onClick = { onColorSelected(color) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty slots
                repeat(4 - rowColors.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DynamicColorOption(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF4285F4), Color(0xFF34A853), 
                            Color(0xFFFBBC05), Color(0xFFEA4335), Color(0xFF4285F4)
                        )
                    )
                )
                .border(
                    3.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Dynamic Color",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "Based on your wallpaper",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomColorOption(
    isSelected: Boolean,
    customColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(customColor)
                .border(
                    3.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (customColor.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                "Custom Color",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                "Create your own theme",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ColorPresetItem(
    color: AppThemeColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    3.dp,
                    if (isSelected) color.primary else Color.Transparent,
                    CircleShape
                )
        ) {
            // Segmented color indicator
            Canvas(modifier = Modifier.fillMaxSize()) {
                val halfW = size.width / 2
                val halfH = size.height / 2
                drawRect(color = color.primary, topLeft = Offset(0f, 0f), size = Size(halfW, halfH))
                drawRect(color = color.secondary, topLeft = Offset(halfW, 0f), size = Size(halfW, halfH))
                drawRect(color = color.tertiary, topLeft = Offset(0f, halfH), size = Size(halfW, halfH))
                drawRect(color = color.primary.copy(alpha = 0.4f), topLeft = Offset(halfW, halfH), size = Size(halfW, halfH))
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            color.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
