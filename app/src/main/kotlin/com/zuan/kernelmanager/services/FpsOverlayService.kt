/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zuan.kernelmanager.utils.FpsReader
import com.zuan.kernelmanager.utils.FpsRecorder
import com.zuan.kernelmanager.utils.MonitorReader
import com.zuan.kernelmanager.utils.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class FpsOverlayService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    companion object {
        var isRunning = false
        const val TAG = "FpsOverlayService"
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private lateinit var layoutParams: WindowManager.LayoutParams
    
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    // --- CONFIG VARIABLES ---
    private var styleMode by mutableStateOf(0) // 0=Android, 1=PC, 2=Mini
    private var androidOrientation by mutableStateOf(0) // 0=Vert, 1=Horz
    private var colorHex by mutableStateOf("#00FF00")
    private var textSizeSp by mutableStateOf(14f)
    private var bgAlpha by mutableStateOf(0.5f)
    private var widthScale by mutableStateOf(1f)
    
    // Toggle Metrics
    private var showFps by mutableStateOf(true)
    private var showCpu by mutableStateOf(true)
    private var showWatts by mutableStateOf(true)
    private var showTemp by mutableStateOf(true)
    private var showRam by mutableStateOf(true)
    private var showRender by mutableStateOf(false)

    // Drag vars
    private var initialX = 0; private var initialY = 0; private var initialTouchX = 0f; private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        ShellExecutor.init(this)
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParams = createLayoutParams(20, 100)
        startForegroundNotification()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        intent?.let {
            if (it.hasExtra("STYLE")) styleMode = it.getIntExtra("STYLE", 0)
            if (it.hasExtra("ORIENTATION")) androidOrientation = it.getIntExtra("ORIENTATION", 0)
            
            it.getStringExtra("COLOR")?.let { c -> colorHex = c }
            it.getStringExtra("POSITION")?.let { p -> resetPosition(p) }
            
            if (it.hasExtra("SIZE")) textSizeSp = it.getFloatExtra("SIZE", 14f)
            if (it.hasExtra("WIDTH_SCALE")) widthScale = it.getFloatExtra("WIDTH_SCALE", 1f)
            if (it.hasExtra("ALPHA")) bgAlpha = it.getFloatExtra("ALPHA", 0.5f)
            
            if (it.hasExtra("SHOW_FPS")) showFps = it.getBooleanExtra("SHOW_FPS", true)
            if (it.hasExtra("SHOW_CPU")) showCpu = it.getBooleanExtra("SHOW_CPU", true)
            if (it.hasExtra("SHOW_WATTS")) showWatts = it.getBooleanExtra("SHOW_WATTS", true)
            if (it.hasExtra("SHOW_TEMP")) showTemp = it.getBooleanExtra("SHOW_TEMP", true)
            if (it.hasExtra("SHOW_RAM")) showRam = it.getBooleanExtra("SHOW_RAM", true)
            if (it.hasExtra("SHOW_RENDER")) showRender = it.getBooleanExtra("SHOW_RENDER", false)
        }

        if (overlayView == null) setupOverlay()
        return START_STICKY
    }

    private fun setupOverlay() {
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FpsOverlayService)
            setViewTreeSavedStateRegistryOwner(this@FpsOverlayService)
            setViewTreeViewModelStoreOwner(this@FpsOverlayService)

            setContent {
                MainOverlayContent(
                    styleMode = styleMode,
                    orientation = androidOrientation,
                    colorHex = colorHex,
                    fontSize = textSizeSp,
                    bgAlpha = bgAlpha,
                    widthScale = widthScale,
                    metrics = MetricsState(showFps, showCpu, showWatts, showTemp, showRam, showRender)
                )
            }
            
            setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val params = layoutParams as WindowManager.LayoutParams
                            initialX = params.x; initialY = params.y
                            initialTouchX = event.rawX; initialTouchY = event.rawY
                            return true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val newX = initialX + (event.rawX - initialTouchX).toInt()
                            val newY = initialY + (event.rawY - initialTouchY).toInt()
                            val params = layoutParams as WindowManager.LayoutParams
                            params.x = newX; params.y = newY
                            windowManager.updateViewLayout(overlayView, params)
                            return true
                        }
                    }
                    return false
                }
            })
        }
        windowManager.addView(overlayView, layoutParams)
    }

    private fun resetPosition(position: String) {
        val params = layoutParams
        params.x = 20; params.y = 200
        windowManager.updateViewLayout(overlayView, params)
    }
    
    private fun createLayoutParams(xPos: Int, yPos: Int): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = xPos; params.y = yPos
        return params
    }
    
    private fun startForegroundNotification() {
         val channelId = "fps_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FPS Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("ZKM Overlay").setSmallIcon(android.R.drawable.ic_menu_info_details).build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        FpsRecorder.stopRecording(this)
        if (overlayView != null) { windowManager.removeView(overlayView); overlayView = null }
        store.clear()
    }
}

