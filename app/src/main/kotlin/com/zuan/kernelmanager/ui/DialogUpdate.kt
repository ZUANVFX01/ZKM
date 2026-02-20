/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.update

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.* 
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.zuan.kernelmanager.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    hazeState: HazeState,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    
    // State untuk pilihan download mode (default: otomatis)
    var isAutoDownload by remember { mutableStateOf(true) }

    // Animasi roket
    val infiniteTransition = rememberInfiniteTransition(label = "rocket_anim")
    val rocketOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Dialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 25.dp,
                        tint = HazeTint(Color.Black.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)),
                exit = scaleOut()
            ) {
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.padding(20.dp)
                ) {
                    
                    // ROCKET
                    Box(
                        modifier = Modifier
                            .zIndex(2f)
                            .offset(y = (-50).dp + rocketOffsetY.dp)
                            .size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawBetterRocket()
                        }
                    }

                    // CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            
                            // HEADER
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF50F2C8),
                                                    Color(0xFF26E3C2)
                                                )
                                            )
                                        )
                                )

                                Canvas(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(60.dp)
                                ) {
                                    drawClouds(color = Color.White)
                                }
                            }

                            // CONTENT
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                
                                Text(
                                    text = stringResource(R.string.update_title, updateInfo.version),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = Color(0xFF333333)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // CHANGELOG BOX (Scrollable)
                                val scrollState = rememberScrollState()
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp)
                                        .background(
                                            color = Color(0xFFF5F5F5),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.verticalScroll(scrollState)
                                    ) {
                                        val formattedChangelog = updateInfo.changelog
                                            .replace("# ", "")
                                            .replace("## ", "")
                                            .replace("- ", "• ")
                                            .replace("* ", "• ")
                                        
                                        Text(
                                            text = formattedChangelog,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 20.sp,
                                                color = Color(0xFF666666)
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // --- DOWNLOAD MODE SELECTION ---
                                if (!isDownloading) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFF0FDF9)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.auto_download),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = Color(0xFF0F766E)
                                                )
                                                Text(
                                                    text = stringResource(R.string.auto_download_desc),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF5F9EA0)
                                                )
                                            }
                                            
                                            Switch(
                                                checked = isAutoDownload,
                                                onCheckedChange = { isAutoDownload = it },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = Color(0xFF26E3C2),
                                                    checkedTrackColor = Color(0xFF26E3C2).copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // --- ACTION BUTTONS ---
                                if (isDownloading) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color(0xFF26E3C2))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.downloading_update),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // CANCEL
                                        OutlinedButton(
                                            onClick = onDismiss,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp),
                                            shape = RoundedCornerShape(50),
                                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                                width = 1.dp,
                                                brush = SolidColor(Color(0xFFEEEEEE))
                                            ),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = Color(0xFF999999)
                                            )
                                        ) {
                                            Text(
                                                stringResource(R.string.btn_cancel),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp
                                            )
                                        }

                                        // UPDATE / OPEN BROWSER
                                        Button(
                                            onClick = {
                                                if (isAutoDownload) {
                                                    // Mode Otomatis: Download APK
                                                    isDownloading = true
                                                    scope.launch {
                                                        onUpdateClick()
                                                        delay(500)
                                                    }
                                                } else {
                                                    // Mode Manual: Buka browser ke GitHub
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        data = Uri.parse(updateInfo.downloadUrl)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp),
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isAutoDownload) 
                                                    Color(0xFF26E3C2) else Color(0xFF0EA5E9)
                                            ),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 6.dp,
                                                pressedElevation = 2.dp
                                            )
                                        ) {
                                            if (isAutoDownload) {
                                                Text(
                                                    stringResource(R.string.btn_update),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = Color.White
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.OpenInBrowser,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        stringResource(R.string.btn_manual_download),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Cloud & Rocket drawing functions tetap sama...
private fun DrawScope.drawClouds(color: Color) {
    val width = size.width
    val height = size.height
    
    val path = Path().apply {
        moveTo(0f, height)
        lineTo(0f, height * 0.5f)
        
        cubicTo(
            width * 0.1f, height * 0.1f,
            width * 0.3f, height * 0.8f,
            width * 0.4f, height * 0.4f
        )
        cubicTo(
            width * 0.5f, height * 0.0f,
            width * 0.6f, height * 0.6f, 
            width * 0.7f, height * 0.4f
        )
        cubicTo(
            width * 0.85f, height * 0.1f, 
            width * 0.95f, height * 0.6f, 
            width, height * 0.5f
        )
        
        lineTo(width, height)
        close()
    }

    drawPath(path = path, color = color)
    
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = height * 0.4f,
        center = Offset(width * 0.2f, height * 0.6f)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = height * 0.5f,
        center = Offset(width * 0.8f, height * 0.7f)
    )
}

private fun DrawScope.drawBetterRocket() {
    val w = size.width
    val h = size.height
    
    drawCircle(
        color = Color(0xFFB2F5EA),
        radius = w * 0.25f,
        center = Offset(w * 0.5f, h * 0.85f)
    )
    
    val wingPath = Path().apply {
        moveTo(w * 0.25f, h * 0.6f)
        lineTo(w * 0.15f, h * 0.8f)
        lineTo(w * 0.35f, h * 0.7f)
        close()
    }
    drawPath(wingPath, Color.White)
    
    val wingRightPath = Path().apply {
        moveTo(w * 0.75f, h * 0.6f)
        lineTo(w * 0.85f, h * 0.8f)
        lineTo(w * 0.65f, h * 0.7f)
        close()
    }
    drawPath(wingRightPath, Color.White)
    
    drawOval(
        color = Color(0xFF26E3C2),
        topLeft = Offset(w * 0.3f, h * 0.1f),
        size = Size(w * 0.4f, h * 0.7f)
    )
    
    drawOval(
        color = Color.White.copy(alpha = 0.2f),
        topLeft = Offset(w * 0.35f, h * 0.15f),
        size = Size(w * 0.15f, h * 0.5f)
    )

    drawCircle(
        color = Color.White,
        radius = w * 0.12f,
        center = Offset(w * 0.5f, h * 0.35f)
    )
    drawCircle(
        color = Color(0xFF80DEEA),
        radius = w * 0.09f,
        center = Offset(w * 0.5f, h * 0.35f)
    )
}
