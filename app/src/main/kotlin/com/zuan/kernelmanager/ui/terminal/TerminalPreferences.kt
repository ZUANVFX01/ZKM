/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlin.math.roundToInt
import com.zuan.kernelmanager.R

@Composable
fun TerminalPreferences(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    isBlurEnabled: Boolean, onBlurChanged: (Boolean) -> Unit,
    glassBlurRadius: Float, onGlassRadiusChanged: (Float) -> Unit,
    isDarkGlass: Boolean, onDarkGlassChanged: (Boolean) -> Unit,
    
    isBgCustomEnabled: Boolean, onBgCustomChanged: (Boolean) -> Unit,
    bgImageBlurRadius: Float, onBgImageBlurChanged: (Float) -> Unit,
    bgImageDimAlpha: Float, onBgImageDimChanged: (Float) -> Unit,
    
    isBubbleBgEnabled: Boolean, onBubbleBgChanged: (Boolean) -> Unit,
    isBgDarkMode: Boolean, onBgDarkModeChanged: (Boolean) -> Unit,
    isLiveGradientEnabled: Boolean, onLiveGradientChanged: (Boolean) -> Unit,
    
    isStatusBarHidden: Boolean, onFullscreenChanged: (Boolean) -> Unit,
    galleryLauncher: ManagedActivityResultLauncher<String, Uri?>,

    isBgVideoEnabled: Boolean, onBgVideoChanged: (Boolean) -> Unit,
    videoLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    val scrollState = rememberScrollState()

    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isMinimized by remember { mutableStateOf(false) }
    var isWindowLocked by remember { mutableStateOf(false) }
    var isFloatingServiceEnabled by remember { mutableStateOf(FloatingTerminalService.isRunning) }
    
    var windowWidth by remember { mutableStateOf(300.dp) }
    var windowHeight by remember { mutableStateOf(500.dp) }
    
    LaunchedEffect(configuration.orientation, isVisible) {
        if (isVisible) {
            if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                windowWidth = 400.dp 
                windowHeight = screenHeight * 0.85f 
            } else {
                windowWidth = 300.dp
                windowHeight = 500.dp
            }
            offsetX = (screenWidth.value / 2 - windowWidth.value / 2) * density.density
            offsetY = (screenHeight.value / 2 - windowHeight.value / 2) * density.density
        }
        isFloatingServiceEnabled = FloatingTerminalService.isRunning
    }

    val prefGlassTint = if (isDarkGlass) Color(0xFF0F0F16).copy(alpha = 0.2f) else Color(0xFFFFFFFF).copy(alpha = 0.15f)
    val prefBorderTint = if (isDarkGlass) Color(0xFFFFFFFF).copy(alpha = 0.15f) else Color(0xFFFFFFFF).copy(alpha = 0.40f)
    
    val prefGlassModifier = if (isBlurEnabled) {
        Modifier.hazeEffect(state = hazeState, style = HazeMaterials.thin().copy(blurRadius = glassBlurRadius.dp)).background(prefGlassTint)
    } else {
        Modifier.background(Color(0xFF181825).copy(alpha = 0.95f))
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(windowWidth, if (isMinimized) 50.dp else windowHeight)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(12.dp), spotColor = Color.Black)
                    .clip(RoundedCornerShape(12.dp))
                    .then(prefGlassModifier) 
                    .border(1.dp, prefBorderTint, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                            .background(Color(0xFF11111B).copy(alpha = if(isDarkGlass) 0.5f else 0.1f))
                            .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); if (!isWindowLocked) { offsetX += dragAmount.x; offsetY += dragAmount.y } } }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFFF5F56)).clickable { onClose() }) {
                             Icon(Icons.Default.Close, stringResource(R.string.action_close), tint = Color.Black.copy(alpha=0.5f), modifier = Modifier.size(8.dp).align(Alignment.Center))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFFFBD2E)).clickable { isMinimized = !isMinimized })
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF27C93F).copy(alpha = 0.3f)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.terminal_preferences_title), color = if(isDarkGlass) Color(0xFFA6ADC8) else Color(0xFF4C4F69), fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { isWindowLocked = !isWindowLocked }, modifier = Modifier.size(24.dp)) {
                            Icon(if (isWindowLocked) Icons.Filled.Lock else Icons.Outlined.Lock, stringResource(R.string.action_lock), tint = if (isWindowLocked) Color(0xFFFF5F56) else if (isDarkGlass) Color(0xFFA6ADC8) else Color(0xFF4C4F69), modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = { isMinimized = !isMinimized }, modifier = Modifier.size(24.dp)) {
                            Icon(if (isMinimized) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, stringResource(R.string.action_resize), tint = if (isDarkGlass) Color(0xFFA6ADC8) else Color(0xFF4C4F69), modifier = Modifier.size(16.dp))
                        }
                    }

                    if (!isMinimized) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(vertical = 8.dp)) {
                                 
                                 MenuToggleItem(Icons.Outlined.BlurOn, stringResource(R.string.terminal_glass_mode), isBlurEnabled, onBlurChanged)
                                 
                                 if (isBlurEnabled) {
                                     Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF11111B).copy(alpha = 0.5f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                         Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onDarkGlassChanged(!isDarkGlass) }) {
                                             Icon(if (isDarkGlass) Icons.Filled.Brightness2 else Icons.Filled.Brightness7, null, tint = Color(0xFFA6ADC8), modifier = Modifier.size(16.dp))
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(if (isDarkGlass) stringResource(R.string.terminal_theme_dark_glass) else stringResource(R.string.terminal_theme_light_glass), color = Color(0xFFCDD6F4), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                             Switch(checked = isDarkGlass, onCheckedChange = onDarkGlassChanged, modifier = Modifier.scale(0.6f), colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E1E2E), checkedTrackColor = Color(0xFF89B4FA), uncheckedThumbColor = Color(0xFFA6ADC8), uncheckedTrackColor = Color(0xFF313244)))
                                         }
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Text(stringResource(R.string.terminal_glass_intensity, glassBlurRadius.toInt()), color = Color(0xFFA6ADC8), fontSize = 10.sp)
                                         Slider(value = glassBlurRadius, onValueChange = onGlassRadiusChanged, valueRange = 0f..60f, colors = SliderDefaults.colors(thumbColor = Color(0xFF89B4FA), activeTrackColor = Color(0xFF89B4FA), inactiveTrackColor = Color(0xFF313244)))
                                     }
                                 }

                                 Divider(color = Color(0xFF313244), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                 
                                 Text(stringResource(R.string.terminal_background_section), color = Color(0xFF585B70), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))

                                 MenuToggleItem(Icons.Outlined.Movie, stringResource(R.string.terminal_live_video_wall), isBgVideoEnabled) { 
                                     onBgVideoChanged(it)
                                     if(it) {
                                         onBgCustomChanged(false)
                                         onBubbleBgChanged(false)
                                         videoLauncher.launch("video/*")
                                     }
                                 }

                                 if (isBgVideoEnabled) {
                                     Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF11111B).copy(alpha=0.5f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                         Row(
                                             modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF313244)).clickable { videoLauncher.launch("video/*") }.padding(8.dp),
                                             verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                                         ) {
                                             Icon(Icons.Outlined.Movie, null, tint = Color(0xFFF9E2AF), modifier = Modifier.size(16.dp))
                                             Spacer(modifier = Modifier.width(8.dp))
                                             Text(stringResource(R.string.terminal_change_video), color = Color(0xFFCDD6F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                         }
                                         Spacer(modifier = Modifier.height(8.dp))
                                         Text(stringResource(R.string.terminal_video_dim, (bgImageDimAlpha * 100).toInt()), color = Color(0xFFA6ADC8), fontSize = 10.sp)
                                         Slider(value = bgImageDimAlpha, onValueChange = onBgImageDimChanged, valueRange = 0f..0.9f, colors = SliderDefaults.colors(thumbColor = Color(0xFFF9E2AF), activeTrackColor = Color(0xFFF9E2AF), inactiveTrackColor = Color(0xFF313244)))
                                     }
                                 }

                                 if (!isBgVideoEnabled) {
                                     MenuToggleItem(Icons.Outlined.Image, stringResource(R.string.terminal_custom_image), isBgCustomEnabled) { 
                                         onBgCustomChanged(it)
                                         if(it) {
                                            onBubbleBgChanged(false)
                                            galleryLauncher.launch("image/*")
                                         }
                                     }
                                     
                                     if (isBgCustomEnabled) {
                                         Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF11111B).copy(alpha=0.5f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                             Row(
                                                 modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFF313244)).clickable { galleryLauncher.launch("image/*") }.padding(8.dp),
                                                 verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                                             ) {
                                                 Icon(Icons.Outlined.Image, null, tint = Color(0xFF89B4FA), modifier = Modifier.size(16.dp))
                                                 Spacer(modifier = Modifier.width(8.dp))
                                                 Text(stringResource(R.string.terminal_select_image), color = Color(0xFFCDD6F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                             }
                                             Spacer(modifier = Modifier.height(12.dp))
                                             Text(stringResource(R.string.terminal_image_blur, bgImageBlurRadius.toInt()), color = Color(0xFFA6ADC8), fontSize = 10.sp)
                                             Slider(value = bgImageBlurRadius, onValueChange = onBgImageBlurChanged, valueRange = 0f..20f, colors = SliderDefaults.colors(thumbColor = Color(0xFFA6E3A1), activeTrackColor = Color(0xFFA6E3A1), inactiveTrackColor = Color(0xFF313244)))
                                             Text(stringResource(R.string.terminal_image_dim, (bgImageDimAlpha * 100).toInt()), color = Color(0xFFA6ADC8), fontSize = 10.sp)
                                             Slider(value = bgImageDimAlpha, onValueChange = onBgImageDimChanged, valueRange = 0f..0.9f, colors = SliderDefaults.colors(thumbColor = Color(0xFFF9E2AF), activeTrackColor = Color(0xFFF9E2AF), inactiveTrackColor = Color(0xFF313244)))
                                         }
                                     }
                                     
                                     if (!isBgCustomEnabled) {
                                         MenuToggleItem(Icons.Outlined.BubbleChart, stringResource(R.string.terminal_bubble_gradient), isBubbleBgEnabled, onBubbleBgChanged)
                                         Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF11111B).copy(alpha=0.5f)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                             if (isBubbleBgEnabled) {
                                                 Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onBgDarkModeChanged(!isBgDarkMode) }) {
                                                    Icon(if (isBgDarkMode) Icons.Filled.Brightness2 else Icons.Filled.Brightness7, null, tint = Color(0xFFA6ADC8), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (isBgDarkMode) stringResource(R.string.terminal_bg_dark_mode) else stringResource(R.string.terminal_bg_light_mode), color = Color(0xFFCDD6F4), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                                    Switch(checked = isBgDarkMode, onCheckedChange = onBgDarkModeChanged, modifier = Modifier.scale(0.6f), colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1E1E2E), checkedTrackColor = Color(0xFFCBA6F7), uncheckedThumbColor = Color(0xFFA6ADC8), uncheckedTrackColor = Color(0xFF313244)))
                                                 }
                                             } else {
                                                 MenuToggleItem(Icons.Outlined.Gradient, stringResource(R.string.terminal_live_gradient), isLiveGradientEnabled, onLiveGradientChanged)
                                             }
                                         }
                                     }
                                 }
                                 
                                 Divider(color = Color(0xFF313244), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                 MenuToggleItem(Icons.Outlined.Fullscreen, stringResource(R.string.terminal_fullscreen), isStatusBarHidden, onFullscreenChanged)
                                 
                                 Divider(color = Color(0xFF313244), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                                 val canDrawOverlays = Settings.canDrawOverlays(context)
                                 
                                 MenuToggleItem(
                                     icon = Icons.Outlined.PictureInPicture,
                                     label = stringResource(R.string.terminal_floating_mode),
                                     isChecked = isFloatingServiceEnabled
                                 ) { shouldEnable ->
                                     if (shouldEnable) {
                                         if (!canDrawOverlays) {
                                             val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                             intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                             context.startActivity(intent)
                                         } else {
                                             val intent = Intent(context, FloatingTerminalService::class.java)
                                             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                 context.startForegroundService(intent)
                                             } else {
                                                 context.startService(intent)
                                             }
                                             isFloatingServiceEnabled = true
                                             onClose() 
                                         }
                                     } else {
                                         context.stopService(Intent(context, FloatingTerminalService::class.java))
                                         isFloatingServiceEnabled = false
                                     }
                                 }
                            }

                            if (!isWindowLocked) {
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd).size(30.dp).background(Color.Transparent)
                                        .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); with(density) { val newWidth = windowWidth + dragAmount.x.toDp(); val newHeight = windowHeight + dragAmount.y.toDp(); windowWidth = newWidth.coerceIn(250.dp, screenWidth); windowHeight = newHeight.coerceIn(100.dp, screenHeight) } } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.DragHandle, stringResource(R.string.action_resize), tint = if (isDarkGlass) Color(0xFF565F89) else Color(0xFF4C4F69), modifier = Modifier.rotate(-45f).size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFFA6ADC8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color(0xFFCDD6F4), fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun MenuToggleItem(
    icon: ImageVector, 
    label: String, 
    isChecked: Boolean, 
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFFA6ADC8), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color(0xFFCDD6F4), fontSize = 13.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked, 
            onCheckedChange = onCheckedChange, 
            modifier = Modifier.scale(0.7f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1E1E2E), 
                checkedTrackColor = Color(0xFFA6E3A1), 
                uncheckedThumbColor = Color(0xFFA6ADC8), 
                uncheckedTrackColor = Color(0xFF313244)
            )
        )
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
