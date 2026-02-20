/*
 * Original code from: libxzr (HorizonKernelFlasher) and capntrips (KernelFlasher)
 * Modified and integrated by: Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.animation.ExperimentalAnimationApi::class,
    androidx.compose.ui.unit.ExperimentalUnitApi::class,
    kotlinx.serialization.ExperimentalSerializationApi::class
)

package com.zuan.kernelmanager.ui.flasher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// --- IMPORT COIL & SETTINGS ---
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.settings.BgType
import com.zuan.kernelmanager.ui.settings.SettingsViewModel
import com.zuan.kernelmanager.ui.components.VideoWallpaperPlayer
import com.zuan.kernelmanager.ui.components.WeatherEffectOverlay

// --- IMPORT ZUAN UTILS ---
import com.zuan.kernelmanager.utils.KernelInfoUtils
import com.zuan.kernelmanager.utils.BootInfo

// --- IMPORT HAZE ---
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

// --- IMPORT MODULE CAPNTRIPS ---
import com.github.capntrips.kernelflasher.FilesystemService
import com.github.capntrips.kernelflasher.IFilesystemService
import com.github.capntrips.kernelflasher.ui.screens.main.MainContent
import com.github.capntrips.kernelflasher.ui.screens.main.MainViewModel
import com.github.capntrips.kernelflasher.ui.screens.backups.BackupsContent
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotContent
import com.github.capntrips.kernelflasher.ui.screens.slot.SlotFlashContent
import com.github.capntrips.kernelflasher.ui.screens.backups.SlotBackupsContent

// --- IMPORT LIBSU ---
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File 
import java.io.InputStream

enum class FlasherMode { HORIZON, CAPNTRIPS }

@Composable
fun generateFlasherBackgroundColors(isDark: Boolean, primaryColor: Color): Pair<Color, Color> {
    return if (isDark) {
        val darkTintedBg = primaryColor.copy(
            red = primaryColor.red * 0.08f,
            green = primaryColor.green * 0.08f,
            blue = primaryColor.blue * 0.08f,
            alpha = 1f
        ).compositeOver(Color(0xFF0A0A0A))
        val cardColor = primaryColor.copy(
            red = primaryColor.red * 0.15f,
            green = primaryColor.green * 0.15f,
            blue = primaryColor.blue * 0.15f,
            alpha = 1f
        ).compositeOver(Color(0xFF141414))
        Pair(darkTintedBg, cardColor)
    } else {
        val softBackground = Color(
            red = 0.97f + (primaryColor.red * 0.03f),
            green = 0.97f + (primaryColor.green * 0.03f),
            blue = 0.97f + (primaryColor.blue * 0.03f),
            alpha = 1f
        )
        val cardColor = Color(0xFFFFFFFF)
        Pair(softBackground, cardColor)
    }
}

// =========================================================================
// [FIXED] FUNGSI SMART EXTRACT: DETEKSI NAMA ASSET OTOMATIS
// =========================================================================
private fun setupFlashTools(context: Context): String {
    // Map: Nama target di folder files -> Kemungkinan nama di assets
    val toolsMap = mapOf(
        "httools_static" to listOf("httools_static", "libhttools_static", "libhttools_static.so"),
        "lptools_static" to listOf("lptools_static", "liblptools_static", "liblptools_static.so"),
        "magiskboot"     to listOf("magiskboot", "libmagiskboot", "libmagiskboot.so"),
        "flash_ak3.sh"   to listOf("flash_ak3.sh")
    )
    
    var status = "Ready"
    
    try {
        val filesDir = context.filesDir
        if (!filesDir.exists()) filesDir.mkdirs()

        for ((targetName, possibleAssetNames) in toolsMap) {
            val destFile = File(filesDir, targetName)
            
            // Cek apakah perlu copy (jika belum ada atau size 0)
            if (!destFile.exists() || destFile.length() == 0L) {
                var copied = false
                
                // Coba cari aset satu per satu (untuk mengatasi masalah nama 'lib...')
                for (assetName in possibleAssetNames) {
                    try {
                        context.assets.open(assetName).use { inputStream ->
                            destFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        copied = true
                        break // Berhasil copy, lanjut ke file berikutnya
                    } catch (e: Exception) {
                        // Lanjut coba nama aset berikutnya
                    }
                }
                
                if (copied) {
                    // Set Permission Eksekusi
                    if (!destFile.setExecutable(true, false)) {
                        Shell.cmd("chmod 755 ${destFile.absolutePath}").exec()
                    }
                } else {
                    // Gagal menemukan aset dengan nama apapun
                    status = "Missing asset for: $targetName"
                }
            }
        }
        
        // Final touch: Set PATH
        Shell.cmd("export PATH=\$PATH:${filesDir.absolutePath}").exec()
        Shell.cmd("chmod 755 ${filesDir.absolutePath}/*").exec() // Paksa chmod semua
        
    } catch (e: Exception) {
        status = "Error: ${e.message}"
    }
    return status
}

@Composable
fun KernelFlasherScreen(
    rootNavController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) { 
    // ... (Bagian Settings & Theme SAMA SEPERTI SEBELUMNYA) ...
    // --- SETTINGS COLLECTION ---
    val bgType by settingsViewModel.bgType.collectAsStateWithLifecycle()
    val bgUriString by settingsViewModel.backgroundImageUri.collectAsStateWithLifecycle()
    val isVideo by settingsViewModel.isVideoWallpaper.collectAsStateWithLifecycle()
    val isBgBlur by settingsViewModel.isBgBlur.collectAsStateWithLifecycle()
    val blurStrength by settingsViewModel.blurStrength.collectAsStateWithLifecycle()
    val bgSaturation by settingsViewModel.bgSaturation.collectAsStateWithLifecycle()
    val bgContrast by settingsViewModel.bgContrast.collectAsStateWithLifecycle()
    val weatherEffect by settingsViewModel.weatherEffect.collectAsStateWithLifecycle()
    val weatherIntensity by settingsViewModel.weatherIntensity.collectAsStateWithLifecycle()
    val isHazeEnabled by settingsViewModel.isHazeEnabled.collectAsStateWithLifecycle()
    val cardDarkness by settingsViewModel.cardDarkness.collectAsStateWithLifecycle()
    val isDynamic by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val themeColorName by settingsViewModel.currentThemeColor.collectAsStateWithLifecycle()
    val isCustomColor by settingsViewModel.isCustomColor.collectAsStateWithLifecycle()
    val customPrimary by settingsViewModel.customPrimaryColor.collectAsStateWithLifecycle()
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
    
    val isDark = when (themeMode) {
        com.zuan.kernelmanager.ui.theme.ThemeMode.LIGHT -> false
        com.zuan.kernelmanager.ui.theme.ThemeMode.DARK -> true
        com.zuan.kernelmanager.ui.theme.ThemeMode.SYSTEM_DEFAULT -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    
    val effectivePrimary = if (isDynamic) MaterialTheme.colorScheme.primary else if (isCustomColor) Color(customPrimary) else themeColorName.primary
    
    val mainBackgroundColor = if (bgType != BgType.SYSTEM) Color.Transparent else if (isDynamic) MaterialTheme.colorScheme.background else generateFlasherBackgroundColors(isDark, effectivePrimary).first
    val themeCardColor = if (isDynamic) MaterialTheme.colorScheme.surfaceContainerHigh else generateFlasherBackgroundColors(isDark, effectivePrimary).second
    
    val isCustomBg = bgType != BgType.SYSTEM
    val isGlassActive = isHazeEnabled && isCustomBg
    val finalCardColor = when {
        isGlassActive -> Color.Transparent
        isCustomBg -> Color.Black.copy(alpha = cardDarkness)
        isDynamic -> MaterialTheme.colorScheme.surfaceContainer
        else -> themeCardColor
    }
    val contentColor = when {
        isCustomBg -> Color.White
        isDynamic -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    var currentMode by remember { mutableStateOf(FlasherMode.CAPNTRIPS) }
    val localHazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(with(density) { 160.dp.roundToPx() }) }
    val headerHeightDp = with(density) { headerHeightPx.toDp() }
    val isAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // =========================================================================
    // [FIXED] LOGIC LOADING: CEGAH RACE CONDITION
    // =========================================================================
    var fsManager by remember { mutableStateOf<FileSystemManager?>(null) }
    var extractionStatus by remember { mutableStateOf(context.getString(R.string.flasher_init)) }
    var mainViewModel by remember { mutableStateOf<MainViewModel?>(null) } 

    // 1. Bind Service
    DisposableEffect(Unit) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                try {
                    val serviceInterface = IFilesystemService.Stub.asInterface(service)
                    fsManager = FileSystemManager.getRemote(serviceInterface.getFileSystemService())
                } catch (e: Exception) { e.printStackTrace() }
            }
            override fun onServiceDisconnected(name: ComponentName?) { fsManager = null }
        }
        val intent = Intent(context, FilesystemService::class.java)
        RootService.bind(intent, connection)
        onDispose { try { RootService.unbind(connection) } catch (e: Exception) {} }
    }

    // 2. Eksekusi Copy File (Jalan Paralel)
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            extractionStatus = context.getString(R.string.flasher_checking_tools)
            // Jalankan fungsi smart extract
            val result = setupFlashTools(context)
            // Set working directory
            Shell.cmd("cd ${context.filesDir.absolutePath}").exec()
            extractionStatus = result
        }
    }

    // 3. Init ViewModel (WAIT FOR EXTRACTION TO BE READY)
    // [PENTING] Menambahkan 'extractionStatus' sebagai key agar LaunchedEffect memantau perubahannya
    LaunchedEffect(fsManager, extractionStatus) {
        // Cekapakah Root ready AND Extraction sudah "Ready"
        if (fsManager != null && extractionStatus == "Ready" && mainViewModel == null) {
            withContext(Dispatchers.IO) {
                val app = context.applicationContext
                // Init ViewModel hanya setelah file tools dijamin ada
                val vm = MainViewModel(app, fsManager!!, rootNavController)
                withContext(Dispatchers.Main) {
                    mainViewModel = vm 
                }
            }
        }
    }

    // ... (UI COMPOSITION BAWAH TETAP SAMA, TIDAK PERLU DIUBAH) ...
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize().background(mainBackgroundColor)) {
            
            // BACKGROUND LAYERS (SAMA)
            if (isCustomBg && bgUriString != null) {
                val blurModifier = if (isBgBlur && blurStrength > 0f) Modifier.blur(blurStrength.dp) else if (isBgBlur) Modifier.blur(20.dp) else Modifier
                val saturationMatrix = ColorMatrix().apply { setToSaturation(bgSaturation) }
                val colorFilter = ColorFilter.colorMatrix(saturationMatrix)
                val uri = Uri.parse(bgUriString)
                Box(modifier = Modifier.fillMaxSize().hazeSource(state = localHazeState, zIndex = 0f)) {
                    if (isVideo) {
                        VideoWallpaperPlayer(uri = uri, modifier = Modifier.fillMaxSize().then(blurModifier))
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(uri).build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().then(blurModifier),
                            contentScale = ContentScale.Crop,
                            colorFilter = colorFilter
                        )
                    }
                    if (bgContrast > 0f) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = bgContrast)))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().hazeSource(state = localHazeState, zIndex = 0f))
            }

            WeatherEffectOverlay(effect = weatherEffect, intensity = weatherIntensity, modifier = Modifier.fillMaxSize())

            // CONTENT SWITCHER
            AnimatedContent(
                targetState = currentMode,
                label = "FlasherModeSwitch",
                modifier = Modifier.fillMaxSize()
            ) { mode ->
                when (mode) {
                    FlasherMode.HORIZON -> {
                        HorizonFlasherContent(
                            navController = rootNavController, 
                            headerPadding = headerHeightDp,
                            isGlassActive = isGlassActive,
                            hazeState = localHazeState,
                            cardColor = finalCardColor,
                            contentColor = contentColor,
                            primaryColor = effectivePrimary
                        ) 
                    }
                    FlasherMode.CAPNTRIPS -> {
                        // Tampilkan UI hanya jika ViewModel sudah jadi (artinya file sudah ready)
                        if (mainViewModel != null) {
                            CapntripsContainer(
                                rootNavController = rootNavController, 
                                headerPadding = headerHeightDp,
                                viewModel = mainViewModel!! 
                            )
                        } else {
                            // Loading State
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = effectivePrimary)
                                    Spacer(Modifier.height(16.dp))
                                    
                                    // Feedback Status Text
                                    val statusText = when {
                                        fsManager == null -> stringResource(R.string.flasher_connecting_root)
                                        extractionStatus != "Ready" -> "Extracting Tools..." // Sedang copy file
                                        else -> stringResource(R.string.flasher_scanning_partitions) // Sedang init VM
                                    }
                                    
                                    Text(statusText, color = contentColor, style = MaterialTheme.typography.bodyMedium)
                                    
                                    // Tampilkan error jika extract gagal
                                    if (extractionStatus.startsWith("Error")) {
                                        Spacer(Modifier.height(8.dp))
                                        Text(extractionStatus, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        Text(stringResource(R.string.please_wait), style = MaterialTheme.typography.bodySmall, color = contentColor.copy(0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Header (SAMA)
            val appBarHazeStyle = if (isCustomBg) {
                HazeStyle(backgroundColor = Color.Black.copy(alpha = 0.2f), blurRadius = 24.dp, noiseFactor = 0.1f, tints = emptyList())
            } else {
                HazeStyle(backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), blurRadius = 24.dp, noiseFactor = 0.05f, tint = null)
            }

            LargeTopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.kernel_flasher_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                        Spacer(Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(end = 16.dp)) {
                            SegmentedButton(
                                selected = currentMode == FlasherMode.HORIZON, 
                                onClick = { currentMode = FlasherMode.HORIZON }, 
                                shape = RoundedCornerShape(12.dp, 0.dp, 0.dp, 12.dp), 
                                icon = { if(currentMode == FlasherMode.HORIZON) Icon(Icons.Default.Check, null) },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = effectivePrimary,
                                    activeContentColor = if (effectivePrimary.luminance() > 0.5f) Color.Black else Color.White,
                                    inactiveContainerColor = if (isCustomBg) Color.White.copy(0.1f) else Color.Transparent,
                                    inactiveContentColor = contentColor
                                )
                            ) { Text(stringResource(R.string.flasher_mode_horizon)) }
                            SegmentedButton(
                                selected = currentMode == FlasherMode.CAPNTRIPS, 
                                onClick = { currentMode = FlasherMode.CAPNTRIPS }, 
                                shape = RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp), 
                                icon = { if(currentMode == FlasherMode.CAPNTRIPS) Icon(Icons.Default.Check, null) },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = effectivePrimary,
                                    activeContentColor = if (effectivePrimary.luminance() > 0.5f) Color.Black else Color.White,
                                    inactiveContainerColor = if (isCustomBg) Color.White.copy(0.1f) else Color.Transparent,
                                    inactiveContentColor = contentColor
                                )
                            ) { Text(stringResource(R.string.flasher_mode_capntrips)) }
                        }
                    }
                },
                navigationIcon = { 
                    IconButton(onClick = { rootNavController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = contentColor) 
                    } 
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent, 
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onGloballyPositioned { coordinates ->
                        if (kotlin.math.abs(headerHeightPx - coordinates.size.height) > 5) {
                            headerHeightPx = coordinates.size.height 
                        }
                    }
                    .hazeEffect(state = localHazeState, style = appBarHazeStyle)
            )
        }
    }
}

// ... CapntripsContainer & Lainnya SAMA SEPERTI FILE SEBELUMNYA ...
@Composable
fun CapntripsContainer(
    rootNavController: NavController, 
    headerPadding: Dp,
    viewModel: MainViewModel
) {
    val internalNavController = rememberNavController()
    val topSpacer = @Composable { Spacer(Modifier.height(headerPadding + 16.dp)) }

    NavHost(
        navController = internalNavController,
        startDestination = "main",
        modifier = Modifier.fillMaxSize()
    ) {
        
        // Error Route
        composable("error/{message}") { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: stringResource(R.string.unknown_error)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.error_occurred), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { internalNavController.popBackStack("main", false) }) {
                    Text(stringResource(R.string.go_back))
                }
            }
        }

        // --- MAIN SCREENS ---
        composable("main") {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                topSpacer()
                MainContent(viewModel = viewModel, navController = internalNavController)
                Spacer(Modifier.height(100.dp))
            }
        }

        composable("backups") {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                topSpacer()
                BackupsContent(viewModel = viewModel.backups, navController = internalNavController)
                Spacer(Modifier.height(100.dp))
            }
        }
         composable("backups/{backupId}") { backStackEntry ->
            val backupId = backStackEntry.arguments?.getString("backupId")
            viewModel.backups.currentBackup = backupId
             Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                topSpacer()
                BackupsContent(viewModel = viewModel.backups, navController = internalNavController)
                Spacer(Modifier.height(100.dp))
             }
        }

        // --- SLOT A ---
        composable("slot_a") {
            val slotVM = viewModel.slotA
            if (slotVM != null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    topSpacer()
                    SlotContent(viewModel = slotVM, slotSuffix = "_a", navController = internalNavController)
                    Spacer(Modifier.height(100.dp))
                }
            } else ErrorScreen(stringResource(R.string.slot_a_not_found), headerPadding)
        }

        val slotARoutes = listOf(
            "slot_a/flash", "slot_a/flash/ak3", "slot_a/flash/image", 
            "slot_a/flash/image/flash", "slot_a/backup", "slot_a/backup/backup"
        )
        slotARoutes.forEach { route ->
            composable(route) { HandleSlotFlash(viewModel.slotA, "_a", internalNavController, headerPadding) }
        }

        val slotABackupsRoutes = listOf(
            "slot_a/backups", "slot_a/backups/{backupId}", "slot_a/backups/{backupId}/restore",
            "slot_a/backups/{backupId}/restore/restore", "slot_a/backups/{backupId}/flash/ak3"
        )
        slotABackupsRoutes.forEach { route ->
            composable(route) { backStackEntry ->
                if(route.contains("{backupId}")) viewModel.backups.currentBackup = backStackEntry.arguments?.getString("backupId")
                HandleSlotBackups(viewModel.slotA, viewModel.backups, "_a", internalNavController, headerPadding)
            }
        }

        // --- SLOT B ---
        composable("slot_b") {
            val slotVM = viewModel.slotB
            if (slotVM != null) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    topSpacer()
                    SlotContent(viewModel = slotVM, slotSuffix = "_b", navController = internalNavController)
                    Spacer(Modifier.height(100.dp))
                }
            } else ErrorScreen(stringResource(R.string.slot_b_not_available), headerPadding)
        }

        val slotBRoutes = listOf(
            "slot_b/flash", "slot_b/flash/ak3", "slot_b/flash/image", 
            "slot_b/flash/image/flash", "slot_b/backup", "slot_b/backup/backup"
        )
        slotBRoutes.forEach { route ->
            composable(route) { HandleSlotFlash(viewModel.slotB, "_b", internalNavController, headerPadding) }
        }
        
        val slotBBackupsRoutes = listOf(
            "slot_b/backups", "slot_b/backups/{backupId}", "slot_b/backups/{backupId}/restore",
            "slot_b/backups/{backupId}/restore/restore", "slot_b/backups/{backupId}/flash/ak3"
        )
        slotBBackupsRoutes.forEach { route ->
            composable(route) { backStackEntry ->
                if (route.contains("{backupId}")) viewModel.backups.currentBackup = backStackEntry.arguments?.getString("backupId")
                HandleSlotBackups(viewModel.slotB, viewModel.backups, "_b", internalNavController, headerPadding) 
            }
        }

        // --- REBOOT ---
         composable("reboot") {
            Box(Modifier.fillMaxSize().padding(top=headerPadding), contentAlignment = Alignment.Center) {
                Button(onClick = { Shell.cmd("reboot").submit() }) { Text(stringResource(R.string.confirm_reboot_system)) }
            }
        }
    }
}

// ==========================================
// 3. HORIZON CONTENT (UPDATED FOR STYLING)
// ==========================================

@Composable
fun HorizonFlasherContent(
    navController: NavController, 
    headerPadding: Dp,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    contentColor: Color,
    primaryColor: Color
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var logs by remember { mutableStateOf("") }
    var isFlashing by remember { mutableStateOf(false) }
    var showRebootDialog by remember { mutableStateOf(false) }

    val activeSlot by produceState(initialValue = stringResource(R.string.loading)) { value = KernelInfoUtils.getActiveSlot() }
    val deviceModel = remember { KernelInfoUtils.getDeviceModel() }
    val kernelVersion = remember { KernelInfoUtils.getKernelVersion() }
    val bootInfoA by produceState(initialValue = BootInfo(stringResource(R.string.scanning), "...", "...")) { value = KernelInfoUtils.getBootInfo("a") }
    val bootInfoB by produceState(initialValue = BootInfo(stringResource(R.string.scanning), "...", "...")) { value = KernelInfoUtils.getBootInfo("b") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isFlashing = true
            logs = context.getString(R.string.flasher_init_process) + "\nTarget: $uri\n"
            scope.launch {
                val worker = com.zuan.kernelmanager.ui.flasher.FlasherWorker(context, uri) { newLog ->
                    logs += "$newLog\n"
                }
                val success = worker.startFlashing()
                isFlashing = false
                if (success) {
                    logs += "\n" + context.getString(R.string.flasher_success)
                    showRebootDialog = true
                } else {
                    logs += "\n" + context.getString(R.string.flasher_failed)
                    Toast.makeText(context, context.getString(R.string.flasher_toast_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = primaryColor) },
            title = { Text(stringResource(R.string.flashing_complete)) },
            text = { Text(stringResource(R.string.flasher_reboot_message)) },
            confirmButton = { Button(onClick = { com.zuan.kernelmanager.ui.flasher.FlasherWorker.rebootDevice() }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text(stringResource(R.string.reboot_system)) } },
            dismissButton = { TextButton(onClick = { showRebootDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)) { Text(stringResource(R.string.later)) } }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = headerPadding + 16.dp, bottom = 100.dp) 
    ) {
        item {
            SectionTitle(stringResource(R.string.horizon_engine_legacy), primaryColor)
            StyledCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardColor = cardColor
            ) {
                Column {
                    PixelListItem(Icons.Default.PhoneAndroid, stringResource(R.string.device_model), deviceModel, contentColor)
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = contentColor.copy(0.1f))
                    PixelListItem(Icons.Default.Memory, stringResource(R.string.kernel_version), kernelVersion, contentColor)
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = contentColor.copy(0.1f))
                    PixelListItem(Icons.Default.SystemUpdate, stringResource(R.string.active_slot), stringResource(R.string.slot_current, activeSlot.uppercase()), primaryColor)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionTitle(stringResource(R.string.boot_partitions), primaryColor)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val isSlotA = activeSlot.contains("A", ignoreCase = true)
                val isSlotB = activeSlot.contains("B", ignoreCase = true)
                Box(Modifier.weight(1f)) { SlotStatusCard(stringResource(R.string.slot_a), isSlotA, bootInfoA, cardColor, contentColor, primaryColor, isGlassActive, hazeState) }
                Box(Modifier.weight(1f)) { SlotStatusCard(stringResource(R.string.slot_b), isSlotB, bootInfoB, cardColor, contentColor, primaryColor, isGlassActive, hazeState) }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            SectionTitle(stringResource(R.string.flasher_console), primaryColor)
            StyledCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).heightIn(min = 150.dp, max = 300.dp),
                isGlassActive = isGlassActive,
                hazeState = hazeState,
                cardColor = if(isGlassActive) Color.Transparent else Color(0xFF1E1E1E).copy(alpha=0.8f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, null, tint = contentColor.copy(0.6f), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.terminal_output), color = contentColor.copy(0.6f), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (logs.isEmpty()) stringResource(R.string.horizon_ready) else logs, color = Color(0xFF00E676), fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { if (!isFlashing) filePickerLauncher.launch("*/*") },
                enabled = !isFlashing,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = if (primaryColor.luminance() > 0.5f) Color.Black else Color.White)
            ) {
                if (isFlashing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.flashing_kernel_progress), style = MaterialTheme.typography.titleMedium)
                } else {
                    Icon(Icons.Default.Bolt, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.flash_horizon_method), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}


