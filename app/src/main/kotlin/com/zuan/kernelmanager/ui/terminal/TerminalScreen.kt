/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.termux.terminal.TerminalSession
import com.zuan.kernelmanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun TerminalScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("terminal_settings", Context.MODE_PRIVATE) }
    
    val session = remember { TerminalManager.createSession(context) }
    val scope = rememberCoroutineScope()

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    var windowWidth by remember { mutableStateOf(screenWidth * 0.9f) }
    var windowHeight by remember { mutableStateOf(screenHeight * 0.6f) }
    
    var isLocked by remember { mutableStateOf(false) }
    var isMaximized by remember { mutableStateOf(false) }
    var contentScale by remember { mutableStateOf(1f) }
    var showPreferences by remember { mutableStateOf(false) }

    var isBlurEnabled by remember { mutableStateOf(prefs.getBoolean("blur_enabled", true)) }
    var glassBlurRadius by remember { mutableStateOf(prefs.getFloat("glass_radius", 20f)) }
    var isDarkGlass by remember { mutableStateOf(prefs.getBoolean("dark_glass", true)) } 
    var isStatusBarHidden by remember { mutableStateOf(prefs.getBoolean("fullscreen", false)) }
    
    var isBgCustomEnabled by remember { mutableStateOf(prefs.getBoolean("bg_custom", false)) }
    var bgImageBlurRadius by remember { mutableStateOf(prefs.getFloat("bg_img_radius", 0f)) }
    var bgImageDimAlpha by remember { mutableStateOf(prefs.getFloat("bg_img_dim", 0.3f)) }
    var isBubbleBgEnabled by remember { mutableStateOf(prefs.getBoolean("bg_bubble_enabled", true)) }
    var isBgDarkMode by remember { mutableStateOf(prefs.getBoolean("bg_dark_mode", true)) }
    var isLiveGradientEnabled by remember { mutableStateOf(prefs.getBoolean("bg_live_gradient", false)) }
    
    var isBgVideoEnabled by remember { mutableStateOf(prefs.getBoolean("bg_video_enabled", false)) }
    var videoUriString by remember { mutableStateOf(prefs.getString("bg_video_uri", null)) }

    var backgroundBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(isBlurEnabled) { prefs.edit().putBoolean("blur_enabled", isBlurEnabled).apply() }
    LaunchedEffect(glassBlurRadius) { prefs.edit().putFloat("glass_radius", glassBlurRadius).apply() }
    LaunchedEffect(isDarkGlass) { prefs.edit().putBoolean("dark_glass", isDarkGlass).apply() }
    LaunchedEffect(isStatusBarHidden) { prefs.edit().putBoolean("fullscreen", isStatusBarHidden).apply() }
    LaunchedEffect(isBgCustomEnabled) { prefs.edit().putBoolean("bg_custom", isBgCustomEnabled).apply() }
    LaunchedEffect(bgImageBlurRadius) { prefs.edit().putFloat("bg_img_radius", bgImageBlurRadius).apply() }
    LaunchedEffect(bgImageDimAlpha) { prefs.edit().putFloat("bg_img_dim", bgImageDimAlpha).apply() }
    LaunchedEffect(isBubbleBgEnabled) { prefs.edit().putBoolean("bg_bubble_enabled", isBubbleBgEnabled).apply() }
    LaunchedEffect(isBgDarkMode) { prefs.edit().putBoolean("bg_dark_mode", isBgDarkMode).apply() }
    LaunchedEffect(isLiveGradientEnabled) { prefs.edit().putBoolean("bg_live_gradient", isLiveGradientEnabled).apply() }
    LaunchedEffect(isBgVideoEnabled) { prefs.edit().putBoolean("bg_video_enabled", isBgVideoEnabled).apply() }
    LaunchedEffect(videoUriString) { prefs.edit().putString("bg_video_uri", videoUriString).apply() }

    val hazeState = rememberHazeState()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "saved_background.jpg")
            if (file.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    withContext(Dispatchers.Main) {
                        backgroundBitmap = bitmap?.asImageBitmap()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isBgCustomEnabled = true
            isBgVideoEnabled = false
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.filesDir, "saved_background.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    withContext(Dispatchers.Main) {
                        backgroundBitmap = bitmap.asImageBitmap()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                
                videoUriString = uri.toString()
                isBgVideoEnabled = true
                isBgCustomEnabled = false
                isBubbleBgEnabled = false
            } catch (e: Exception) {
                e.printStackTrace()
                videoUriString = uri.toString()
                isBgVideoEnabled = true
            }
        }
    }

    LaunchedEffect(configuration.orientation) {
        offsetX = 0f
        offsetY = 0f
        if (configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            windowWidth = screenWidth * 0.7f  
            windowHeight = screenHeight * 0.85f 
        } else {
            windowWidth = screenWidth * 0.9f
            windowHeight = screenHeight * 0.6f
        }
    }

    LaunchedEffect(isStatusBarHidden) {
        val window = (view.context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            if (isStatusBarHidden) {
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.statusBars())
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize()
                .then(if (isBlurEnabled) Modifier.hazeSource(state = hazeState) else Modifier)
        ) {
            if (isBgVideoEnabled && !videoUriString.isNullOrEmpty()) {
                VideoBackground(
                    videoUri = Uri.parse(videoUriString),
                    modifier = Modifier.fillMaxSize()
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgImageDimAlpha)))
            
            } else if (isBgCustomEnabled && backgroundBitmap != null) {
                Image(
                    bitmap = backgroundBitmap!!, contentDescription = stringResource(R.string.terminal_custom_bg_desc),
                    contentScale = ContentScale.Crop, 
                    modifier = Modifier.fillMaxSize().then(if (Build.VERSION.SDK_INT >= 31 && bgImageBlurRadius > 0f) Modifier.blur(bgImageBlurRadius.dp) else Modifier)
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgImageDimAlpha)))
            } else if (isBubbleBgEnabled) {
                AnimatedBubbleBackground(isDarkMode = isBgDarkMode)
            } else {
                if (isLiveGradientEnabled) AnimatedLinearGradientBackground() 
                else Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = listOf(Color(0xFF4158D0), Color(0xFFC850C0), Color(0xFFFFCC70)), start = Offset(0f, 2000f), end = Offset(1000f, 0f))))
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (isMaximized) Alignment.TopCenter else Alignment.Center
        ) {
            val windowModifier = if (isMaximized) {
                Modifier.fillMaxSize() 
            } else {
                Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(windowWidth, windowHeight)
            }
            
            val windowShape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp)
            val glassTint = if (isDarkGlass) Color(0xFF0F0F16).copy(alpha = 0.2f) else Color(0xFFFFFFFF).copy(alpha = 0.15f)
            val borderTint = if (isDarkGlass) Color(0xFFFFFFFF).copy(alpha = 0.15f) else Color(0xFFFFFFFF).copy(alpha = 0.40f)

            val glassModifier = if (isBlurEnabled) {
                Modifier.hazeEffect(state = hazeState, style = HazeMaterials.thin().copy(blurRadius = glassBlurRadius.dp)) {
                   if (Build.VERSION.SDK_INT == 31 || Build.VERSION.SDK_INT == 32) forceInvalidateOnPreDraw = true 
                }.background(glassTint) 
            } else Modifier.background(Color(0xFF1E1E2E))

            Column(
                modifier = windowModifier.then(if (isMaximized) Modifier.imePadding().navigationBarsPadding() else Modifier)
                    .shadow(elevation = 40.dp, shape = windowShape, spotColor = Color.Black)
                    .clip(windowShape).then(glassModifier).border(width = 1.dp, color = borderTint, shape = windowShape)
            ) {
                val currentHeaderHeight = if (isMaximized) 40.dp + statusBarHeight else 40.dp
                val currentContentPaddingTop = if (isMaximized) statusBarHeight else 0.dp

                Row(
                    modifier = Modifier.fillMaxWidth().height(currentHeaderHeight)
                        .background(Color(0xFF000000).copy(alpha = if (isDarkGlass) 0.2f else 0.05f))
                        .padding(top = currentContentPaddingTop, start = 16.dp, end = 16.dp)
                        .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); if (!isLocked && !isMaximized) { offsetX += dragAmount.x; offsetY += dragAmount.y } } },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrafficLightButton(Color(0xFFFF5F56))
                    Spacer(modifier = Modifier.width(8.dp))
                    TrafficLightButton(Color(0xFFFFBD2E))
                    Spacer(modifier = Modifier.width(8.dp))
                    TrafficLightButton(Color(0xFF27C93F))

                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = stringResource(R.string.terminal_title), color = if (isDarkGlass) Color(0xFFA9B1D6).copy(alpha = 0.9f) else Color(0xFF4C4F69), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { showPreferences = !showPreferences }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Settings, stringResource(R.string.action_settings), tint = if (isDarkGlass) Color(0xFFA9B1D6) else Color(0xFF4C4F69), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { isLocked = !isLocked }, modifier = Modifier.size(32.dp)) {
                        Icon(if (isLocked) Icons.Filled.Lock else Icons.Outlined.Lock, stringResource(R.string.action_lock), tint = if (isLocked) Color(0xFFFF5F56) else if (isDarkGlass) Color(0xFF7AA2F7) else Color(0xFF1E66F5), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { isMaximized = !isMaximized }, modifier = Modifier.size(32.dp)) {
                        Icon(if (isMaximized) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp, stringResource(R.string.action_resize), tint = if (isDarkGlass) Color(0xFFA9B1D6) else Color(0xFF4C4F69), modifier = Modifier.size(20.dp))
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    TerminalSessionContent(
                        session = session,
                        contentScale = contentScale,
                        onScaleChanged = { contentScale = it }
                    )

                    if (!isMaximized && !isLocked) {
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).background(Color.Transparent)
                                .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); with(density) { val newWidth = windowWidth + dragAmount.x.toDp(); val newHeight = windowHeight + dragAmount.y.toDp(); windowWidth = newWidth.coerceIn(200.dp, screenWidth); windowHeight = newHeight.coerceIn(200.dp, screenHeight) } } },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.DragHandle, stringResource(R.string.action_resize), tint = Color(0xFF565F89), modifier = Modifier.rotate(-45f).size(18.dp))
                        }
                    }
                }
            }
        }
        
        TerminalPreferences(
            isVisible = showPreferences,
            onClose = { showPreferences = false },
            hazeState = hazeState,
            isBlurEnabled = isBlurEnabled, onBlurChanged = { isBlurEnabled = it },
            glassBlurRadius = glassBlurRadius, onGlassRadiusChanged = { glassBlurRadius = it },
            isDarkGlass = isDarkGlass, onDarkGlassChanged = { isDarkGlass = it },
            isBgCustomEnabled = isBgCustomEnabled, onBgCustomChanged = { isBgCustomEnabled = it },
            bgImageBlurRadius = bgImageBlurRadius, onBgImageBlurChanged = { bgImageBlurRadius = it },
            bgImageDimAlpha = bgImageDimAlpha, onBgImageDimChanged = { bgImageDimAlpha = it },
            isBubbleBgEnabled = isBubbleBgEnabled, onBubbleBgChanged = { isBubbleBgEnabled = it },
            isBgDarkMode = isBgDarkMode, onBgDarkModeChanged = { isBgDarkMode = it },
            isLiveGradientEnabled = isLiveGradientEnabled, onLiveGradientChanged = { isLiveGradientEnabled = it },
            isStatusBarHidden = isStatusBarHidden, onFullscreenChanged = { isStatusBarHidden = it },
            galleryLauncher = galleryLauncher,
            isBgVideoEnabled = isBgVideoEnabled, onBgVideoChanged = { isBgVideoEnabled = it },
            videoLauncher = videoLauncher
        )
    }
}

