/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.soc

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
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
import com.zuan.kernelmanager.utils.GovTunable
import com.zuan.kernelmanager.utils.MiscUtils
import com.zuan.kernelmanager.utils.SoCUtils
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState
import kotlin.random.Random

// Custom Colors for Dashboard Cards
private val DashboardDarkBg = Color(0xFF1E1E1E)
private val DashboardLightBg = Color(0xFFF5F5F5)
private val DashboardMintBg = Color(0xFFA7F3B8)
private val DashboardPurpleBg = Color(0xFFBFA8F7)
private val DashboardAccentGreen = Color(0xFF3DDB85)
private val DashboardTextDark = Color(0xFF1E1E1E)
private val DashboardTextLight = Color(0xFFF5F5F5)
private val DashboardCardShape = RoundedCornerShape(28.dp)

@Composable
fun SoCScreen(viewModel: SoCViewModel = viewModel(), navController: NavController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    
    // Mengambil data clusters secara real-time
    val clusters by viewModel.clusterStates.collectAsState()
    
    val openMiscSheet = remember { mutableStateOf(false) }
    var selectedCluster by remember { mutableStateOf<SoCViewModel.CPUState?>(null) }
    
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SoCViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val row1Height = screenHeight * 0.22f
    val row3Height = screenHeight * 0.22f

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startJob()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopJob()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { PinnedTopAppBar(scrollBehavior = scrollBehavior, hazeState = hazeState) },
        bottomBar = { BottomNavigationBar(navController, hazeState = hazeState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().haze(state = hazeState),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding() + 16.dp, bottom = innerPadding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // NEW: Detailed Core Monitor (Tanpa Card per Item, Langsung Text + Progress)
            item {
                DetailedCoreMonitorCard(viewModel = viewModel, modifier = Modifier.fillMaxWidth())
            }

            // Row 1: Summary
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().height(row1Height)) {
                    CPUSummaryCard(viewModel, modifier = Modifier.weight(1f).fillMaxHeight())
                    GPUSummaryCard(viewModel, hazeState, navController, modifier = Modifier.weight(1f).fillMaxHeight())
                }
            }

            // Row 2: Cluster & Misc
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                    ClusterStatusVerticalCard(
                        viewModel, 
                        hazeState, 
                        modifier = Modifier.weight(1f),
                        onClusterClick = { cluster -> selectedCluster = cluster }
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
                        ThermalVisualCard(viewModel, modifier = Modifier.weight(1f).fillMaxWidth())
                        MiscFeaturesCard(onClick = { openMiscSheet.value = true }, modifier = Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }

            // Row 3: Gauge
            item { PerformanceGaugeCard(viewModel, modifier = Modifier.fillMaxWidth().height(row3Height)) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (openMiscSheet.value) {
            MiscBottomSheet(viewModel = viewModel, onDismiss = { openMiscSheet.value = false }, accentColor = DashboardAccentGreen)
        }

        // === REAL-TIME CPU SHEET ===
        if (selectedCluster != null) {
            val liveCluster = clusters.find { it.policyPath == selectedCluster?.policyPath } ?: selectedCluster
            if (liveCluster != null) {
                CpuBottomSheet(
                    cluster = liveCluster, 
                    viewModel = viewModel,
                    onDismiss = { selectedCluster = null },
                    accentColor = DashboardAccentGreen
                )
            }
        }
    }
}

// === NEW: DETAILED CORE MONITOR CARD (Minimalist) ===

@Composable
fun DetailedCoreMonitorCard(viewModel: SoCViewModel, modifier: Modifier = Modifier) {
    val coreList by viewModel.coreMonitorList.collectAsState()
    
    // Cek apakah sistem sedang dalam mode gelap
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Logika Warna Sesuai Request:
    // Light Mode -> Warna GPU Card (DashboardLightBg)
    // Dark Mode  -> Warna CPU Card (DashboardDarkBg)
    val cardBg = if (isDark) DashboardDarkBg else DashboardLightBg
    val contentColor = if (isDark) DashboardTextLight else DashboardTextDark
    val subTextColor = if (isDark) DashboardTextLight.copy(alpha = 0.7f) else DashboardTextDark.copy(alpha = 0.7f)

    DashboardCard(backgroundColor = cardBg, contentColor = contentColor, modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Core Monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${coreList.size} Cores Active", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                }
                Icon(painter = painterResource(R.drawable.ic_cpu), contentDescription = null, tint = DashboardAccentGreen, modifier = Modifier.size(24.dp))
            }

            // Grid Layout (Chunked per 4)
            if (coreList.isEmpty()) {
                 Text("Loading cores...", color = subTextColor, modifier = Modifier.padding(16.dp))
            } else {
                coreList.chunked(4).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowItems.forEach { core -> 
                            CoreDetailItem(
                                core = core, 
                                textColor = contentColor,
                                subTextColor = subTextColor,
                                modifier = Modifier.weight(1f)
                            ) 
                        }
                        // Isi sisa kolom kosong jika baris tidak penuh
                        val missingItems = 4 - rowItems.size
                        if (missingItems > 0) {
                            repeat(missingItems) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CoreDetailItem(
    core: SoCViewModel.CoreMonitorData, 
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedUsage by animateFloatAsState(targetValue = core.usage / 100f, animationSpec = tween(500), label = "coreUsage")
    val barColor = when {
        core.usage > 85f -> Color(0xFFFF5252) // Merah
        core.usage > 50f -> Color(0xFFFFD740) // Kuning
        else -> DashboardAccentGreen
    }

    // TANPA CARD BACKGROUND & CLIP -> Langsung Column
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        // Baris Atas: Nama CPU & Persentase
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "CPU ${core.index}", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Bold, 
                color = subTextColor,
                fontSize = 10.sp
            )
            Text(
                "${core.usage.toInt()}%", 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.ExtraBold, 
                color = barColor,
                fontSize = 10.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Native Linear Progress Indicator (Modern Capsule Look)
        LinearProgressIndicator(
            progress = { animatedUsage },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = barColor,
            trackColor = subTextColor.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round, // Membuat ujungnya bulat (Modern)
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Baris Bawah: Freq & Gov
        Text(
            "${core.freq}", 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.Bold, 
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            core.gov, 
            style = MaterialTheme.typography.bodySmall, 
            fontSize = 9.sp, 
            color = subTextColor, 
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// === CPU BOTTOM SHEET WITH TABS ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuBottomSheet(
    cluster: SoCViewModel.CPUState,
    viewModel: SoCViewModel,
    onDismiss: () -> Unit,
    accentColor: Color
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 0 = Cluster, 1 = Governor Tuning
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Cluster", "Governor Tuning")
    
    val govTunables by viewModel.govTunables.collectAsState()

    // Auto load tunables saat tab tuning dibuka
    LaunchedEffect(selectedTab, cluster.gov) {
        if (selectedTab == 1) {
            viewModel.loadGovTunables(cluster.policyPath, cluster.gov)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
            
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "${cluster.name} Cluster", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "Current: ${cluster.currentFreq} MHz", style = MaterialTheme.typography.bodyMedium, color = accentColor)
                }
                Icon(
                    painter = painterResource(if(cluster.isPrime) R.drawable.ic_rocket_launch else R.drawable.ic_speed),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
                indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = accentColor) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) },
                        selectedContentColor = accentColor,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader("Frequency Scaling")
                        MiscSelectorItem(title = "Minimum Frequency", currentValue = "${cluster.minFreq} MHz", options = cluster.availableFreq) { newFreq -> viewModel.updateFreq("min", newFreq, cluster.policyPath) }
                        MiscSelectorItem(title = "Maximum Frequency", currentValue = "${cluster.maxFreq} MHz", options = cluster.availableFreq) { newFreq -> viewModel.updateFreq("max", newFreq, cluster.policyPath) }
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader("Governor")
                        MiscSelectorItem(title = "Scaling Governor", currentValue = cluster.gov, options = cluster.availableGov) { newGov -> viewModel.updateGov(newGov, cluster.policyPath) }
                    }
                } else {
                    GovernorTuningList(
                        tunables = govTunables,
                        accentColor = accentColor,
                        onApply = { tunable, newVal -> viewModel.applyTunable(tunable, newVal) }
                    )
                }
            }
        }
    }
}