// ==========================================
// 4. HELPER COMPOSABLES
// ==========================================

@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    isGlassActive: Boolean,
    hazeState: HazeState,
    cardColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier.clip(shape)
            .hazeEffect(
                state = hazeState, 
                style = HazeStyle(
                    backgroundColor = cardColor.copy(alpha = 0.5f), 
                    blurRadius = 24.dp, 
                    noiseFactor = 0.1f,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(0.1f), shape)
    } else {
        Modifier.clip(shape)
    }

    Card(
        modifier = modifier.then(glassModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if(isGlassActive) Color.Transparent else cardColor)
    ) {
        content()
    }
}

@Composable
fun HandleSlotFlash(slotVM: com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel?, suffix: String, navController: NavController, headerPadding: Dp) {
    if (slotVM != null) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(headerPadding + 16.dp)) 
            SlotFlashContent(viewModel = slotVM, slotSuffix = suffix, navController = navController)
            Spacer(Modifier.height(100.dp))
        }
    } else ErrorScreen(stringResource(R.string.slot_vm_not_found), headerPadding)
}

@Composable
fun HandleSlotBackups(
    slotVM: com.github.capntrips.kernelflasher.ui.screens.slot.SlotViewModel?, 
    backupsVM: com.github.capntrips.kernelflasher.ui.screens.backups.BackupsViewModel,
    suffix: String, 
    navController: NavController,
    headerPadding: Dp
) {
    if (slotVM != null) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(headerPadding + 16.dp)) 
            SlotBackupsContent(slotViewModel = slotVM, backupsViewModel = backupsVM, slotSuffix = suffix, navController = navController)
            Spacer(Modifier.height(100.dp))
        }
    } else ErrorScreen(stringResource(R.string.slot_vm_not_found), headerPadding)
}

