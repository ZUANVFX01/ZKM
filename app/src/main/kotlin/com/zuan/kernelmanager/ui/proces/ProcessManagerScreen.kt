/*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.proces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.utils.ProcessData
import com.zuan.kernelmanager.utils.ProcessUtils
import com.zuan.kernelmanager.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ProcessManagerViewModel : ViewModel() {
    var processList by mutableStateOf<List<ProcessData>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set

    var limitOption by mutableStateOf(10) // Default 10
        private set

    // Default sort CPU
    var sortType by mutableStateOf(SortType.CPU)
        private set

    fun setLimit(newLimit: Int) {
        limitOption = newLimit
        refreshData()
    }

    fun setSort(newSort: SortType) {
        sortType = newSort
        refreshData()
    }

    fun startMonitoring(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                updateData(context)
                delay(3000)
            }
        }
    }

    fun refreshData() {
        isLoading = true
    }

    private fun updateData(context: android.content.Context) {
        // Pass sortType ke Utils
        val data = ProcessUtils.getTopProcesses(context, limitOption, sortType)
        viewModelScope.launch(Dispatchers.Main) {
            processList = data
            isLoading = false
        }
    }
}

@Composable
fun ProcessManagerScreen(
    navController: NavController,
    viewModel: ProcessManagerViewModel = viewModel()
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.startMonitoring(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Process Monitor", fontWeight = FontWeight.Bold)
                        val sortText = if (viewModel.sortType == SortType.CPU) "Highest CPU" else "Highest RAM"
                        Text(sortText, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    LimitSelector(
                        currentLimit = viewModel.limitOption,
                        onLimitSelected = { viewModel.setLimit(it) }
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.surface
                )
            )
        },
        containerColor = colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Columns (Interactive Sorting)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("APP / PKG", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                
                Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PID", width = 40.dp, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    
                    // Clickable Header: RES
                    SortableHeader(
                        title = "RES", 
                        width = 50.dp, 
                        isActive = viewModel.sortType == SortType.RES,
                        onClick = { viewModel.setSort(SortType.RES) }
                    )
                    
                    // Clickable Header: CPU
                    SortableHeader(
                        title = "CPU", 
                        width = 40.dp, 
                        isActive = viewModel.sortType == SortType.CPU,
                        onClick = { viewModel.setSort(SortType.CPU) }
                    )
                }
            }

            if (viewModel.processList.isEmpty() && viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(viewModel.processList) { process ->
                        ProcessItem(process, viewModel.sortType)
                        Divider(color = colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun SortableHeader(title: String, width: androidx.compose.ui.unit.Dp, isActive: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    // Jika aktif, warnanya Primary (Biru/Teal/dll), jika tidak abu-abu
    val textColor = if (isActive) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    
    Box(
        modifier = Modifier
            .width(width)
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        // Indikator kecil jika aktif (optional)
        if (isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(16.dp)
                    .height(2.dp)
                    .background(textColor)
            )
        }
    }
}

@Composable
fun AppIcon(drawable: android.graphics.drawable.Drawable?, modifier: Modifier = Modifier) {
    if (drawable != null) {
        AndroidView(
            factory = { context ->
                android.widget.ImageView(context).apply {
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                }
            },
            update = { imageView ->
                imageView.setImageDrawable(drawable)
            },
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(8.dp)
        )
    }
}

@Composable
fun LimitSelector(currentLimit: Int, onLimitSelected: (Int) -> Unit) {
    val options = listOf(5, 10, 20)
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { limit ->
            val isSelected = currentLimit == limit
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onLimitSelected(limit) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = limit.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProcessItem(process: ProcessData, sortType: SortType) {
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Opsi: Kill process atau detail */ }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon App
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(drawable = process.icon, modifier = Modifier.fillMaxSize())
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nama Aplikasi & Package
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = process.appName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = process.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }

        // Stats: PID, RES, CPU
        Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(process.pid, modifier = Modifier.width(40.dp), fontSize = 11.sp, color = colorScheme.onSurface, maxLines = 1)
            
            // Logic Warna: Jika Sort RAM aktif, warnai kolom RAM. Jika Sort CPU aktif, warnai kolom CPU.
            val resColor = if (sortType == SortType.RES) colorScheme.primary else colorScheme.onSurface
            Text(
                process.res, 
                modifier = Modifier.width(50.dp), 
                fontSize = 11.sp, 
                color = resColor, 
                fontWeight = if (sortType == SortType.RES) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
            
            // Highlight CPU tinggi tetap merah, tapi jika sedang sort CPU jadi bold
            val cpuVal = process.cpu.replace("%", "").toFloatOrNull() ?: 0f
            val isHighLoad = cpuVal > 10f
            
            val cpuColor = when {
                isHighLoad -> Color.Red
                sortType == SortType.CPU -> colorScheme.primary
                else -> colorScheme.onSurface
            }
            
            Text(
                process.cpu, 
                modifier = Modifier.width(40.dp), 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Bold, 
                color = cpuColor,
                maxLines = 1
            )
        }
    }
}

// Helper untuk Text dengan fixed width
@Composable
fun Text(text: String, width: androidx.compose.ui.unit.Dp, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight? = null, color: Color) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}