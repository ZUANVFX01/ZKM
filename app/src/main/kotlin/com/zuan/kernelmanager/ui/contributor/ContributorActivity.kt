/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
package com.zuan.kernelmanager.ui.contributor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zuan.kernelmanager.ui.theme.ZuanKernelManagerTheme

class ContributorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZuanKernelManagerTheme {
                ContributorScreen()
            }
        }
    }
}
