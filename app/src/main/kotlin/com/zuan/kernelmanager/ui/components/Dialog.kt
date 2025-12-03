/*
 * Copyright (c) 2025 Rve <rve27github@gmail.com>
 * All Rights Reserved.
 */
 /*
 * Copyright (c) 2025 ZKM <zuanvfx01github@gmail.com>
 * All Rights Reserved.
 */
@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.DialogState
import com.composables.core.Scrim
// Import Haze
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun DialogUnstyled(
    state: DialogState,
    title: String? = null,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
    hazeState: HazeState? = null
) {
    Dialog(state = state) {
        // --- 1. LOGIKA SCRIM (Background Belakang) ---
        // Warna scrim kita buat transparan total jika haze aktif, biar blurnya clean
        val scrimColor = if (hazeState != null) Color.Black.copy(alpha = 0.2f) else Color.Black.copy(0.6f)
        
        val scrimModifier = if (hazeState != null) {
            Modifier
                .fillMaxSize()
                // NYALAKAN LAGI BLUR BACKGROUND DI SINI
                .hazeChild(
                    state = hazeState, 
                    style = HazeMaterials.regular() // Pakai regular biar background agak gelap/blur
                )
        } else {
            Modifier.fillMaxSize()
        }

        Scrim(
            modifier = scrimModifier,
            scrimColor = scrimColor,
            enter = fadeIn(),
            exit = fadeOut()
        )

        // --- 2. LOGIKA MODIFIER DIALOG (Kotak Kaca) ---
        val dialogShape = AlertDialogDefaults.shape
        
        val baseModifier = Modifier
            .displayCutoutPadding()
            .systemBarsPadding()
            .widthIn(min = 280.dp, max = 560.dp)
            .padding(24.dp)

        val dialogModifier = if (hazeState != null) {
            baseModifier
                .clip(dialogShape)
                .hazeChild(
                    state = hazeState, 
                    style = HazeMaterials.thin() // Dialog pakai thin biar beda layer sama background
                )
        } else {
            baseModifier
        }

        // --- 3. DIALOG PANEL ---
        DialogPanel(
            modifier = dialogModifier,
            backgroundColor = if (hazeState != null) Color.White.copy(alpha = 0.45f) else Color.White,
            contentColor = Color.Black,
            shape = dialogShape,
            enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ),
            exit = scaleOut(targetScale = 0.6f) + fadeOut(tween(durationMillis = 150)),
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                ) {
                    title?.let {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.Start),
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black.copy(alpha = 0.9f),
                            )
                        }
                    }
                    text?.let {
                        Box(
                            modifier = Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = 24.dp)
                                .align(Alignment.Start),
                        ) {
                            text()
                        }
                    }
                    Row(Modifier.align(Alignment.End)) {
                        dismissButton?.let {
                            dismissButton()
                        }
                        confirmButton?.let {
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTextButton(icon: Any? = null, text: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shapes = ButtonDefaults.shapes(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                when (icon) {
                    is ImageVector -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    is Painter -> Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }
            Text(text)
        }
    }
}
