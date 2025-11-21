/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.components

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults // Tambahan penting
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier // Tambahan
import androidx.compose.ui.graphics.Color // Tambahan
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import com.zuan.kernelmanager.R
import com.zuan.kernelmanager.ui.contributor.ContributorActivity
import com.zuan.kernelmanager.ui.settings.SettingsActivity
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun PinnedTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    hazeState: HazeState? = null // Tambahkan parameter ini (nullable agar aman)
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    TopAppBar(
        title = { Text("Zuan Kernel Manager", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        // Terapkan HazeChild di sini
        modifier = Modifier.let { modifier ->
            if (hazeState != null) {
                modifier.hazeChild(
                    state = hazeState,
                    style = HazeMaterials.regular() // Menggunakan style material (adaptif light/dark)
                )
            } else modifier
        },
        // KUNCI: Buat background transparan agar blur di belakangnya terlihat
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        actions = {
            TooltipBox(
                positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Left,
                ),
                tooltip = { PlainTooltip(caretShape = TooltipDefaults.caretShape()) { Text("Menu") } },
                state = rememberTooltipState(),
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More",
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = MaterialTheme.shapes.large,
            ) {
                DropdownMenuItem(
                    text = {
                        Text("Source code")
                    },
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://github.com/Rve27/RvKernel-Manager".toUri()))
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text("Telegram group")
                    },
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://t.me/rve_enterprises".toUri()))
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_telegram),
                            contentDescription = null,
                        )
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text("Contributors")
                    },
                    onClick = {
                        context.startActivity(Intent(context, ContributorActivity::class.java))
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = "Contributors",
                        )
                    },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text("Settings")
                    },
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = null,
                        )
                    },
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun TopAppBarWithBackButton(
    text: String,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    hazeState: HazeState? = null // Tambahkan parameter ini
) {
    LargeFlexibleTopAppBar(
        title = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        // Terapkan HazeChild
        modifier = Modifier.let { modifier ->
            if (hazeState != null) {
                modifier.hazeChild(
                    state = hazeState,
                    style = HazeMaterials.regular()
                )
            } else modifier
        },
        // KUNCI: Background transparan
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        navigationIcon = {
            TooltipBox(
                positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Below,
                ),
                tooltip = { PlainTooltip(caretShape = TooltipDefaults.caretShape()) { Text("Back") } },
                state = rememberTooltipState(),
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}
