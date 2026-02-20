/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.zuan.kernelmanager.ui.fpsmanager

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.zuan.kernelmanager.R
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

@Composable
fun FpsManagerGamesContent(
    contentColor: Color, 
    subContentColor: Color,
    isGlassActive: Boolean = false,
    hazeState: HazeState
) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm: PackageManager = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            
            val filtered = apps.filter { appInfo: ApplicationInfo ->
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isGame = (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                
                (isGame || (!isSystem || isUpdatedSystem)) && appInfo.packageName != context.packageName
            }.mapNotNull { appInfo: ApplicationInfo ->
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    AppInfo(
                        label = appInfo.loadLabel(pm).toString(),
                        packageName = appInfo.packageName,
                        icon = appInfo.loadIcon(pm)
                    )
                } else null
            }.sortedBy { it.label }
            
            installedApps = filtered
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.fps_game_launcher),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        Text(
            stringResource(R.string.fps_game_launcher_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = subContentColor
        )
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(installedApps) { app ->
                    GameCard(app, contentColor, isGlassActive)
                }
            }
        }
    }
}

@Composable
fun GameCard(app: AppInfo, textColor: Color, isGlassActive: Boolean = false) {
    val context = LocalContext.current
    
    val cardColors = if (isGlassActive) {
        CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = cardColors,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        onClick = {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            launchIntent?.let { context.startActivity(it) }
        }
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
