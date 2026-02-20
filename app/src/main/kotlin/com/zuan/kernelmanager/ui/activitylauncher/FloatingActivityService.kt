/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.activitylauncher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap // [FIX] Import KTX resmi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.topjohnwu.superuser.Shell
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.MainActivity
import com.zuan.kernelmanager.ui.proces.MyLifecycleOwner 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

// Data Class untuk History Log
data class ActivityLog(
    val pkg: String,
    val cls: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)

class FloatingActivityService : Service() {

    companion object {
        var isRunning = false
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var params: WindowManager.LayoutParams
    
    private val CHANNEL_ID = "activity_inspector_channel"
    private val NOTIF_ID = 102

    private var currentWidth = 0
    private var currentHeight = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundNotif()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val metrics = resources.displayMetrics
        
        // Default Size: Sedikit lebih besar untuk list
        currentWidth = (300 * metrics.density).toInt()
        currentHeight = (380 * metrics.density).toInt()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            currentWidth,
            currentHeight,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or 
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
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
                FloatingActivityContent(
                    onClose = { stopSelf() },
                    onDrag = { x, y -> updatePosition(x, y) },
                    onResize = { x, y -> updateSize(x, y) }
                )
            }
        }

        windowManager.addView(floatingView, params)
    }

    private fun updatePosition(deltaX: Float, deltaY: Float) {
        params.x += deltaX.toInt()
        params.y += deltaY.toInt()
        try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
    }

    private fun updateSize(deltaX: Float, deltaY: Float) {
        val metrics = resources.displayMetrics
        val minW = (200 * metrics.density).toInt()
        val minH = (150 * metrics.density).toInt()

        currentWidth = max(minW, currentWidth + deltaX.toInt())
        currentHeight = max(minH, currentHeight + deltaY.toInt())
        
        params.width = currentWidth
        params.height = currentHeight
        try { windowManager.updateViewLayout(floatingView, params) } catch (_: Exception) {}
    }

    private fun startForegroundNotif() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Activity Inspector", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Activity Logger Active")
            .setContentText("Recording activity history...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        if (::floatingView.isInitialized) {
            try { windowManager.removeView(floatingView) } catch (_: Exception) {}
        }
    }
}

