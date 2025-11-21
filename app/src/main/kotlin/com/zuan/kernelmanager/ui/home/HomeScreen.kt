/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.CustomListItem
import com.zuan.kernelmanager.ui.components.PinnedTopAppBar
import com.zuan.kernelmanager.ui.navigation.BottomNavigationBar
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import com.zuan.kernelmanager.ui.home.HomeViewModel.DeviceInfo
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

// Data class untuk Extension Menu - field 'route' sudah ada
data class ExtensionItem(
    val name: String,
    val type: String,
    val imageVector: ImageVector,
    val status: ExtensionStatus,
    val route: String
)

enum class ExtensionStatus { GOOD, WARNING }

// Data class untuk GPU Info
data class GpuInfo(
    val name: String,
    val vendor: String,
    val openglInfo: String
)

// UPDATE: Kernel sudah dihapus dari sini
val dummyExtensions = listOf(
    ExtensionItem("Terminal", "SYSTEM", Icons.Default.Code, ExtensionStatus.GOOD, NavigationRoute.Terminal.route),
    ExtensionItem("SetEdit", "SYSTEM", Icons.Default.Description, ExtensionStatus.WARNING, NavigationRoute.SetEdit.route),
    ExtensionItem("Performa", "PERFORMANCE", Icons.Default.Speed, ExtensionStatus.GOOD, NavigationRoute.Performance.route)
)

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colorScheme = MaterialTheme.colorScheme

    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.loadDeviceInfo(context)
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            PinnedTopAppBar(
                scrollBehavior = scrollBehavior,
                hazeState = hazeState
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.background(colorScheme.background)
    ) { innerPadding ->
        val deviceInfo by viewModel.deviceInfo.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .haze(state = hazeState)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 24.dp))

            DeviceInfoSection(deviceInfo)
            Spacer(modifier = Modifier.height(24.dp))

            SystemInfoSection(deviceInfo)
            Spacer(modifier = Modifier.height(24.dp))

            GpuInfoSection(deviceInfo)
            Spacer(modifier = Modifier.height(24.dp))

            // Pass navController untuk navigasi saat diklik
            ExtensionMenuSection(
                onItemClick = { route ->
                    navController.navigate(route)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            PerformanceSettingsSection(
                onClick = {
                    navController.navigate(NavigationRoute.Performance.route)
                }
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 24.dp))
        }
    }
}

// ... (Kode DeviceInfoSection, SystemInfoSection, KernelInfoCard, SystemStatsCard, GpuInfoSection, GpuInfoCard sama seperti sebelumnya) ...
// (Untuk menghemat ruang, saya fokus ke bagian yang berubah. Jika Anda perlu full code, silakan bilang.)

@Composable
fun DeviceInfoSection(deviceInfo: DeviceInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = colorScheme.primary == Color(0xFFBB86FC) ||
            colorScheme.background == Color(0xFF121212)

    val gradientBrush = if (isDarkMode) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF004D40),
                colorScheme.surface
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF00695C),
                colorScheme.background
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                brush = gradientBrush,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device Info",
                    tint = colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device Information",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))

                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = deviceInfo.manufacturer,
                        color = colorScheme.onSurface,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = deviceInfo.deviceCodename,
                        color = colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = deviceInfo.deviceName,
                            color = colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp)
                ) {
                    CircularProgressIndicator(
                        progress = 1f,
                        modifier = Modifier.fillMaxSize(),
                        color = colorScheme.onSurface.copy(alpha = 0.3f),
                        strokeWidth = 10.dp
                    )
                    CircularProgressIndicator(
                        progress = 0.65f,
                        modifier = Modifier.fillMaxSize(),
                        color = colorScheme.onSurface,
                        strokeWidth = 10.dp
                    )
                    Text(
                        text = "${deviceInfo.ramInfo}\nAvailable",
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SystemInfoSection(deviceInfo: DeviceInfo) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KernelInfoCard(
            deviceInfo = deviceInfo,
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SystemStatsCard(
                title = "Android",
                icon = Icons.Default.Android,
                value = deviceInfo.androidVersion.split(" ").firstOrNull() ?: "13",
                unit = "Version",
                trend = deviceInfo.androidVersion.split(" ").getOrNull(1) ?: "Tiramisu"
            )
            SystemStatsCard(
                title = "Selinux",
                icon = Icons.Default.Security,
                value = "Enforcing",
                unit = "Mode",
                trend = "Strict"
            )
        }
    }
}

