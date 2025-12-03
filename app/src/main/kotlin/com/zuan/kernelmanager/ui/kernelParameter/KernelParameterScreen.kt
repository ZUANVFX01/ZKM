/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)

package com.zuan.kernelmanager.ui.kernelParameter

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.components.PinnedTopAppBar
import com.zuan.kernelmanager.ui.navigation.BottomNavigationBar
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun KernelParameterScreen(viewModel: KernelParameterViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hazeState = rememberHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    val isDark = isSystemInDarkTheme()
    val colors = if (isDark) DarkPalette else LightPalette

    // --- DATA STATE ---
    val mem by viewModel.mem.collectAsState()
    val net by viewModel.net.collectAsState()
    val profile by viewModel.kernelProfile.collectAsState()
    val displayState by viewModel.displayState.collectAsState()
    
    val topPagerState = rememberPagerState(pageCount = { 4 })
    val bottomPagerState = rememberPagerState(pageCount = { 4 })

    // LOGIC SINKRONISASI OFFSET
    LaunchedEffect(bottomPagerState) {
        snapshotFlow { Pair(bottomPagerState.currentPage, bottomPagerState.currentPageOffsetFraction) }
            .collectLatest { (page, offset) ->
                topPagerState.scrollToPage(page, offset)
            }
    }

    // --- SETUP DISPLAY LISTENER (REAL-TIME FPS) ---
    DisposableEffect(context) {
        viewModel.loadDisplayInfo(context) // Load info statis saat masuk

        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        val listener = object : android.hardware.display.DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayRemoved(displayId: Int) {}
            override fun onDisplayChanged(displayId: Int) {
                // Saat refresh rate berubah, update viewModel
                val display = displayManager.getDisplay(displayId)
                display?.let { viewModel.updateRealtimeRefreshRate(it.refreshRate) }
            }
        }
        displayManager.registerDisplayListener(listener, null)
        onDispose {
            displayManager.unregisterDisplayListener(listener)
        }
    }

    // Dialogs
    val openZramAction = rememberDialogState(initiallyVisible = false)
    val openZD = rememberDialogState(initiallyVisible = false)
    val openZCD = rememberDialogState(initiallyVisible = false)

    val pullToRefreshState = remember {
        object : PullToRefreshState {
            private val anim = Animatable(0f, Float.VectorConverter)
            override val distanceFraction get() = anim.value
            override val isAnimating: Boolean get() = anim.isRunning
            override suspend fun animateToThreshold() { anim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioHighBouncy)) }
            override suspend fun animateToHidden() { anim.animateTo(0f) }
            override suspend fun snapTo(targetValue: Float) { anim.snapTo(targetValue) }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = { PinnedTopAppBar(scrollBehavior = scrollBehavior, hazeState = hazeState) },
        bottomBar = { BottomNavigationBar(navController, hazeState = hazeState) },
    ) { innerPadding ->
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .haze(state = hazeState)
        ) {
            
            Spacer(modifier = Modifier.height(16.dp))

            // 1. TOP CAROUSEL (Hero Section)
            HorizontalPager(
                state = topPagerState,
                contentPadding = PaddingValues(horizontal = 0.dp),
                modifier = Modifier.wrapContentHeight().fillMaxWidth()
            ) { page ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    when (page) {
                        0 -> { 
                            val profileName = when(profile.currentProfile) { 0 -> "Power"; 2 -> "Perf"; else -> "Balance" }
                            DynamicHeroCard(title="Active Profile", mainValue=profileName, subValue="Kernel Mode", icon=Icons.Rounded.Speed, accentColor=Color(0xFF3B82F6), gradientColors=listOf(Color(0xFF3B82F6), Color(0xFF2563EB)), chartValue="100%", progress=1.0f, colors=colors, onClick={})
                        }
                        1 -> { 
                            // Calculate Progress Float from Percentage
                            val usageFloat = mem.zramUsagePercent / 100f
                            DynamicHeroCard(
                                title="Memory Status", 
                                mainValue=mem.zramSize, 
                                subValue=mem.zramAlgo, 
                                icon=Icons.Rounded.SdStorage, 
                                accentColor=Color(0xFFFACC15), 
                                gradientColors=listOf(Color(0xFFFACC15), Color(0xFFEAB308)), 
                                chartValue="${mem.zramUsagePercent}%",
                                progress=usageFloat,
                                colors=colors, 
                                onClick={ openZramAction.visible = true }
                            )
                        }
                        2 -> { 
                            DynamicHeroCard(title="Network State", mainValue="Online", subValue=net.tcpCongestion, icon=Icons.Rounded.Wifi, accentColor=Color(0xFF10B981), gradientColors=listOf(Color(0xFF10B981), Color(0xFF059669)), chartValue="TCP", progress=1.0f, colors=colors, onClick={})
                        }
                        3 -> { 
                            // Real-time Display FPS
                            val fps = displayState.refreshRate.toInt()
                            val progress = (fps / 144f).coerceIn(0f, 1f) 
                            
                            DynamicHeroCard(
                                title="Display Info", 
                                mainValue="$fps Hz", 
                                subValue="Real-time Refresh", 
                                icon=Icons.Rounded.BrightnessHigh, 
                                accentColor=Color(0xFFEC4899), 
                                gradientColors=listOf(Color(0xFFEC4899), Color(0xFFDB2777)), 
                                chartValue="FPS", 
                                progress=progress, 
                                colors=colors, 
                                onClick={}
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. STICKY TABS (Controller)
            BentoSegmentedControl(
                pagerState = bottomPagerState, 
                colors = colors,
                onTabSelected = { index ->
                    scope.launch { bottomPagerState.animateScrollToPage(index) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. BOTTOM CONTENT (Pages)
            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = viewModel.isRefreshing,
                onRefresh = { viewModel.refresh() },
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(state = pullToRefreshState, isRefreshing = viewModel.isRefreshing, modifier = Modifier.align(Alignment.TopCenter), containerColor = colors.cardBg, color = colors.tabAccent)
                },
            ) {
                HorizontalPager(
                    state = bottomPagerState,
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> SchedulerPage(viewModel, colors)
                        1 -> MemoryPage(viewModel, colors)
                        2 -> NetworkPage(viewModel, colors)
                        3 -> DisplayPage(viewModel, colors)
                    }
                }
            }
        }
    }

    // --- DIALOGS ---
    DialogUnstyled(
        state = openZramAction, hazeState = hazeState, title = "ZRAM Settings",
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openZramAction.visible = false; openZD.visible = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colors.tabAccent)) { Text("Change Size", color = if(isSystemInDarkTheme()) Color.Black else Color.White) }
                Button(onClick = { openZramAction.visible = false; openZCD.visible = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = colors.cardActive)) { Text("Change Algorithm", color = colors.textMain) }
            }
        },
        dismissButton = { TextButton(onClick = { openZramAction.visible = false }) { Text("Cancel", color = colors.textSub) } }
    )
    
    DialogUnstyled(state = openZD, hazeState = hazeState, title = "ZRAM Size", text = {
        Column { listOf("1 GB", "2 GB", "3 GB", "4 GB").forEach { size -> DialogTextButton(text = size, onClick = { viewModel.updateZramSize(size.substringBefore(" GB").toInt()); openZD.visible = false }) } }
    }, dismissButton = { TextButton(onClick = { openZD.visible = false }) { Text("Cancel") } })
    
    DialogUnstyled(state = openZCD, hazeState = hazeState, title = "Comp Algo", text = {
        Column { mem.availableZramAlgo.forEach { algo -> DialogTextButton(text = algo, onClick = { viewModel.updateZramCompAlgorithm(algo); openZCD.visible = false }) } }
    }, dismissButton = { TextButton(onClick = { openZCD.visible = false }) { Text("Cancel") } })
}