@Composable
fun TerminalSessionContent(
    session: TerminalSession,
    contentScale: Float,
    onScaleChanged: (Float) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val currentScale by rememberUpdatedState(contentScale)
    val currentOnScaleChanged by rememberUpdatedState(onScaleChanged)

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newScale = (currentScale * zoom).coerceIn(0.5f, 3.0f)
                        currentOnScaleChanged(newScale)
                    }
                }
        ) {
            FastFetchHeader(scale = contentScale)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    com.termux.view.TerminalView(ctx, null).apply {
                        setTextSize((26 * currentScale).toInt()) 
                        keepScreenOn = true
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        isFocusable = true
                        isFocusableInTouchMode = true
                        val paddingPx = (16 * density.density).toInt()
                        setPadding(paddingPx, 0, paddingPx, paddingPx)
                        TerminalManager.uiUpdater = { post { invalidate() } }
                        
                        setTerminalViewClient(object : com.termux.view.TerminalViewClient {
                            override fun onScale(scale: Float): Float { 
                                val newScale = (currentScale * scale).coerceIn(0.5f, 3.0f)
                                currentOnScaleChanged(newScale)
                                return 1.0f 
                            }
                            override fun onSingleTapUp(e: android.view.MotionEvent?) {
                                requestFocus()
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                                imm.showSoftInput(this@apply, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                            }
                            override fun shouldEnforceCharBasedInput(): Boolean = true
                            override fun onKeyDown(keyCode: Int, e: android.view.KeyEvent?, session: TerminalSession?): Boolean { if (keyCode == android.view.KeyEvent.KEYCODE_ENTER) { session?.write("\r"); return true }; return false }
                            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession?): Boolean { if (session != null && Character.isValidCodePoint(codePoint)) { session.write(String(Character.toChars(codePoint))); return true }; return false }
                            override fun shouldBackButtonBeMappedToEscape(): Boolean = false
                            override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
                            override fun isTerminalViewSelected(): Boolean = true
                            override fun onKeyUp(keyCode: Int, e: android.view.KeyEvent?): Boolean = false
                            override fun onLongPress(event: android.view.MotionEvent?): Boolean = false
                            override fun readControlKey(): Boolean = false
                            override fun readAltKey(): Boolean = false
                            override fun readShiftKey(): Boolean = false
                            override fun readFnKey(): Boolean = false
                            override fun onEmulatorSet() {}
                            override fun copyModeChanged(isCopyMode: Boolean) {}
                            override fun logError(tag: String?, message: String?) {}
                            override fun logWarn(tag: String?, message: String?) {}
                            override fun logInfo(tag: String?, message: String?) {}
                            override fun logDebug(tag: String?, message: String?) {}
                            override fun logVerbose(tag: String?, message: String?) {}
                            override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
                            override fun logStackTrace(tag: String?, e: Exception?) {}
                        })
                        attachSession(session)
                    }
                },
                update = { view -> 
                    val newSize = (26 * currentScale).toInt()
                    view.setTextSize(newSize)
                }
            )
        }
    }
}

