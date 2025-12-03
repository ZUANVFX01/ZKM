/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.components.PinnedTopAppBar
import com.zuan.kernelmanager.ui.navigation.BottomNavigationBar
import com.zuan.kernelmanager.ui.navigation.NavigationRoute
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(), 
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val colorScheme = MaterialTheme.colorScheme
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val extensions by viewModel.extensionList.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDeviceInfo(context)
                viewModel.startRamMonitor(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { PinnedTopAppBar(scrollBehavior = scrollBehavior, hazeState = hazeState) },
        bottomBar = { BottomNavigationBar(navController, hazeState = hazeState) },
        modifier = Modifier.background(colorScheme.background)
    ) { innerPadding ->
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

            ExtensionMenuSection(
                extensions = extensions,
                onItemClick = { route -> navController.navigate(route) }
            )
            Spacer(modifier = Modifier.height(24.dp))

            FpsManagerSettingsSection(
                onClick = { navController.navigate(NavigationRoute.FpsManager.route) }
            )

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 24.dp))
        }
    }
}

@Composable
fun DeviceInfoSection(deviceInfo: DeviceInfo) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = isSystemInDarkTheme()
    val gradientColors = if (isDark) {
        listOf(Color(0xFF004D40), Color(0xFF00695C)) 
    } else {
        listOf(Color(0xFF00897B), Color(0xFF4DB6AC)) 
    }

    val animatedProgress by animateFloatAsState(
        targetValue = deviceInfo.ramUsageProgress,
        label = "RamProgress",
        animationSpec = androidx.compose.animation.core.tween(500)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(180.dp), 
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors))
        ) {
            // --- WATERMARK LOGO ICON ---
            Icon(
                painter = painterResource(id = R.drawable.ic_app),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .offset(x = 30.dp, y = 30.dp)
                    .rotate(-15f)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // === BAGIAN KIRI (TEKS) ===
                Column(modifier = Modifier.weight(1f)) {
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = deviceInfo.manufacturer,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 40.sp,
                            letterSpacing = (-1).sp
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                                        // --- COMPACT MODE: BADGE SOLID HIJAU ---
                        Surface(
                            color = Color(0xFF4CAF50), 
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(20.dp) // Turunkan lagi ke 20.dp biar ramping
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp), // Padding tipis biar text dapat ruang
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified, 
                                    contentDescription = null,
                                    tint = Color.White, 
                                    modifier = Modifier.size(10.dp) // Icon dikecilkan ke 10.dp
                                )
                                Spacer(modifier = Modifier.width(2.dp)) // Jarak icon ke text dirapatkan
                                Text(
                                    text = deviceInfo.buildTags.uppercase(),
                                    color = Color.White, 
                                    fontSize = 8.sp, // Font 8.sp
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp, // Huruf dirapatkan sedikit biar "KEYS" muat
                                    maxLines = 1
                                )
                            }
                        }
                        // ---------------------------------------

                        // ----------------------------------
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = deviceInfo.deviceCodename,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = deviceInfo.deviceName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // --- CPU INFO ---
                    Column {
                        Text(
                            text = deviceInfo.buildUserLabel.uppercase(), 
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = deviceInfo.cpuModel, 
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // === BAGIAN KANAN (RAM MONITOR) ===
                // ... (Bagian ini tidak berubah) ...
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White.copy(alpha = 0.2f),
                            strokeWidth = 8.dp,
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            strokeWidth = 8.dp,
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round 
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = deviceInfo.ramAvailableText, 
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Free",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Total: ${deviceInfo.ramTotalText}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun SystemInfoSection(deviceInfo: DeviceInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min), 
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KernelInfoCard(
            deviceInfo = deviceInfo, 
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SystemStatsCard(
                title = "Android", 
                icon = Icons.Default.Android, 
                value = deviceInfo.androidVersionNumber, 
                unit = "Version", 
                trend = deviceInfo.androidVersionTrend,
                modifier = Modifier.weight(0.4f).fillMaxWidth()
            )

            SystemStatsCard(
                title = "Selinux", 
                icon = Icons.Default.Security, 
                value = deviceInfo.selinuxStatus, 
                unit = "Mode", 
                trend = "Status",
                modifier = Modifier.weight(0.6f).fillMaxWidth().fillMaxHeight()
            )
        }
    }
}

