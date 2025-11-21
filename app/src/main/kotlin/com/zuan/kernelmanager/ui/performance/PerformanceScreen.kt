/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package com.zuan.kernelmanager.ui.performance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PerformanceScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CPU Governor Card
            PerformanceCard(title = "CPU Governor", icon = Icons.Default.Speed) {
                Text("Little Cluster: schedutil", style = MaterialTheme.typography.bodyMedium)
                Slider(value = 0.8f, onValueChange = {})
                Spacer(modifier = Modifier.height(8.dp))
                Text("Big Cluster: schedutil", style = MaterialTheme.typography.bodyMedium)
                Slider(value = 0.6f, onValueChange = {})
            }

            // GPU Frequency Card
            PerformanceCard(title = "GPU Frequency", icon = Icons.Default.Speed) {
                Text("Max Freq: 850 MHz", style = MaterialTheme.typography.bodyMedium)
                Slider(value = 0.9f, onValueChange = {})
                Spacer(modifier = Modifier.height(8.dp))
                Text("Min Freq: 300 MHz", style = MaterialTheme.typography.bodyMedium)
                Slider(value = 0.2f, onValueChange = {})
            }

            // I/O Scheduler Card
            PerformanceCard(title = "I/O Scheduler", icon = Icons.Default.Speed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scheduler", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { }) {
                        Text("cfq")
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}