@Composable
fun KernelInfoCard(deviceInfo: DeviceInfo, modifier: Modifier = Modifier) {
    var isFullKernelVersion by rememberSaveable { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.height(350.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.ic_linux),
                contentDescription = "Linux Tux Icon",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(20.dp),
                contentScale = ContentScale.Fit,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colorScheme.onSurface)
            )

            Column {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { isFullKernelVersion = !isFullKernelVersion },
                            onLongClick = {
                                clipboardManager.setText(
                                    AnnotatedString(
                                        if (isFullKernelVersion) deviceInfo.fullKernelVersion
                                        else deviceInfo.kernelVersion
                                    )
                                )
                            }
                        )
                ) {
                    Text(
                        text = "Kernel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isFullKernelVersion) deviceInfo.fullKernelVersion
                        else deviceInfo.kernelVersion,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Kernel Info",
                        modifier = Modifier.size(100.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { /* TODO: Navigate to kernel details */ },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .width(120.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            text = "View Details",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatsCard(title: String, icon: ImageVector, value: String, unit: String, trend: String) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colorScheme.onSurface)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(16.dp), tint = colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(text = unit, fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Status", modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                Text(text = trend, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
fun GpuInfoSection(deviceInfo: DeviceInfo) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GpuInfoCard(
            gpuInfo = GpuInfo(
                name = deviceInfo.gpuModel,
                vendor = deviceInfo.gpuVendor,
                openglInfo = deviceInfo.gpuGlVersion
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun GpuInfoCard(gpuInfo: GpuInfo, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Graphics Processing Unit", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = "GPU", modifier = Modifier.size(20.dp), tint = colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "GPU:", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                    Text(text = gpuInfo.name, fontSize = 16.sp, color = colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Vendor:", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
                    Text(text = gpuInfo.vendor, fontSize = 16.sp, color = colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "OpenGL ES Info:", fontSize = 12.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = gpuInfo.openglInfo, fontSize = 10.sp, color = colorScheme.onSurfaceVariant, lineHeight = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ExtensionMenuSection(onItemClick: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Extension menu")
                withStyle(style = SpanStyle(baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript, fontSize = 16.sp)) {
                    append(dummyExtensions.size.toString())
                }
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyExtensions) { extension ->
                ExtensionItemCard(extension = extension, onClick = { onItemClick(extension.route) })
            }
        }
    }
}

@Composable
fun ExtensionItemCard(extension: ExtensionItem, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = colorScheme.primary == Color(0xFFBB86FC) || colorScheme.background == Color(0xFF121212)
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDarkMode) Color(0xFF004D40) else Color(0xFF00695C))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = if (extension.status == ExtensionStatus.GOOD) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = "Status",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.TopStart)
        )
        Icon(
            imageVector = extension.imageVector,
            contentDescription = extension.name,
            modifier = Modifier.align(Alignment.Center).size(100.dp),
            tint = Color.White
        )
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(text = extension.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = extension.type, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PerformanceSettingsSection(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = colorScheme.primary == Color(0xFFBB86FC) || colorScheme.background == Color(0xFF121212)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        Text(text = "Adjust the performance settings", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDarkMode) Color(0xFF004D40) else Color(0xFF00695C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Tune, contentDescription = "Performance Settings", tint = Color.White)
        }
    }
}