data class MetricsState(val fps: Boolean, val cpu: Boolean, val watt: Boolean, val temp: Boolean, val ram: Boolean, val showRender: Boolean)

// --- MAIN UI COMPOSER ---

@Composable
fun MainOverlayContent(
    styleMode: Int,
    orientation: Int,
    colorHex: String,
    fontSize: Float,
    bgAlpha: Float,
    widthScale: Float,
    metrics: MetricsState
) {
    val context = LocalContext.current
    val customColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Green }

    // Data Holders
    var fpsVal by remember { mutableStateOf("0") }
    var fpsFloat by remember { mutableFloatStateOf(0f) }
    var cpuVal by remember { mutableStateOf("0%") }
    var cpuInt by remember { mutableIntStateOf(0) }
    var wattVal by remember { mutableStateOf("0.0W") }
    var wattFloat by remember { mutableFloatStateOf(0f) }
    var tempVal by remember { mutableStateOf("0°C") }
    var tempFloat by remember { mutableFloatStateOf(0f) }
    var ramVal by remember { mutableStateOf("0") }
    var ramInt by remember { mutableIntStateOf(0) }
    var renderName by remember { mutableStateOf("FPS") }

    // Record State
    var isRec by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                val start = System.currentTimeMillis()
                try {
                    // Sync Record State
                    isRec = FpsRecorder.isRecording
                    isPaused = FpsRecorder.isPaused

                    // 1. Baca Sensor
                    if (metrics.fps) {
                         fpsFloat = FpsReader.getRealFps()
                         fpsVal = String.format("%.0f", fpsFloat)
                    }
                    if (metrics.cpu) {
                        cpuInt = MonitorReader.getCpuLoad()
                        cpuVal = "$cpuInt%"
                    }
                    if (metrics.watt) {
                        wattFloat = MonitorReader.getPowerWatt()
                        wattVal = String.format("%.1fW", wattFloat)
                    }
                    if (metrics.temp) {
                        tempFloat = MonitorReader.getBatteryTemp(context)
                        tempVal = String.format("%.1f°C", tempFloat)
                    }
                    if (metrics.ram) {
                        val r = MonitorReader.getRamInfo(context)
                        ramInt = r.usedMb
                        ramVal = "${r.usedMb}"
                    }
                    if (metrics.showRender) {
                        renderName = MonitorReader.getCurrentRenderer()
                    }

                    // 2. Kirim ke Recorder jika aktif
                    if (isRec) {
                        val currentPkg = MonitorReader.getForegroundPackage()
                        FpsRecorder.tick(context, currentPkg, fpsFloat, cpuInt, wattFloat, tempFloat, ramInt)
                    }

                } catch (e: Exception) {}
                delay(1000 - (System.currentTimeMillis() - start).coerceAtLeast(0))
            }
        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = bgAlpha), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .width(IntrinsicSize.Max)
            .widthIn(min = (80 * widthScale).dp)
    ) {
        // STRUKTUR VERTIKAL: ATAS (DATA) - BAWAH (TOMBOL)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            
            // --- BAGIAN ATAS: DATA SESUAI STYLE ---
            Box(Modifier.fillMaxWidth()) {
                when (styleMode) {
                    0 -> AndroidStyleOverlay(orientation, customColor, fontSize, metrics, fpsVal, cpuVal, wattVal, tempVal, ramVal)
                    1 -> PcStyleOverlay(fontSize, metrics, fpsVal, cpuVal, wattVal, tempVal, ramVal, renderName)
                    2 -> MiniMonitorOverlay(fontSize, metrics, fpsVal, cpuVal, tempVal)
                }
            }

            // --- BAGIAN BAWAH: TOMBOL RECORD ---
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(Modifier.height(4.dp))
            
            RecordControlButton(isRec, isPaused, context)
        }
    }
}