// === NEW: GOVERNOR TUNING LIST COMPONENTS ===

@Composable
fun GovernorTuningList(
    tunables: List<GovTunable>,
    accentColor: Color,
    onApply: (GovTunable, String) -> Unit
) {
    if (tunables.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null, tint = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No Tunables Available", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Governor might not support tuning", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item { SectionHeader("Parameters") }
            items(tunables) { item ->
                TunableItem(item = item, accentColor = accentColor, onApply = onApply)
            }
        }
    }
}

@Composable
fun TunableItem(
    item: GovTunable,
    accentColor: Color,
    onApply: (GovTunable, String) -> Unit
) {
    var textValue by remember { mutableStateOf(item.value) }
    val focusManager = LocalFocusManager.current
    
    // Update text field if value changes externally (e.g. reload)
    LaunchedEffect(item.value) { textValue = item.value }

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp)
    ) {
        Text(text = item.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onApply(item, textValue); focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = accentColor
            ),
            trailingIcon = {
                if (textValue != item.value) {
                     Icon(
                        painter = painterResource(R.drawable.ic_check), 
                        contentDescription = "Save",
                        tint = accentColor,
                        modifier = Modifier.clickable { onApply(item, textValue); focusManager.clearFocus() }
                    )
                }
            }
        )
    }
}