@Composable
fun FloatingActivityContent(
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    // STATE: History Log
    val activityLogs = remember { mutableStateListOf<ActivityLog>() }
    var lastLoggedRaw by remember { mutableStateOf("") }
    
    // STATE: Settings
    var showSettings by remember { mutableStateOf(false) }
    var showIcons by remember { mutableStateOf(true) }
    var showTime by remember { mutableStateOf(true) }
    var showFullClass by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // --- LOGIC MONITORING ---
    LaunchedEffect(isPaused) {
        withContext(Dispatchers.IO) {
            while (isActive) {
                if (!isPaused) {
                    var foundString: String? = null

                    // CARA 1: Dumpsys Window (Brute Force Fix for MIUI)
                    try {
                        val out = Shell.cmd("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'").exec().out
                        val validLine = out.firstOrNull { it.contains("/") && it.contains("u0") }
                        if (validLine != null) foundString = parseSimple(validLine)
                    } catch (e: Exception) {}

                    // CARA 2: Fallback
                    if (foundString == null) {
                        try {
                            val out = Shell.cmd("dumpsys activity activities | grep mResumedActivity").exec().out
                            val validLine = out.firstOrNull { it.contains("/") }
                            if (validLine != null) foundString = parseSimple(validLine)
                        } catch (e: Exception) {}
                    }

                    // UPDATE LOG LIST
                    if (foundString != null && foundString != lastLoggedRaw) {
                        lastLoggedRaw = foundString
                        val split = foundString.split("/")
                        if (split.size >= 2) {
                            val pkg = split[0]
                            var cls = split[1]
                            if (cls.startsWith(".")) cls = "$pkg$cls"
                            
                            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            val timeStr = timeFormat.format(Date())

                            val newLog = ActivityLog(pkg, cls, timeStr)
                            
                            // Tambah ke paling atas (Index 0)
                            withContext(Dispatchers.Main) {
                                activityLogs.add(0, newLog)
                                // Batasi history max 50 item biar gak berat
                                if (activityLogs.size > 50) activityLogs.removeRange(50, activityLogs.size)
                                // Scroll ke atas otomatis jika user di posisi atas
                                if (listState.firstVisibleItemIndex < 2) {
                                    listState.scrollToItem(0)
                                }
                            }
                        }
                    }
                }
                delay(800) 
            }
        }
    }

    // --- UI DESIGN ---
    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF11111B).copy(alpha = 0.95f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF45475A)),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // --- HEADER BAR ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFF1E1E2E))
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.x, dragAmount.y)
                                }
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Layers, null, tint = Color(0xFF89B4FA), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Activity Logger",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCDD6F4),
                            modifier = Modifier.weight(1f)
                        )

                        // Button Group
                        IconButton(onClick = { showSettings = !showSettings }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Settings, null, tint = if(showSettings) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { activityLogs.clear() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DeleteSweep, null, tint = Color(0xFFF38BA8), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onClose() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color(0xFFF38BA8), modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    HorizontalDivider(color = Color(0xFF313244))

                    // --- SETTINGS PANEL (EXPANDABLE) ---
                    AnimatedVisibility(
                        visible = showSettings,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF181825))
                                .padding(8.dp)
                        ) {
                            Text("Preferences", style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                FilterChipCompact("Icons", showIcons) { showIcons = !showIcons }
                                FilterChipCompact("Time", showTime) { showTime = !showTime }
                                FilterChipCompact("Full Info", showFullClass) { showFullClass = !showFullClass }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { isPaused = !isPaused },
                                colors = ButtonDefaults.buttonColors(containerColor = if(isPaused) Color(0xFFA6E3A1) else Color(0xFFFAB387), contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(if(isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if(isPaused) "Resume Monitoring" else "Pause Monitoring", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- CONTENT LIST ---
                    Box(modifier = Modifier.weight(1f)) {
                        if (activityLogs.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Scanning Activities...", color = Color.Gray, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                itemsIndexed(activityLogs) { index, log ->
                                    val isTop = index == 0
                                    ActivityLogItem(
                                        log = log,
                                        isTop = isTop,
                                        showIcon = showIcons,
                                        showTime = showTime,
                                        showFullClass = showFullClass,
                                        context = context
                                    )
                                    if (isTop) HorizontalDivider(color = Color(0xFF89B4FA), thickness = 2.dp)
                                    else HorizontalDivider(color = Color(0xFF313244), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }

            // --- RESIZE HANDLE ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .offset(x = 6.dp, y = 6.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(dragAmount.x, dragAmount.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragIndicator, 
                    contentDescription = "Resize",
                    tint = Color(0xFF89B4FA),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChipCompact(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFF89B4FA) else Color(0xFF313244))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, color = if(selected) Color.Black else Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActivityLogItem(
    log: ActivityLog,
    isTop: Boolean,
    showIcon: Boolean,
    showTime: Boolean,
    showFullClass: Boolean,
    context: Context
) {
    // Load Icon on Demand (Optimasi: Simpel pakai produceState)
    val appIcon by produceState<Drawable?>(initialValue = null, key1 = log.pkg) {
        if (showIcon) {
            withContext(Dispatchers.IO) {
                try {
                    value = context.packageManager.getApplicationIcon(log.pkg)
                } catch (e: Exception) { null }
            }
        }
    }

    val backgroundColor = if (isTop) Color(0xFF1E1E2E) else Color.Transparent
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Icon Section
        if (showIcon) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon!!.toBitmap().asImageBitmap(), // [FIX] Now uses KTX correctly
                    contentDescription = null,
                    modifier = Modifier.size(if(isTop) 36.dp else 28.dp)
                )
            } else {
                Box(Modifier.size(if(isTop) 36.dp else 28.dp).clip(CircleShape).background(Color.Gray))
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        // 2. Text Info
        Column(modifier = Modifier.weight(1f)) {
            // Package Name
            Text(
                text = log.pkg,
                style = MaterialTheme.typography.labelSmall,
                color = if(isTop) Color(0xFFA6E3A1) else Color.Gray,
                fontWeight = if(isTop) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Class Name
            val displayClass = if (showFullClass) log.cls else log.cls.substringAfterLast('.')
            Text(
                text = displayClass,
                style = MaterialTheme.typography.bodySmall,
                color = if(isTop) Color.White else Color(0xFFCDD6F4),
                fontSize = if(isTop) 13.sp else 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = if(showFullClass) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 3. Time & Action
        Column(horizontalAlignment = Alignment.End) {
            if (showTime) {
                Text(log.time, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(4.dp))
            // Quick Copy
            Icon(
                Icons.Default.ContentCopy, 
                null, 
                tint = Color(0xFF45475A), 
                modifier = Modifier.size(14.dp).clickable {
                    val clip = ClipData.newPlainText("Activity", "${log.pkg}/${log.cls}")
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// Logic Parsing Brute Force (Sama seperti sebelumnya karena sudah fix)
fun parseSimple(line: String): String? {
    try {
        val slashIndex = line.indexOf("/")
        if (slashIndex == -1) return null
        
        var startIndex = slashIndex - 1
        while (startIndex >= 0 && line[startIndex] != ' ') { startIndex-- }
        val packageName = line.substring(startIndex + 1, slashIndex)
        
        var endIndex = slashIndex + 1
        while (endIndex < line.length && line[endIndex] != ' ' && line[endIndex] != '}') { endIndex++ }
        val className = line.substring(slashIndex + 1, endIndex)
        
        return "$packageName/$className"
    } catch (e: Exception) { return null }
}