@Composable
fun ErrorScreen(msg: String, topPadding: Dp) {
    Box(Modifier.fillMaxSize().padding(top=topPadding + 32.dp), contentAlignment = Alignment.Center) {
        Text(msg)
    }
}

@Composable
fun SectionTitle(title: String, color: Color) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 32.dp, bottom = 8.dp))
}

@Composable
fun PixelListItem(icon: ImageVector, title: String, value: String, contentColor: Color) {
    ListItem(
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = contentColor) },
        supportingContent = { Text(value, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Icon(icon, null, tint = contentColor.copy(0.7f)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SlotStatusCard(
    slotName: String, 
    isActive: Boolean, 
    data: BootInfo, 
    cardColor: Color, 
    contentColor: Color, 
    primaryColor: Color, 
    isGlassActive: Boolean, 
    hazeState: HazeState
) {
    val bgColor = if (isActive) primaryColor.copy(alpha = if(isGlassActive) 0.6f else 1f) else if(isGlassActive) cardColor.copy(alpha=0.3f) else MaterialTheme.colorScheme.surfaceContainerHighest
    
    val textColor = if (isActive) (if(primaryColor.luminance() > 0.5f) Color.Black else Color.White) else contentColor
    
    val shape = RoundedCornerShape(20.dp)
    
    val glassModifier = if (isGlassActive) {
        Modifier.clip(shape)
            .hazeEffect(
                state = hazeState, 
                style = HazeStyle(
                    backgroundColor = bgColor, 
                    blurRadius = 15.dp,
                    tints = emptyList()
                )
            )
            .border(1.dp, Color.White.copy(0.1f), shape)
    } else {
        Modifier.clip(shape)
    }

    Card(
        modifier = Modifier.fillMaxWidth().then(glassModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = if(isGlassActive) Color.Transparent else bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Icon(if (isActive) Icons.Default.CheckCircle else Icons.Outlined.SdStorage, null, tint = textColor, modifier = Modifier.size(20.dp))
                if (isActive) Text(stringResource(R.string.active_label).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(slotName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("SHA1: ${data.sha1.take(8)}...", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = textColor.copy(alpha = 0.7f))
                Text(stringResource(R.string.format_label, data.format), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}
