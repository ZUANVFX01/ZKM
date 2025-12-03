/*
 * Copyright (c) 2025 ZKM
 * UI Home: FIX LOGIC SWITCH (Force Init Root saat diklik)
 */
package com.zuan.kernelmanager.ui.fpsmanager

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.services.FpsOverlayService
import com.zuan.kernelmanager.utils.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FpsManagerHomeContent() {
    val context = LocalContext.current
    
    // State Mode Eksekusi (Root/Shizuku)
    var selectedMode by remember { mutableStateOf(ShellExecutor.preferredAccessType) }
    
    // State Service Real-time
    var isServiceRunning by remember { mutableStateOf(FpsOverlayService.isRunning) }

    // Init & Monitoring Loop
    LaunchedEffect(Unit) {
        // Init awal (Cek akses yang tersedia saat app dibuka)
        ShellExecutor.init(context)
        selectedMode = ShellExecutor.preferredAccessType
        
        // Loop cek status service
        while(true) {
            isServiceRunning = FpsOverlayService.isRunning
            delay(1000)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item { DeviceInfoCard() }

        item {
            ServiceStatusCard(
                isRunning = isServiceRunning,
                onToggleService = { 
                    if (isServiceRunning) {
                        context.stopService(Intent(context, FpsOverlayService::class.java))
                        isServiceRunning = false 
                    } else {
                        val intent = Intent(context, FpsOverlayService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                        isServiceRunning = true
                    }
                }
            )
        }

        item {
            ModeSelectionCard(
                currentMode = selectedMode,
                onModeSelected = { newMode ->
                    // Update UI State
                    selectedMode = newMode
                    // Update Logic Backend (PENTING)
                    ShellExecutor.preferredAccessType = newMode
                    
                    val modeName = if (newMode == ShellExecutor.AccessType.ROOT) "ROOT (LibSU)" else "SHIZUKU"
                    Toast.makeText(context, "Mode switched to: $modeName", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun DeviceInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Device Information", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            DeviceInfoRow("Manufacturer", Build.MANUFACTURER.uppercase())
            DeviceInfoRow("Model", Build.MODEL)
            DeviceInfoRow("Android", "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
        }
    }
}

@Composable
fun DeviceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
fun ServiceStatusCard(isRunning: Boolean, onToggleService: () -> Unit) {
    val containerColor = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val contentColor = if (isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Card(colors = CardDefaults.cardColors(containerColor = containerColor), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("FPS Service Status", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.CheckCircle else Icons.Default.Dangerous,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = contentColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "RUNNING" else "STOPPED",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )
                }
            }
            Button(
                onClick = onToggleService,
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (isRunning) "STOP" else "START")
            }
        }
    }
}

@Composable
fun ModeSelectionCard(currentMode: ShellExecutor.AccessType, onModeSelected: (ShellExecutor.AccessType) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Scope untuk menjalankan request Root async

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Execution Mode", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // --- ROOT OPTION (FIXED LOGIC) ---
            // Cek status root saat ini hanya untuk deskripsi visual
            val isRootDetected = try { Shell.getShell().isRoot } catch(e:Exception) { false }
            
            ModeOptionItem(
                title = "Root Access (LibSU)",
                desc = if (isRootDetected) "Granted & Active" else "Tap to Request Access",
                isSelected = currentMode == ShellExecutor.AccessType.ROOT,
                isEnabled = true, // Selalu enable agar user bisa klik untuk memancing prompt root
                onClick = {
                    // LOGIC BARU: Force Request Root saat diklik
                    scope.launch(Dispatchers.IO) {
                        try {
                            // Ini akan memicu popup Magisk/KSU jika belum diizinkan
                            val result = Shell.getShell().isRoot 
                            
                            withContext(Dispatchers.Main) {
                                if (result) {
                                    onModeSelected(ShellExecutor.AccessType.ROOT)
                                } else {
                                    Toast.makeText(context, "Root Access DENIED / Gagal", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error checking root: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // --- SHIZUKU OPTION ---
            val isShizukuReady = ShellExecutor.isShizukuAvailable()
            ModeOptionItem(
                title = "Shizuku (ADB)",
                desc = if (isShizukuReady) "Available" else "Not Detected / App Not Running",
                isSelected = currentMode == ShellExecutor.AccessType.SHIZUKU,
                isEnabled = isShizukuReady,
                onClick = { 
                     if (!ShellExecutor.isShizukuPermissionGranted()) {
                         ShellExecutor.requestShizukuPermission()
                     }
                     onModeSelected(ShellExecutor.AccessType.SHIZUKU) 
                }
            )
        }
    }
}

@Composable
fun ModeOptionItem(title: String, desc: String, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceContainer
    
    // Jika disabled, warnanya redup
    val actualBg = if (isEnabled) backgroundColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(actualBg)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected, 
            onClick = null, // null karena klik ditangani row
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), 
                color = if (isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
            )
            Text(
                text = desc, 
                style = MaterialTheme.typography.labelSmall, 
                color = if (isEnabled) Color.Gray else Color.LightGray
            )
        }
    }
}
