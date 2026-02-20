/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.proces

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.MainActivity
import com.zuan.kernelmanager.utils.ProcessUtils
import com.zuan.kernelmanager.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.max

class FloatingProcessService : Service() {

    // [PENTING] Menambahkan state global agar UI tahu service sedang jalan atau tidak
    companion object {
        var isRunning = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    
    private val NOTIFICATION_ID = 101
    private val CHANNEL_ID = "floating_monitor_channel"

    // Simpan ukuran saat ini agar bisa di-update
    private var currentWidth = 0
    private var currentHeight = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true // Set status aktif
        startForegroundServiceNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Hitung ukuran awal (Default: Width 280dp, Height 350dp)
        val metrics = resources.displayMetrics
        currentWidth = (280 * metrics.density).toInt()
        currentHeight = (350 * metrics.density).toInt()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            currentWidth, // Gunakan ukuran fix
            currentHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 200
        }

        floatingView = ComposeView(this).apply {
            val lifecycleOwner = MyLifecycleOwner()
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            val viewModelStore = ViewModelStore()
            setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = viewModelStore
            })
            lifecycleOwner.performRestore(null)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
            
            setContent {
                FloatingScreenContent(
                    onClose = { stopSelf() },
                    onDrag = { x, y -> updateWindowPosition(x, y) },
                    onResize = { dx, dy -> updateWindowSize(dx, dy) }
                )
            }
        }

        windowManager.addView(floatingView, params)
    }
    
    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Process Monitor Overlay", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.fps_notification_title))
            .setContentText(getString(R.string.fps_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateWindowPosition(deltaX: Float, deltaY: Float) {
        params.x += deltaX.toInt()
        params.y += deltaY.toInt()
        try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
    }

    private fun updateWindowSize(deltaX: Float, deltaY: Float) {
        // Update size dengan batas minimal
        val metrics = resources.displayMetrics
        val minWidth = (200 * metrics.density).toInt()
        val minHeight = (150 * metrics.density).toInt()

        currentWidth = max(minWidth, currentWidth + deltaX.toInt())
        currentHeight = max(minHeight, currentHeight + deltaY.toInt())

        params.width = currentWidth
        params.height = currentHeight
        
        try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false // Set status mati
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
    }
}

// Helper Lifecycle
class MyLifecycleOwner : androidx.savedstate.SavedStateRegistryOwner {
    private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    private val savedStateRegistryController = androidx.savedstate.SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: androidx.savedstate.SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    fun handleLifecycleEvent(event: Lifecycle.Event) = lifecycleRegistry.handleLifecycleEvent(event)
    fun performRestore(savedState: android.os.Bundle?) = savedStateRegistryController.performRestore(savedState)
}

@Composable
fun FloatingScreenContent(
    onClose: () -> Unit, 
    onDrag: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    var processList by remember { mutableStateOf(emptyList<com.zuan.kernelmanager.utils.ProcessData>()) }
    var currentSort by remember { mutableStateOf(SortType.CPU) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(currentSort) { 
        withContext(Dispatchers.IO) {
            while (isActive) {
                try {
                    // Ambil lebih banyak data agar list penuh
                    val newData = ProcessUtils.getTopProcesses(context, 30, currentSort)
                    withContext(Dispatchers.Main) {
                        processList = newData
                        isError = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) { isError = true }
                }
                delay(1500)
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        // Gunakan Box sebagai container utama agar Resize Handle bisa ditumpuk (overlap)
        Box(
            modifier = Modifier
                .fillMaxSize() // Isi penuh ukuran WindowManager yang sudah diset di Service
                .padding(4.dp) // Sedikit padding luar agar shadow terlihat
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    
                    // --- HEADER: DRAG AREA ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.x, dragAmount.y)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = null, tint = Color.White.copy(0.7f), modifier = Modifier.size(24.dp))
                        Text(
                            text = stringResource(R.string.fps_monitor_title),
                            style = MaterialTheme.typography.titleSmall, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cancel), tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- TOGGLE SWITCH ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ToggleChip(stringResource(R.string.fps_cpu), currentSort == SortType.CPU, { currentSort = SortType.CPU }, Modifier.weight(1f))
                        ToggleChip(stringResource(R.string.fps_ram), currentSort == SortType.RES, { currentSort = SortType.RES }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- CONTENT LIST ---
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f) // Ambil sisa ruang
                            .fillMaxWidth()
                    ) {
                        if (isError) {
                            item { Text(stringResource(R.string.fps_error), color = Color.Red, fontSize = 12.sp) }
                        } else {
                            items(processList) { process ->
                                ProcessItemCompact(process, currentSort)
                            }
                        }
                    }
                    
                    // Space kecil di bawah agar tidak ketutup resize handle
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- RESIZE HANDLE (POJOK KANAN BAWAH) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp) // Area sentuh resize
                    .offset(x = 6.dp, y = 6.dp) // Sedikit keluar agar mudah diraih
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(dragAmount.x, dragAmount.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Visual Handle (Icon Panah Pojok)
                Icon(
                    imageVector = Icons.Rounded.DragIndicator, 
                    contentDescription = stringResource(R.string.fps_resize),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
fun ToggleChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.Black else Color.White.copy(0.7f))
    }
}

@Composable
fun ProcessItemCompact(process: com.zuan.kernelmanager.utils.ProcessData, sortType: SortType) {
    val progressValue = remember(process, sortType) {
        if (sortType == SortType.CPU) (process.cpu.replace("%", "").toFloatOrNull() ?: 0f) / 100f
        else {
            val ramVal = process.res.replace("M", "").replace("K", "").replace("B", "").toFloatOrNull() ?: 0f
            if (process.res.contains("G")) 1f else (ramVal / 500f).coerceIn(0f, 1f)
        }
    }
    val barColor = if (sortType == SortType.CPU) if (progressValue > 0.5f) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary else Color(0xFF69F0AE)
    val displayValue = if (sortType == SortType.CPU) process.cpu else process.res

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(process.appName, style = MaterialTheme.typography.bodySmall, color = Color.White, maxLines = 1, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { progressValue.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color = barColor, trackColor = Color.Gray.copy(0.3f),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(displayValue, style = MaterialTheme.typography.labelSmall, color = barColor, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.widthIn(min = 40.dp))
    }
}
