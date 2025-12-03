/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize // Tambahan
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState // Tambahan
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll // Tambahan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.core.rememberDialogState
import com.zuan.kernelmanager.ui.components.CustomListItem
import com.zuan.kernelmanager.ui.components.DialogTextButton
import com.zuan.kernelmanager.ui.components.DialogUnstyled
import com.zuan.kernelmanager.ui.components.TopAppBarWithBackButton
import com.zuan.kernelmanager.ui.theme.ThemeMode
// Import Haze
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(), lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // 1. Setup Haze State
    val hazeState = rememberHazeState()

    val themeMode by viewModel.themeMode.collectAsState()
    val pollingInterval by viewModel.pollingInterval.collectAsState()
    var value by remember { mutableStateOf((pollingInterval / 1000).toString()) }
    val intervalSeconds = value.toLongOrNull()
    val appVersion by viewModel.appVersion.collectAsState()

    val openThemeDialog = rememberDialogState(initiallyVisible = false)
    val openPollingDialog = rememberDialogState(initiallyVisible = false)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.loadSettingsData(context)
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBarWithBackButton(
                text = "Settings",
                onBack = { (context as? Activity)?.finish() },
                scrollBehavior = scrollBehavior,
                hazeState = hazeState // 2. Pasang hazeState di AppBar
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState) // 3. Pasang modifier haze di content
                .verticalScroll(rememberScrollState()) // Tambahkan scroll biar aman
        ) {
            // 4. Gunakan Spacer untuk padding atas (Status Bar + AppBar)
            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))

            CustomListItem(
                icon = Icons.Default.Palette,
                title = "App theme",
                summary = "Choose between light, dark, or system default theme",
                onClick = { openThemeDialog.visible = true },
            )
            CustomListItem(
                icon = Icons.Default.Timer,
                title = "SoC polling interval",
                summary = "Set how often SoC data is updated (in seconds)",
                onClick = { openPollingDialog.visible = true },
            )
            CustomListItem(
                icon = Icons.Default.Info,
                title = "Zuan Kernel Manager version",
                summary = appVersion,
                onLongClick = { clipboardManager.setText(AnnotatedString(appVersion)) },
            )

            // 5. Gunakan Spacer untuk padding bawah (Nav Bar)
            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 16.dp))
        }
    }

    DialogUnstyled(
        state = openThemeDialog,
        hazeState = hazeState, // 6. Pass hazeState ke Dialog
        title = "Select theme",
        text = {
            Column {
                DialogTextButton(
                    icon = Icons.Default.LightMode,
                    text = "Light mode",
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.LIGHT)
                        openThemeDialog.visible = false
                    },
                )
                DialogTextButton(
                    icon = Icons.Default.DarkMode,
                    text = "Dark mode",
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.DARK)
                        openThemeDialog.visible = false
                    },
                )
                DialogTextButton(
                    icon = Icons.Default.Android,
                    text = "System default",
                    onClick = {
                        viewModel.setThemeMode(ThemeMode.SYSTEM_DEFAULT)
                        openThemeDialog.visible = false
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { openThemeDialog.visible = false },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("Close")
            }
        },
    )

    DialogUnstyled(
        state = openPollingDialog,
        hazeState = hazeState, // 6. Pass hazeState ke Dialog
        title = "SoC polling interval",
        text = {
            Column {
                Text(
                    text = "Set the polling interval for SoC data (1-30 seconds)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Interval (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (intervalSeconds != null && intervalSeconds in 1..30) {
                                viewModel.setPollingInterval(intervalSeconds * 1000)
                                openPollingDialog.visible = false
                            }
                        },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (intervalSeconds != null && intervalSeconds in 1..30) {
                        viewModel.setPollingInterval(intervalSeconds * 1000)
                        openPollingDialog.visible = false
                    }
                },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { openPollingDialog.visible = false },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text("Cancel")
            }
        },
    )
}