// === EXISTING CLUSTER CARD & MISC COMPONENTS ===

@Composable
fun ClusterStatusVerticalCard(viewModel: SoCViewModel, hazeState: HazeState, modifier: Modifier, onClusterClick: (SoCViewModel.CPUState) -> Unit) {
    val clusters by viewModel.clusterStates.collectAsState()
    DashboardCard(backgroundColor = DashboardLightBg, contentColor = DashboardTextDark, modifier = modifier) {
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp).fillMaxSize()) {
            Text("Processor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.padding(vertical = 4.dp).padding(start = 7.dp).width(2.dp).fillMaxHeight().background(Color.LightGray.copy(alpha = 0.5f)))
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    if (clusters.isEmpty()) Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No Clusters", style = MaterialTheme.typography.bodySmall) }
                    else clusters.forEach { cluster -> ClusterItem(title = cluster.name, state = cluster, icon = if (cluster.isPrime) R.drawable.ic_rocket_launch else R.drawable.ic_speed, isActive = cluster.isPrime, onClick = { onClusterClick(cluster) }) }
                }
            }
        }
    }
}

@Composable
fun ClusterItem(title: String, state: SoCViewModel.CPUState, icon: Int, isActive: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 4.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
             Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(if (isActive) DashboardAccentGreen else Color.LightGray))
            if (isActive) Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
        }
        Spacer(modifier = Modifier.width(12.dp)); Icon(painter = painterResource(icon), contentDescription = null, tint = if (isActive) DashboardTextDark else Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp)); Column { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isActive) DashboardTextDark else Color.Gray); Text("${state.currentFreq} MHz", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
    }
}

@Composable
fun MiscFeaturesCard(onClick: () -> Unit, modifier: Modifier) {
    DashboardCard(backgroundColor = DashboardMintBg, contentColor = DashboardTextDark, modifier = modifier.clickable { onClick() }) {
         Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.1f)) { drawCircle(color = Color.Black, center = Offset(size.width, size.height), radius = size.height) }
            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Misc Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium); Icon(painter = painterResource(R.drawable.ic_settings), contentDescription = null, modifier = Modifier.size(20.dp)) }
                Column { Text(text = "Configs", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp)); Text(text = "Thermal, I/O, MTK", style = MaterialTheme.typography.labelMedium, color = DashboardTextDark.copy(alpha = 0.7f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiscBottomSheet(viewModel: SoCViewModel, onDismiss: () -> Unit, accentColor: Color) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val miscState by viewModel.miscState.collectAsState()
    val devfreqList by viewModel.devfreqList.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("General", "MTK", "Devfreq")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Text("Miscellaneous Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = accentColor,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) },
                indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = accentColor) }
            ) {
                tabs.forEachIndexed { index, title -> Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title, fontWeight = FontWeight.SemiBold) }, selectedContentColor = accentColor, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    item { SectionHeader("System Security") }
                    item { MiscSwitchItem("SELinux Mode", if (miscState.selinuxMode.contains("Enforcing", true)) "Enforcing" else "Permissive", miscState.selinuxMode.contains("Enforcing", true), { viewModel.toggleSelinux(it) }, accentColor) }
                    item { MiscSwitchItem("Double Tap to Wake", "Wake device via gesture", miscState.dt2w, { viewModel.toggleDt2w(it) }, accentColor) }
                    item { Spacer(modifier = Modifier.height(16.dp)); SectionHeader("Touchpanel") }
                    item { MiscSwitchItem("Game Mode", "Optimize touch sampling", miscState.tpGameMode, { viewModel.toggleGeneric(MiscUtils.TP_GAME_MODE, it) }, accentColor) }
                    item { MiscSwitchItem("Touch Limit", "Enable Touch Limit", miscState.tpLimit, { viewModel.toggleGeneric(MiscUtils.TP_LIMIT_ENABLE, it) }, accentColor) }
                    item { Spacer(modifier = Modifier.height(16.dp)); SectionHeader("Kernel Policy") }
                    item { MiscSelectorItem("Thermal Governor", miscState.thermalGov, miscState.availThermalGovs) { viewModel.setThermalGov(it) } }
                    item { MiscSelectorItem("I/O Scheduler", miscState.ioSched, miscState.availIoScheds) { viewModel.setIoSched(it) } }
                }
                if (selectedTab == 1) {
                    item { SectionHeader("Haptics") }
                    item {
                         Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Vibrator Strength", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold); Text("${miscState.mtkVibratorLevel}%", color = accentColor, fontWeight = FontWeight.Bold) }
                            Slider(value = miscState.mtkVibratorLevel.toFloat(), onValueChange = { viewModel.setVibrator(it) }, valueRange = 0f..100f, colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor, inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest))
                        }
                    }
                    item { SectionHeader("Power & Thermal") }
                    item { MiscSwitchItem("Power Budget (PBM)", "MTK Power Budget", miscState.mtkPbm, { viewModel.toggleMtkStop(MiscUtils.MTK_PBM_STOP, it) }, accentColor) }
                    item { MiscSwitchItem("Batoc Current Limit", "Battery OC Protection", miscState.mtkBatoc, { viewModel.toggleMtkStop(MiscUtils.MTK_BATOC_STOP, it) }, accentColor) }
                    item { MiscSwitchItem("Eara Thermal", "Enable Eara monitoring", miscState.mtkEara, { viewModel.toggleGeneric(MiscUtils.MTK_EARA_ENABLE, it) }, accentColor) }
                }
                if (selectedTab == 2) {
                     if (devfreqList.isEmpty()) item { Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No Devfreq devices found.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                     else items(devfreqList) { item -> DevfreqCard(item, accentColor) }
                }
            }
        }
    }
}

