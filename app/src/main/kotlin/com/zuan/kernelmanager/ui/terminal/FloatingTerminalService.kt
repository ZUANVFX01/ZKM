/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zuan.kernelmanager.R

class FloatingTerminalService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        var isRunning by mutableStateOf(false)
    }

    private val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private var composeView: ComposeView? = null
    private lateinit var overlayParams: WindowManager.LayoutParams
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        isRunning = true
        startForeground(1337, createNotification())
        setupOverlay()
    }

    private fun setupOverlay() {
        overlayParams = WindowManager.LayoutParams(
            800,
            1000,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        overlayParams.gravity = Gravity.TOP or Gravity.START
        overlayParams.x = 100
        overlayParams.y = 100

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingTerminalService)
            setViewTreeViewModelStoreOwner(this@FloatingTerminalService)
            setViewTreeSavedStateRegistryOwner(this@FloatingTerminalService)

            setContent {
                var globalContentScale by remember { mutableStateOf(0.8f) }

                FloatingContent(
                    contentScale = globalContentScale,
                    onScaleChanged = { globalContentScale = it },
                    onClose = { stopSelf() },
                    onDrag = { x, y ->
                        overlayParams.x += x.toInt()
                        overlayParams.y += y.toInt()
                        windowManager.updateViewLayout(this, overlayParams)
                    },
                    onResize = { widthChange, heightChange ->
                        overlayParams.width += widthChange.toInt()
                        overlayParams.height += heightChange.toInt()
                        if (overlayParams.width < 300) overlayParams.width = 300
                        if (overlayParams.height < 300) overlayParams.height = 300
                        windowManager.updateViewLayout(this, overlayParams)
                    },
                    onFocusRequest = { isFocused ->
                        if (isFocused) {
                            overlayParams.flags = overlayParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                        } else {
                            overlayParams.flags = overlayParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        }
                        windowManager.updateViewLayout(this, overlayParams)
                    }
                )
            }
        }

        windowManager.addView(composeView, overlayParams)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (composeView != null) windowManager.removeView(composeView)
        viewModelStore.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null

        private fun createNotification(): Notification {
        val channelId = "floating_term_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Floating Terminal", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return Notification.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))  // <-- GANTI INI
            .setContentText(getString(R.string.terminal_overlay_active))  // <-- GANTI INI
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }


    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
}

@Composable
fun FloatingContent(
    contentScale: Float,
    onScaleChanged: (Float) -> Unit,
    onClose: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    onFocusRequest: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val session = remember { TerminalManager.createSession(context) }
    
    var isKeyboardFocused by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E).copy(alpha = 0.95f)) 
            .border(1.dp, Color(0xFF45475A), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- TITLE BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF11111B))
                    .pointerInput(isLocked) {
                        if (!isLocked) {
                            detectDragGestures { _, dragAmount ->
                                onDrag(dragAmount.x, dragAmount.y)
                            }
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close Button
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFFF5F56)).clickable { onClose() }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = Color.Black.copy(0.5f), modifier = Modifier.size(8.dp).align(Alignment.Center))
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Title
                Text(
                    text = if(isLocked) stringResource(R.string.terminal_overlay_title_locked) else stringResource(R.string.terminal_overlay_title), 
                    color = if(isLocked) Color(0xFFF38BA8) else Color(0xFFA6ADC8), 
                    fontSize = 11.sp, 
                    fontFamily = FontFamily.Monospace, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.weight(1f)
                )

                // --- LOCK BUTTON ---
                IconButton(onClick = { isLocked = !isLocked }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if(isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = stringResource(R.string.action_lock),
                        tint = if(isLocked) Color(0xFFFF5F56) else Color(0xFF45475A),
                        modifier = Modifier.size(14.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))

                // --- KEYBOARD / FOCUS TOGGLE ---
                IconButton(
                    onClick = { 
                        isKeyboardFocused = !isKeyboardFocused
                        onFocusRequest(isKeyboardFocused)
                    }, 
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if(isKeyboardFocused) Icons.Filled.Keyboard else Icons.Outlined.Keyboard,
                        contentDescription = stringResource(R.string.action_input_keyboard),
                        tint = if(isKeyboardFocused) Color(0xFFA6E3A1) else Color(0xFF45475A),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // --- TERMINAL CONTENT ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable { 
                        if (!isKeyboardFocused) {
                            isKeyboardFocused = true
                            onFocusRequest(true)
                        }
                    }
            ) {
                TerminalSessionContent(
                    session = session,
                    contentScale = contentScale,
                    onScaleChanged = onScaleChanged
                )
            }
        }

        // --- RESIZE HANDLE ---
        if (!isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(30.dp)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onResize(dragAmount.x, dragAmount.y)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(R.string.action_resize),
                    tint = Color(0xFF565F89),
                    modifier = Modifier.rotate(-45f).size(18.dp)
                )
            }
        }
    }
}
