/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.zuan.kernelmanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zuan.kernelmanager.ui.navigation.ZuanKernelManagerNavHost
import com.zuan.kernelmanager.ui.theme.ZuanKernelManagerTheme
import com.topjohnwu.superuser.Shell
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    private var isRoot = false
    private var showRootDialog by mutableStateOf(false)

    // Launcher untuk request izin notifikasi (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> 
        // Hasil request (bisa kosongkan jika tidak ada logic khusus)
    }

    private val checkRoot = Runnable {
        Shell.getShell { shell ->
            isRoot = shell.isRoot
            if (!isRoot) {
                showRootDialog = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !isRoot }
        enableEdgeToEdge()
        Thread(checkRoot).start()

        // Minta Izin Notifikasi saat App Dibuka
        askNotificationPermission()

        setContent {
            ZuanKernelManagerTheme {
                ZuanKernelManagerApp(showRootDialog = showRootDialog)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    companion object {
        init {
            @Suppress("DEPRECATION")
            if (Shell.getCachedShell() == null) {
                Shell.setDefaultBuilder(
                    Shell.Builder.create()
                        .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
                        .setTimeout(20),
                )
            }
        }
    }
}

@Composable
fun ZuanKernelManagerApp(showRootDialog: Boolean = false) {
    if (showRootDialog) {
        AlertDialog(
            onDismissRequest = {},
            text = {
                Text(
                    text = "Zuan Kernel Manager requires root access!",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        exitProcess(0)
                    },
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Text("Exit")
                }
            },
        )
    }
    Surface {
        ZuanKernelManagerNavHost()
    }
}
