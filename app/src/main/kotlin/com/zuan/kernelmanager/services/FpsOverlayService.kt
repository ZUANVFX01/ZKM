/*
 * Copyright (c) 2025 ZKM
 * Service Overlay: FPS, CPU, Watts, Temp dengan Custom UI
 */
package com.zuan.kernelmanager.services

import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
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
    private var colorHex by mutableStateOf("#00FF00")
    private var textSizeSp by mutableStateOf(14f)
    private var bgAlpha by mutableStateOf(0.5f)
    
    // Toggle Metrics
    private var showFps by mutableStateOf(true)
    private var showCpu by mutableStateOf(true)    // Default true
    private var showWatts by mutableStateOf(true)  // Default true
    private var showTemp by mutableStateOf(true)   // Default true

    // Drag vars
    private var initialX = 0; private var initialY = 0; private var initialTouchX = 0f; private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        // 🔥 INISIALISASI SHELL DI SERVICE
        ShellExecutor.init(this)
        
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParams = createLayoutParams(20, 100)
        startForegroundNotification()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service onStartCommand")
        
        // --- TERIMA DATA DARI UI ---
        intent?.let {
            it.getStringExtra("COLOR")?.let { c -> colorHex = c }
            it.getStringExtra("POSITION")?.let { p -> resetPosition(p) }
            
            if (it.hasExtra("SIZE")) textSizeSp = it.getFloatExtra("SIZE", 14f)
            if (it.hasExtra("ALPHA")) bgAlpha = it.getFloatExtra("ALPHA", 0.5f)
            
            if (it.hasExtra("SHOW_FPS")) showFps = it.getBooleanExtra("SHOW_FPS", true)
            if (it.hasExtra("SHOW_CPU")) showCpu = it.getBooleanExtra("SHOW_CPU", true)  // Default true
            if (it.hasExtra("SHOW_WATTS")) showWatts = it.getBooleanExtra("SHOW_WATTS", true)  // Default true
            if (it.hasExtra("SHOW_TEMP")) showTemp = it.getBooleanExtra("SHOW_TEMP", true)  // Default true
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
                OverlayContent(
                    colorHex = colorHex,
                    fontSize = textSizeSp,
                    bgAlpha = bgAlpha,
                    showFps = showFps,
                    showCpu = showCpu,
                    showWatts = showWatts,
                    showTemp = showTemp
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
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        var newX = 20
        var newY = 100
        when (position) {
            "TopLeft" -> { newX = 20; newY = 100 }
            "TopRight" -> { newX = screenWidth - 250; newY = 100 }
            "BottomLeft" -> { newX = 20; newY = screenHeight - 300 }
            "BottomRight" -> { newX = screenWidth - 250; newY = screenHeight - 300 }
        }
        val params = layoutParams as WindowManager.LayoutParams
        params.x = newX
        params.y = newY
        overlayView?.let { windowManager.updateViewLayout(it, params) }
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
        params.x = xPos
        params.y = yPos
        return params
    }
    
    private fun startForegroundNotification() {
         val channelId = "fps_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FPS Overlay Service", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("ZKM Overlay")
            .setContentText("Monitoring Running")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        isRunning = false
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
        store.clear()
    }
}

@Composable
fun OverlayContent(
    colorHex: String,
    fontSize: Float,
    bgAlpha: Float,
    showFps: Boolean,
    showCpu: Boolean,
    showWatts: Boolean,
    showTemp: Boolean
) {
    // 1. Ambil Context di sini
    val context = LocalContext.current
    
    val textColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Green }

    var fpsValue by remember { mutableStateOf("0") }
    var cpuValue by remember { mutableStateOf("0%") }
    var wattValue by remember { mutableStateOf("0.0W") }
    var tempValue by remember { mutableStateOf("0°C") }
    
    // Debug state
    var debugInfo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                val start = System.currentTimeMillis()
                
                // 🔥 BACA DATA REAL
                try {
                    if (showFps) {
                        val fps = FpsReader.getRealFps()
                        fpsValue = String.format("%.0f", fps)
                    }
                    
                    if (showCpu) {
                        val cpu = MonitorReader.getCpuLoad()
                        cpuValue = "$cpu%"
                    }
                    
                    if (showWatts) {
                        val watts = MonitorReader.getPowerWatt()
                        wattValue = String.format("%.1fW", watts)
                    }
                    
                    if (showTemp) {
                        // 2. Kirim Context ke sini (Error Line 256 Fixed)
                        val temp = MonitorReader.getBatteryTemp(context)
                        tempValue = String.format("%.1f°C", temp)
                    }
                    
                    // Debug (Error Line 261 Fixed)
                    debugInfo = MonitorReader.getDebugInfo(context)
                    Log.d("OverlayContent", debugInfo)
                    
                } catch (e: Exception) {
                    Log.e("OverlayContent", "Error reading data: ${e.message}")
                }

                // Jaga refresh rate (1 detik)
                val sleepTime = 1000 - (System.currentTimeMillis() - start)
                if (sleepTime > 0) delay(sleepTime)
            }
        }
    }

    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = bgAlpha), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .widthIn(min = 80.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            if (showFps) StatRow("FPS", fpsValue, textColor, fontSize)
            if (showCpu) StatRow("CPU", cpuValue, textColor, fontSize)
            if (showWatts) StatRow("PWR", wattValue, textColor, fontSize)
            if (showTemp) StatRow("TMP", tempValue, textColor, fontSize)
        }
    }
}


@Composable
fun StatRow(label: String, value: String, color: Color, size: Float) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label ",
            color = Color.LightGray,
            fontSize = (size * 0.7f).sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = color,
            fontSize = size.sp,
            fontWeight = FontWeight.Bold
        )
    }
}