// === HELPER WIDGETS ===
@Composable
fun SectionHeader(title: String) { Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)) }

@Composable
fun MiscSwitchItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, accentColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable { onCheckedChange(!checked) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) { Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accentColor, uncheckedThumbColor = MaterialTheme.colorScheme.outline, uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest))
    }
}

@Composable
fun MiscSelectorItem(title: String, currentValue: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).clickable { expanded = true }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column { Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(currentValue, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
        Icon(painter = painterResource(R.drawable.ic_arrow_drop_down), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option, color = MaterialTheme.colorScheme.onSurface) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
fun DevfreqCard(item: SoCViewModel.DevfreqItem, accentColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(R.drawable.ic_speed), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp)); Text(item.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Gov: ${item.gov}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            Text(item.curFreq, style = MaterialTheme.typography.bodySmall, color = accentColor, modifier = Modifier.background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun CPUSummaryCard(viewModel: SoCViewModel, modifier: Modifier = Modifier) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val cpuTemp by viewModel.cpuTemp.collectAsState()
    DashboardCard(backgroundColor = DashboardDarkBg, contentColor = DashboardTextLight, modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) { Text("CPU Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium); Icon(painter = painterResource(R.drawable.ic_cpu), contentDescription = null, tint = DashboardTextLight.copy(alpha = 0.7f)) }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (cpuUsage == "N/A") "N/A" else cpuUsage, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(painter = painterResource(R.drawable.ic_heat), contentDescription = null, modifier = Modifier.size(16.dp), tint = DashboardAccentGreen); Spacer(modifier = Modifier.width(4.dp)); Text(if (cpuTemp == "N/A") "N/A" else "$cpuTemp°C", style = MaterialTheme.typography.bodyMedium, color = DashboardAccentGreen) }
            }
        }
    }
}

@Composable
fun GPUSummaryCard(viewModel: SoCViewModel, hazeState: HazeState, navController: NavController, modifier: Modifier = Modifier) {
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val gpuTemp by viewModel.gpuTemp.collectAsState()
    val gpuState by viewModel.gpuState.collectAsState()
    val context = LocalContext.current
    DashboardCard(backgroundColor = DashboardLightBg, contentColor = DashboardTextDark, modifier = modifier.clickable { 
            when (gpuState.type) {
                SoCUtils.GpuType.ADRENO -> navController.navigate(NavigationRoute.Adreno.route)
                SoCUtils.GpuType.MEDIATEK_V2, SoCUtils.GpuType.MEDIATEK_LEGACY -> navController.navigate(NavigationRoute.Mtk.route)
                SoCUtils.GpuType.GENERIC_DEVFREQ -> navController.navigate(NavigationRoute.GenericGpu.route)
                else -> Toast.makeText(context, "GPU Interface not supported", Toast.LENGTH_SHORT).show()
            }
        }) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) { Text("GPU", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium); StatusChip(if (gpuState.currentFreq == "N/A") "Unknown" else "${gpuState.currentFreq} MHz", DashboardTextDark) }
             Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (gpuUsage == "N/A") "N/A" else gpuUsage, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 36.sp))
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(painter = painterResource(R.drawable.ic_heat), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary); Spacer(modifier = Modifier.width(4.dp)); Text(if (gpuTemp == "N/A") "N/A" else "$gpuTemp°C", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                 val usageFloat = gpuUsage.replace("%", "").toFloatOrNull() ?: 0f
                 val animatedUsage by animateFloatAsState(targetValue = usageFloat / 100f, label = "gpu bar")
                 Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedUsage).background(DashboardAccentGreen))
            }
        }
    }
}