// --- TOMBOL RECORD (DI BAWAH) ---
@Composable
fun RecordControlButton(isRec: Boolean, isPaused: Boolean, context: android.content.Context) {
    Box(
        modifier = Modifier
            .size(24.dp) // Ukuran icon kecil pas di bawah
            .clip(CircleShape)
            .background(if (isRec) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .clickable {
                if (isRec) {
                    // STOP
                    FpsRecorder.stopRecording(context)
                } else {
                    // START
                    // Logic Fix: Jika package kosong (gagal detect), tetap start sebagai "Unknown App"
                    var pkg = MonitorReader.getForegroundPackage()
                    if (pkg.isEmpty()) pkg = "Unknown App" 
                    FpsRecorder.startRecording(pkg)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isRec) {
            if (isPaused) {
                // Kuning (Paused/Out of game)
                Icon(Icons.Default.Pause, null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
            } else {
                // Merah Kotak (Recording)
                Icon(Icons.Default.Stop, null, tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        } else {
            // Putih Bulat (Standby)
            Icon(Icons.Default.FiberManualRecord, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
        }
    }
}

// --- STYLE 1: ANDROID ---
@Composable
fun AndroidStyleOverlay(
    orientation: Int, color: Color, size: Float, m: MetricsState,
    fps: String, cpu: String, watt: String, temp: String, ram: String
) {
    if (orientation == 0) { // Vertical
        Column(horizontalAlignment = Alignment.Start) {
            if (m.fps) AndroidRow("FPS", fps, color, size)
            if (m.cpu) AndroidRow("CPU", cpu, color, size)
            if (m.ram) AndroidRow("RAM", "$ram MB", color, size)
            if (m.watt) AndroidRow("PWR", watt, color, size)
            if (m.temp) AndroidRow("TMP", temp, color, size)
        }
    } else { // Horizontal
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (m.fps) AndroidRow("FPS", fps, color, size)
            if (m.cpu) AndroidRow("CPU", cpu, color, size)
            if (m.ram) AndroidRow("RAM", ram, color, size)
        }
    }
}

@Composable
fun AndroidRow(label: String, value: String, color: Color, size: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label ", color = Color.LightGray, fontSize = (size * 0.7f).sp)
        Text(text = value, color = color, fontSize = size.sp, fontWeight = FontWeight.Bold)
    }
}

// --- STYLE 2: PC MODE ---
@Composable
fun PcStyleOverlay(
    size: Float, m: MetricsState,
    fps: String, cpu: String, watt: String, temp: String, ramMb: String,
    renderLabel: String
) {
    val font = FontFamily.Monospace
    val green = Color(0xFF00FF00)
    val blue = Color(0xFF00BFFF)
    val orange = Color(0xFFFF8C00)
    val white = Color.White
    
    val finalFpsLabel = if (m.showRender) renderLabel else "FPS"

    Column {
        if (m.temp || m.watt) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("GPU", color = green, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                val text = if(m.temp && m.watt) "$temp" else if(m.temp) temp else ""
                Text(text, color = orange, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
            }
        }
        if (m.ram) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MEM", color = green, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Text("$ramMb MB", color = orange, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
            }
        }
        if (m.cpu) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CPU", color = blue, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(16.dp))
                Text(cpu, color = orange, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
            }
        }
        if (m.fps) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(finalFpsLabel, color = Color(0xFFECA3A3), fontSize = size.sp, fontFamily = font)
                Spacer(Modifier.width(16.dp))
                Text(fps, color = white, fontSize = size.sp, fontFamily = font, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- STYLE 3: MINI MONITOR ---
@Composable
fun MiniMonitorOverlay(
    size: Float, m: MetricsState,
    fps: String, cpu: String, temp: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (m.fps) {
            Text(fps, color = Color.White, fontSize = (size * 1.2f).sp, fontWeight = FontWeight.ExtraBold)
            Text("FPS", color = Color.Gray, fontSize = (size * 0.6f).sp)
        }
        if (m.cpu || m.temp) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                if (m.cpu) Text(cpu, color = Color(0xFF00BFFF), fontSize = (size * 0.8f).sp, modifier = Modifier.padding(end=4.dp))
                if (m.temp) Text(temp, color = Color(0xFFFF8C00), fontSize = (size * 0.8f).sp)
            }
        }
    }
}