@Composable
fun KernelInfoCard(deviceInfo: DeviceInfo, modifier: Modifier = Modifier) {
    var isFullKernelVersion by rememberSaveable { mutableStateOf(true) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val displayText = if (isFullKernelVersion) deviceInfo.fullKernelVersion else deviceInfo.kernelVersion

    Card(
        modifier = modifier, 
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.ic_linux),
                contentDescription = "Linux",
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).size(20.dp),
                contentScale = ContentScale.Fit,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colorScheme.onSurface)
            )
            Column {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { isFullKernelVersion = !isFullKernelVersion },
                            onLongClick = { clipboardManager.setText(AnnotatedString(displayText)) }
                        )
                ) {
                    Text("Kernel", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(displayText, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, lineHeight = 16.sp, modifier = Modifier.padding(end = 24.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp).background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, "Kernel", modifier = Modifier.size(100.dp), tint = colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { 
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS)
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) 
                                context.startActivity(intent)
                            } catch (e: Exception) { e.printStackTrace() }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).width(120.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primaryContainer, contentColor = colorScheme.onPrimaryContainer)
                    ) {
                        Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatsCard(
    title: String, 
    icon: ImageVector, 
    value: String, 
    unit: String, 
    trend: String,
    modifier: Modifier = Modifier
) {
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colorScheme.onSurface)
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, title, modifier = Modifier.size(16.dp), tint = colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(unit, fontSize = 14.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowDownward, "Trend", modifier = Modifier.size(16.dp), tint = colorScheme.onSurfaceVariant)
                Text(trend, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
fun GpuInfoSection(deviceInfo: DeviceInfo) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Graphics Processing Unit", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Memory, "GPU", modifier = Modifier.size(20.dp), tint = colorScheme.onPrimaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                GpuDetailRow("GPU:", deviceInfo.gpuModel)
                Spacer(modifier = Modifier.height(8.dp))
                GpuDetailRow("Vendor:", deviceInfo.gpuVendor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth().background(Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("OpenGL ES Info:", fontSize = 12.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(deviceInfo.gpuGlVersion, fontSize = 10.sp, color = colorScheme.onSurfaceVariant, lineHeight = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GpuDetailRow(label: String, value: String) {
    val colorScheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 14.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium, modifier = Modifier.width(60.dp))
        Text(value, fontSize = 16.sp, color = colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ExtensionMenuSection(extensions: List<ExtensionItem>, onItemClick: (String) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                append("Extension menu")
                withStyle(SpanStyle(baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript, fontSize = 16.sp)) {
                    append(extensions.size.toString())
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
            items(extensions) { extension ->
                ExtensionItemCard(extension, onClick = { onItemClick(extension.route) })
            }
        }
    }
}

@Composable
fun ExtensionItemCard(extension: ExtensionItem, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = colorScheme.primary == Color(0xFFBB86FC) || colorScheme.background == Color(0xFF121212)
    val cardColor = if (isDarkMode) Color(0xFF004D40) else Color(0xFF00695C)

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Icon(
            imageVector = if (extension.status == ExtensionStatus.GOOD) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
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
            Text(extension.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(extension.type, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FpsManagerSettingsSection(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val isDarkMode = colorScheme.primary == Color(0xFFBB86FC) || colorScheme.background == Color(0xFF121212)
    val iconBgColor = if (isDarkMode) Color(0xFF004D40) else Color(0xFF00695C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ChevronRight, null, tint = colorScheme.onSurfaceVariant)
        Text("Adjust the FpsManager settings", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Tune, "Performance", tint = Color.White)
        }
    }
}
