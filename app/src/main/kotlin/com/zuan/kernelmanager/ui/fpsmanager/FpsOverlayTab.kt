/*
 * Copyright (c) 2025 ZKM
 * UI Overlay: Advanced Settings (Color Picker, Size, Metrics)
 */
package com.zuan.kernelmanager.ui.fpsmanager

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zuan.kernelmanager.services.FpsOverlayService
import kotlinx.coroutines.delay

@Composable
fun FpsManagerOverlayContent() {
    val context = LocalContext.current
    
    // --- STATE SETTINGS ---
    var isOverlayEnabled by remember { mutableStateOf(FpsOverlayService.isRunning) }
    var selectedPosition by remember { mutableStateOf("TopLeft") }
    
    // Customization State
    var selectedColorHex by remember { mutableStateOf("#00FF00") }
    var textSize by remember { mutableStateOf(16f) }
    var bgAlpha by remember { mutableStateOf(0.6f) }
    
    // Metrics State
    var showFps by remember { mutableStateOf(true) }
    var showCpu by remember { mutableStateOf(true) }
    var showWatts by remember { mutableStateOf(true) }
    var showTemp by remember { mutableStateOf(true) }
    
    // Dialog State
    var showColorDialog by remember { mutableStateOf(false) }

    // Sync Service Status
    LaunchedEffect(Unit) {
        while(true) {
            isOverlayEnabled = FpsOverlayService.isRunning
            delay(1000)
        }
    }

    // Fungsi Update Service (Kirim semua settingan baru)
    fun updateService(pos: String? = null) {
        if (isOverlayEnabled) {
            val intent = Intent(context, FpsOverlayService::class.java).apply {
                putExtra("COLOR", selectedColorHex)
                if (pos != null) putExtra("POSITION", pos)
                putExtra("SIZE", textSize)
                putExtra("ALPHA", bgAlpha)
                putExtra("SHOW_FPS", showFps)
                putExtra("SHOW_CPU", showCpu)
                putExtra("SHOW_WATTS", showWatts)
                putExtra("SHOW_TEMP", showTemp)
            }
            context.startForegroundService(intent)
        }
    }

    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        
        // --- MASTER SWITCH ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Overlay", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(if (isOverlayEnabled) "Active" else "Inactive", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = isOverlayEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (!Settings.canDrawOverlays(context)) {
                                Toast.makeText(context, "Grant Overlay Permission", Toast.LENGTH_LONG).show()
                                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                                return@Switch
                            }
                            // Start Service dengan settingan saat ini
                            updateService(selectedPosition)
                            isOverlayEnabled = true
                        } else {
                            context.stopService(Intent(context, FpsOverlayService::class.java))
                            isOverlayEnabled = false
                        }
                    }
                )
            }
        }
        
        val contentAlpha = if (isOverlayEnabled) 1f else 0.5f
        
        Column(modifier = Modifier.alpha(contentAlpha)) {
            
            // --- METRICS TOGGLE ---
            Text("Displayed Metrics", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricCheckbox("FPS", showFps) { showFps = it; updateService() }
                        MetricCheckbox("CPU Load", showCpu) { showCpu = it; updateService() }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricCheckbox("Watt (Pwr)", showWatts) { showWatts = it; updateService() }
                        MetricCheckbox("Temp", showTemp) { showTemp = it; updateService() }
                    }
                }
            }

            // --- APPEARANCE ---
            Text("Appearance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            
            // Size Slider
            Text("Text Size: ${textSize.toInt()} sp", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = textSize,
                onValueChange = { textSize = it },
                onValueChangeFinished = { updateService() },
                valueRange = 10f..40f,
                enabled = isOverlayEnabled
            )
            
            // Alpha Slider
            Text("Background Opacity: ${(bgAlpha * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = bgAlpha,
                onValueChange = { bgAlpha = it },
                onValueChangeFinished = { updateService() },
                valueRange = 0f..1f,
                enabled = isOverlayEnabled
            )

            // --- POSITION ---
            Spacer(modifier = Modifier.height(8.dp))
            Text("Position", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                listOf("TopLeft", "TopRight", "BottomLeft").forEach { pos ->
                    FilterChip(
                        selected = selectedPosition == pos,
                        onClick = { 
                             selectedPosition = pos
                             updateService(pos)
                        },
                        label = { Text(pos) },
                        enabled = isOverlayEnabled
                    )
                }
            }

            // --- CUSTOM COLOR PICKER ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Text Color", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { showColorDialog = true }, enabled = isOverlayEnabled) {
                    Text("Custom Color")
                }
            }
            
            // Quick Presets
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                val presets = listOf("#00FF00" to Color.Green, "#FF0000" to Color.Red, "#FFFF00" to Color.Yellow, "#FFFFFF" to Color.White)
                presets.forEach { (hex, color) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable(enabled = isOverlayEnabled) {
                                selectedColorHex = hex
                                updateService()
                            }
                            .border(if (selectedColorHex == hex) 3.dp else 0.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    )
                }
                // Preview Selected Custom Color (Jika bukan preset)
                if (presets.none { it.first == selectedColorHex }) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(selectedColorHex)))
                            .border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    )
                }
            }
            Spacer(modifier = Modifier.height(50.dp)) // Extra space bawah
        }
    }
    
    // --- DIALOG COLOR PICKER ---
    if (showColorDialog) {
        SimpleColorPickerDialog(
            initialColor = try { Color(android.graphics.Color.parseColor(selectedColorHex)) } catch(e:Exception){ Color.Green },
            onDismiss = { showColorDialog = false },
            onColorSelected = { color ->
                // Convert Color to Hex String
                val argb = color.toArgb()
                val hex = String.format("#%06X", (0xFFFFFF and argb))
                selectedColorHex = hex
                showColorDialog = false
                updateService()
            }
        )
    }
}

@Composable
fun MetricCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

// Simple RGB Slider Dialog
@Composable
fun SimpleColorPickerDialog(initialColor: Color, onDismiss: () -> Unit, onColorSelected: (Color) -> Unit) {
    var red by remember { mutableStateOf(initialColor.red) }
    var green by remember { mutableStateOf(initialColor.green) }
    var blue by remember { mutableStateOf(initialColor.blue) }
    
    val currentColor = Color(red, green, blue)

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pick Custom Color", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preview Box
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).background(currentColor))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sliders
                Text("Red: ${(red * 255).toInt()}")
                Slider(value = red, onValueChange = { red = it }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red))
                
                Text("Green: ${(green * 255).toInt()}")
                Slider(value = green, onValueChange = { green = it }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green))
                
                Text("Blue: ${(blue * 255).toInt()}")
                Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..1f, colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onColorSelected(currentColor) }) { Text("Apply") }
                }
            }
        }
    }
}