@Composable
fun TrafficLightButton(color: Color) {
    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
}

@Composable
fun AnimatedBubbleBackground(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_anim")

    val bubble1X by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 7000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "b1x"
    )
    val bubble1Y by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 5000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "b1y"
    )

    val bubble2X by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 6000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "b2x"
    )
    val bubble2Y by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 8000, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "b2y"
    )

    val bgColor = if (isDarkMode) Color(0xFF0F0F16) else Color(0xFFF2F4F8)
    val bubble1Color = Color(0xFFCBA6F7).copy(alpha = 0.6f) 
    val bubble2Color = Color(0xFFF5C2E7).copy(alpha = 0.6f) 

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().background(bgColor)) {
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(bubble1Color, Color.Transparent), center = Offset(size.width * bubble1X, size.height * bubble1Y), radius = size.minDimension * 0.6f),
            center = Offset(size.width * bubble1X, size.height * bubble1Y), radius = size.minDimension * 0.6f
        )
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(bubble2Color, Color.Transparent), center = Offset(size.width * bubble2X, size.height * bubble2Y), radius = size.minDimension * 0.5f),
            center = Offset(size.width * bubble2X, size.height * bubble2Y), radius = size.minDimension * 0.5f
        )
    }
}

@Composable
fun AnimatedLinearGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_anim")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 10000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "angle"
    )
    val colors = listOf(Color(0xFF4158D0), Color(0xFFC850C0), Color(0xFFFFCC70))

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val radians = Math.toRadians(angle.toDouble())
        val x = kotlin.math.cos(radians).toFloat()
        val y = kotlin.math.sin(radians).toFloat()
        val start = Offset(size.width / 2 + x * size.width, size.height / 2 + y * size.height)
        val end = Offset(size.width / 2 - x * size.width, size.height / 2 - y * size.height)
        drawRect(brush = Brush.linearGradient(colors = colors, start = start, end = end))
    }
}
