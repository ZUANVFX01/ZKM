/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.fpsmanager

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.data.FpsDataPoint
import com.zuan.kernelmanager.data.FpsDatabase
import com.zuan.kernelmanager.data.FpsSession
import com.zuan.kernelmanager.utils.DataExporter
import com.zuan.kernelmanager.utils.MonitorReader
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FpsManagerStatsContent(
    contentColor: Color, 
    subContentColor: Color,
    isGlassActive: Boolean = false,
    hazeState: HazeState,
    glassModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val sessionList by FpsDatabase.getDatabase(context).fpsDao().getAllSessions()
        .collectAsState(initial = emptyList())
        
    var expandedSessionId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = subContentColor)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.fps_session_history), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                    Text(stringResource(R.string.fps_records_count, sessionList.size), style = MaterialTheme.typography.bodyMedium, color = subContentColor)
                }
                Spacer(Modifier.weight(1f))
                if (sessionList.isNotEmpty()) {
                    IconButton(onClick = { scope.launch(Dispatchers.IO) { FpsDatabase.getDatabase(context).fpsDao().clearAll() } }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        items(sessionList, key = { it.id }) { session ->
            ProSessionCard(
                session = session,
                isExpanded = expandedSessionId == session.id,
                contentColor = contentColor,
                subContentColor = subContentColor,
                onExpand = { expandedSessionId = if (expandedSessionId == session.id) null else session.id },
                onDelete = { scope.launch(Dispatchers.IO) { FpsDatabase.getDatabase(context).fpsDao().deleteSession(session.id) } },
                isGlassActive = isGlassActive,
                glassModifier = glassModifier
            )
        }
    }
}

@Composable
fun ProSessionCard(
    session: FpsSession, 
    isExpanded: Boolean,
    contentColor: Color, 
    subContentColor: Color,
    onExpand: () -> Unit, 
    onDelete: () -> Unit,
    isGlassActive: Boolean = false,
    glassModifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dataPoints by remember { mutableStateOf<List<FpsDataPoint>>(emptyList()) }
    
    val appName = remember(session.packageName) { MonitorReader.getAppName(context, session.packageName) }
    val appIcon = remember(session.packageName) { MonitorReader.getAppIcon(context, session.packageName) }

    LaunchedEffect(isExpanded) {
        if (isExpanded && dataPoints.isEmpty()) {
            withContext(Dispatchers.IO) {
                dataPoints = FpsDatabase.getDatabase(context).fpsDao().getPointsForSession(session.id)
            }
        }
    }

    val cardContent: @Composable () -> Unit = {
        Column(Modifier.padding(16.dp)) {
             Row(verticalAlignment = Alignment.CenterVertically) {
                if (appIcon != null) {
                    AppIconView(drawable = appIcon, modifier = Modifier.size(48.dp))
                } else {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(contentColor.copy(alpha=0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Gamepad, null, tint = contentColor.copy(alpha=0.5f))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(appName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor, maxLines = 1)
                    val dateStr = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault()).format(Date(session.startTime))
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = subContentColor)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(getFpsColor(session.avgFps).copy(alpha=0.2f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("${session.avgFps.toInt()} FPS", color = getFpsColor(session.avgFps), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            if (isExpanded) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox(stringResource(R.string.fps_stat_max), "${session.maxFps.toInt()}", Color(0xFF2196F3))
                    StatBox(stringResource(R.string.fps_stat_min), "${session.minFps.toInt()}", Color(0xFFF44336))
                    StatBox(stringResource(R.string.fps_stat_avg), String.format("%.1f", session.avgFps), Color(0xFF4CAF50))
                    StatBox(stringResource(R.string.fps_stat_var), String.format("%.1f", session.variance), Color.Gray)
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBox(stringResource(R.string.fps_stat_stable), "${session.smoothness.toInt()}%", Color(0xFFFF9800))
                    StatBox(stringResource(R.string.fps_stat_max_temp), "${session.maxTemp}°", Color(0xFFFF5722))
                    StatBox(stringResource(R.string.fps_stat_avg_pwr), String.format("%.1fW", session.avgWatt), Color(0xFFE91E63))
                    StatBox(stringResource(R.string.fps_stat_ram), "${session.avgRam}M", Color(0xFF9C27B0))
                }

                Spacer(Modifier.height(24.dp))
                
                if (dataPoints.isNotEmpty()) {
                    Text(stringResource(R.string.fps_chart_fps_temp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subContentColor)
                    Box(Modifier.height(150.dp).fillMaxWidth()) {
                        ZkmLineChart(dataPoints.map { it.fps }, 144f, Color(0xFF4CAF50), fillColor = Color(0xFF4CAF50).copy(alpha=0.1f))
                        ZkmLineChart(dataPoints.map { it.temp }, 100f, Color(0xFFFF5722), strokeWidth = 2f)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.fps_chart_cpu), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = subContentColor)
                    Box(Modifier.height(100.dp).fillMaxWidth()) {
                        ZkmLineChart(dataPoints.map { it.cpuLoad.toFloat() }, 100f, Color(0xFF2196F3), fillColor = Color(0xFF2196F3).copy(alpha=0.1f))
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { 
                        if (dataPoints.isNotEmpty()) {
                            DataExporter.exportSessionToCsv(context, session, dataPoints)
                        }
                    }) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.fps_export_csv), color = MaterialTheme.colorScheme.primary)
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.fps_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (isGlassActive) {
        Box(
            modifier = glassModifier
                .fillMaxWidth()
                .clickable { onExpand() }
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        ) {
            cardContent()
        }
    } else {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth().clickable { onExpand() }
        ) {
            cardContent()
        }
    }
}

@Composable
fun AppIconView(drawable: Drawable, modifier: Modifier = Modifier) {
    AndroidView(factory = { ctx -> ImageView(ctx).apply { setImageDrawable(drawable); scaleType = ImageView.ScaleType.FIT_CENTER } }, modifier = modifier)
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun ZkmLineChart(points: List<Float>, maxVal: Float, lineColor: Color, strokeWidth: Float = 3f, fillColor: Color? = null) {
    if (points.isEmpty()) return
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width; val height = size.height; val stepX = width / (points.size - 1).coerceAtLeast(1)
        val path = Path(); val fillPath = Path()
        points.forEachIndexed { index, value ->
            val x = index * stepX; val y = height - ((value / maxVal) * height)
            if (index == 0) { path.moveTo(x, y); fillPath.moveTo(x, height); fillPath.lineTo(x, y) } else { path.lineTo(x, y); fillPath.lineTo(x, y) }
        }
        fillPath.lineTo(width, height); fillPath.close()
        if (fillColor != null) drawPath(path = fillPath, color = fillColor)
        drawPath(path = path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

fun getFpsColor(fps: Float): Color { 
    return when { 
        fps >= 55 -> Color(0xFF4CAF50)
        fps >= 25 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    } 
}