@Composable
fun ThermalVisualCard(viewModel: SoCViewModel, modifier: Modifier) {
    val cpuTemp by viewModel.cpuTemp.collectAsState()
    val gpuTemp by viewModel.gpuTemp.collectAsState()
    val maxTempStr = listOf(cpuTemp, gpuTemp).mapNotNull { it.replace("°C", "").toIntOrNull() }.maxOrNull()?.toString() ?: "N/A"
    val tempValue = maxTempStr.toIntOrNull() ?: 40
    val normalizedTemp = ((tempValue - 30f) / (80f - 30f)).coerceIn(0f, 1f)
    val animatedTempProgress by animateFloatAsState(targetValue = normalizedTemp, animationSpec = tween(1000), label = "tempVisual")

    DashboardCard(backgroundColor = DashboardPurpleBg, contentColor = DashboardTextDark, modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.3f)) {
                val path = Path(); val width = size.width; val height = size.height 
                path.moveTo(0f, height)
                var previousX = 0f; var previousY = height * (1f - (animatedTempProgress * 0.5f))
                for (i in 1..5) {
                    val x = (width / 5) * i; val variance = (1f - animatedTempProgress) * height * 0.2f
                    val y = height * (1f - animatedTempProgress) + (Random.nextFloat() - 0.5f) * variance
                    path.cubicTo(previousX + (x - previousX)/2f, previousY, previousX + (x - previousX)/2f, y, x, y)
                    previousX = x; previousY = y
                }
                path.lineTo(width, height); path.close()
                drawPath(path = path, color = Color.White.copy(alpha = 0.5f)); drawPath(path = path, color = Color.White, style = Stroke(width = 2.dp.toPx()))
            }
            Column(modifier = Modifier.padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text("Max Temp", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium); Icon(painter = painterResource(R.drawable.ic_heat), contentDescription = null, modifier = Modifier.size(20.dp)) }
                Text(if (maxTempStr == "N/A") "N/A" else "$maxTempStr°C", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
            }
        }
    }
}

@Composable
fun PerformanceGaugeCard(viewModel: SoCViewModel, modifier: Modifier) {
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val gpuUsage by viewModel.gpuUsage.collectAsState()
    val cpuVal = cpuUsage.replace("%", "").toFloatOrNull() ?: 0f
    val gpuVal = gpuUsage.replace("%", "").toFloatOrNull() ?: 0f
    val avgUsage = (cpuVal + gpuVal) / 2f
    val animatedUsage by animateFloatAsState(targetValue = avgUsage / 100f, animationSpec = tween(1000, easing = LinearEasing), label = "gauge")

    DashboardCard(backgroundColor = DashboardMintBg, contentColor = DashboardTextDark, modifier = modifier.height(180.dp)) {
        Row(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxHeight().weight(0.45f)) {
                Text("System Load", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${avgUsage.toInt()}%", style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontSize = 42.sp))
                Text("Avg. CPU + GPU", style = MaterialTheme.typography.labelLarge, color = DashboardTextDark.copy(alpha = 0.6f))
            }
            Box(modifier = Modifier.weight(0.55f).fillMaxHeight().aspectRatio(1f).drawBehind {
                val strokeWidth = 24.dp.toPx(); val radius = size.minDimension / 2 - strokeWidth
                val center = Offset(size.width / 2, size.height / 2); val startAngle = 150f; val sweepAngle = 240f
                drawArc(color = Color.Black.copy(alpha = 0.1f), startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawArc(color = DashboardTextDark, startAngle = startAngle, sweepAngle = sweepAngle * animatedUsage, useCenter = false, topLeft = Offset(center.x - radius, center.y - radius), size = Size(radius * 2, radius * 2), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }, contentAlignment = Alignment.Center) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CPU: ${(cpuVal).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DashboardTextDark.copy(alpha = 0.7f))
                    Text("GPU: ${(gpuVal).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = DashboardTextDark.copy(alpha = 0.7f))
                 }
            }
        }
    }
}

@Composable
fun DashboardCard(backgroundColor: Color, contentColor: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(modifier = modifier, shape = DashboardCardShape, colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) { content() }
}

@Composable
fun StatusChip(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape) { Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) }
}
