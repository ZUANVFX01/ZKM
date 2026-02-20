/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.terminal

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zuan.kernelmanager.R
import java.util.concurrent.TimeUnit

// --- THEME PALETTE ---
// private val C_Bg = Color(0xFF1E1E2E) // Tidak dipakai lagi di sini agar transparan
private val C_Pink = Color(0xFFF5C2E7)
private val C_Green = Color(0xFFA6E3A1)
private val C_Yellow = Color(0xFFF9E2AF)
private val C_Blue = Color(0xFF89B4FA)
private val C_Lavender = Color(0xFFB4BEFE)
private val C_TextMain = Color(0xFFCDD6F4)
private val C_TextDim = Color(0xFFA6ADC8)
private val C_Divider = Color(0xFF45475A)

@Composable
fun FastFetchHeader(scale: Float) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // --- REALTIME DATA ---
    val memInfo = remember {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(info)
        info
    }
    
    val totalRam = Formatter.formatShortFileSize(context, memInfo.totalMem)
    val availRam = memInfo.availMem
    val usedRamCalc = memInfo.totalMem - availRam
    val usedRam = Formatter.formatShortFileSize(context, usedRamCalc)
    val ramPercentage = ((usedRamCalc.toDouble() / memInfo.totalMem.toDouble()) * 100).toInt()
    
    val uptimeMillis = SystemClock.elapsedRealtime()
    val uptimeHours = TimeUnit.MILLISECONDS.toHours(uptimeMillis)
    val uptimeMinutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
    val uptimeStr = "${uptimeHours}h ${uptimeMinutes}m"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // .background(C_Bg) <--- DIHAPUS: Agar background mengikuti parent (TerminalScreen)
            // Jadi kalau parent transparan/glass, header ini ikutan transparan.
            .padding(
                top = 16.dp * scale, 
                bottom = 16.dp * scale, 
                start = 16.dp, 
                end = 16.dp
            )
    ) {
        // 1. COMMAND PROMPT
        Text(
            text = "> fastfetch",
            color = C_Green,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp * scale, 
            modifier = Modifier.padding(bottom = 8.dp * scale)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top 
        ) {
            // --- 2. WAIFU IMAGE ---
            val imageWeight = if (isLandscape) 0.2f else 0.35f
            val textWeight = 1f - imageWeight

            Box(
                modifier = Modifier
                    .weight(imageWeight) 
                    .padding(end = 12.dp * scale)
            ) {
                // Pastikan gambar waifu ada di drawable
                Image(
                    painter = painterResource(id = R.drawable.waifu),
                    contentDescription = "Waifu Art",
                    contentScale = ContentScale.FillWidth, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp * scale))
                )
            }

            // --- 3. INFO BLOCKS ---
            Column(
                modifier = Modifier.weight(textWeight)
            ) {
                BracketLine(true, scale)
                
                // === SOFTWARE ===
                FetchItem(Icons.Default.Settings, "OS", "Android ${Build.VERSION.RELEASE}", C_Pink, scale)
                FetchItem(Icons.Rounded.Memory, "Kernel", System.getProperty("os.version")?.take(15) ?: "Linux", C_Pink, scale)
                FetchItem(Icons.Outlined.Widgets, "Pkgs", "1337 (dpkg)", C_Green, scale)
                FetchItem(Icons.Default.Smartphone, "Display", "${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels}", C_Blue, scale)
                FetchItem(Icons.Rounded.Terminal, "Shell", "ZuanShell v2.5", C_Yellow, scale)
                
                Spacer(modifier = Modifier.height(6.dp * scale))
                BracketLine(false, scale)
                Spacer(modifier = Modifier.height(6.dp * scale))
                
                // Header User
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Person, null, tint = C_TextMain, modifier = Modifier.size(12.dp * scale))
                    Spacer(modifier = Modifier.width(4.dp * scale))
                    Text("zuan @ ${Build.MODEL.take(10).trim()}", color = C_TextMain, fontSize = 11.sp * scale, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(6.dp * scale))
                BracketLine(true, scale)

                // === HARDWARE ===
                FetchItem(Icons.Default.DeveloperBoard, "SoC", Build.HARDWARE.uppercase(), C_Green, scale)
                FetchItem(Icons.Outlined.Memory, "GPU", "Adreno/Mali", C_Green, scale) 
                FetchItem(Icons.Outlined.SdStorage, "RAM", "$usedRam / $totalRam ($ramPercentage%)", C_Lavender, scale)
                FetchItem(Icons.Default.Timer, "Uptime", uptimeStr, C_Pink, scale)
                
                BracketLine(false, scale)

                // === PALETTE ===
                Spacer(modifier = Modifier.height(8.dp * scale))
                ColorDots(scale)
            }
        }
    }
}

// --- VISUAL ELEMENTS ---

@Composable
fun BracketLine(isTop: Boolean, scale: Float) {
    Row(verticalAlignment = if (isTop) Alignment.Top else Alignment.Bottom) {
        Box(modifier = Modifier.width(1.dp).height(4.dp * scale).background(C_Divider))
        Box(modifier = Modifier.weight(1f).height(1.dp).background(C_Divider))
        Box(modifier = Modifier.width(1.dp).height(4.dp * scale).background(C_Divider))
    }
}

@Composable
fun FetchItem(icon: ImageVector, label: String, value: String, accentColor: Color, scale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (0.5).dp * scale), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(10.dp * scale)
        )
        Spacer(modifier = Modifier.width(6.dp * scale))
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                    append(label)
                }
                withStyle(SpanStyle(color = C_TextDim)) {
                    append(" : ")
                }
                withStyle(SpanStyle(color = C_TextMain)) {
                    append(value)
                }
            },
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp * scale,
            lineHeight = 12.sp * scale
        )
    }
}

@Composable
fun ColorDots(scale: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp * scale)) {
        listOf(
            Color(0xFF45475A), Color(0xFFF38BA8), Color(0xFFA6E3A1), 
            Color(0xFFF9E2AF), Color(0xFF89B4FA), Color(0xFFF5C2E7)
        ).forEach { color ->
            Box(modifier = Modifier.size(8.dp * scale).clip(CircleShape).background(color))
        }
    }
}
