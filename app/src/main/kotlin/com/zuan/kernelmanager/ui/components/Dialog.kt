/*
 * Copyright (c) 2025 ZKM
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)

package com.zuan.kernelmanager.ui.components

import android.os.Build
import android.view.WindowManager
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.composables.core.Dialog
import com.composables.core.DialogPanel
import com.composables.core.DialogProperties
import com.composables.core.DialogState
import com.composables.core.Scrim
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
    hazeState: HazeState? = null,
    isCustomBg: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // [1] Warna Panel Dialog (Isinya)
    // Menggunakan logika DialogNEW agar tetap transparan (glassy)
    val dialogContainerColor = if (isCustomBg) {
        if (isDark) {
            Color(0xFF101010).copy(alpha = 0.55f) // Hitam Transparan
        } else {
            Color.White.copy(alpha = 0.65f)       // Putih Transparan
        }
    } else {
        AlertDialogDefaults.containerColor
    }

    val mainTextColor = if (isCustomBg) {
        if (isDark) Color.White else Color.Black
    } else {
        AlertDialogDefaults.titleContentColor
    }

    val subTextColor = if (isCustomBg) {
        if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.8f)
    } else {
        AlertDialogDefaults.textContentColor
    }

    // Properties Default
    val properties = DialogProperties()

    Dialog(state = state, properties = properties) {

        // [2] NATIVE WINDOW BLUR (Android 12+)
        // Hanya aktifkan Native Blur jika HazeState TIDAK ada.
        // Jika HazeState ada, kita prioritaskan Haze (Compose Blur).
        val dialogWindowProvider = LocalView.current.parent as? DialogWindowProvider
        SideEffect {
            if (dialogWindowProvider != null) {
                val window = dialogWindowProvider.window

                // Paksa background window transparan agar blur/haze terlihat
                window.setBackgroundDrawableResource(android.R.color.transparent)

                // Logika Native Blur (hanya jika Haze tidak dipakai)
                if (hazeState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

                    if (isCustomBg) {
                        window.attributes.blurBehindRadius = 64
                        window.setDimAmount(0.3f)
                    } else {
                        window.attributes.blurBehindRadius = 0
                        window.setDimAmount(0.6f)
                    }
                } else {
                    // Fallback atau saat Haze aktif (kita matikan native dimming biar Haze yg handle)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    // Set dim amount 0 karena Scrim Compose akan menangani visualnya
                    window.setDimAmount(0f)
                }
            }
        }

        // [3] SCRIM (Layar Gelap/Blur Compose)
        // Logika gabungan antara DialogOLD (Haze) dan DialogNEW (Native)
        val useNativeBlur = (hazeState == null) && isCustomBg && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        
        // Modifier untuk efek Haze (dari DialogOLD)
        val scrimModifier = if (hazeState != null) {
            Modifier
                .fillMaxSize()
                .hazeChild(
                    state = hazeState,
                    style = HazeMaterials.regular() // Style iOS Glass
                )
        } else {
            Modifier.fillMaxSize()
        }

        // Warna Scrim: Transparan jika pakai Haze atau Native Blur
        val scrimColor = if (hazeState != null || useNativeBlur) {
            Color.Transparent
        } else {
            Color.Black.copy(alpha = 0.6f) // Fallback scrim biasa
        }

        Scrim(
            modifier = scrimModifier,
            scrimColor = scrimColor,
            enter = fadeIn(),
            exit = fadeOut()
        )

        val dialogShape = AlertDialogDefaults.shape

        val dialogModifier = Modifier
            .displayCutoutPadding()
            .systemBarsPadding()
            .widthIn(min = 280.dp, max = 560.dp)
            .padding(24.dp)
            .clip(dialogShape)

        DialogPanel(
            modifier = dialogModifier,
            backgroundColor = dialogContainerColor,
            contentColor = mainTextColor,
            shape = dialogShape,
            enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(initialScale = 0.8f),
            exit = scaleOut(targetScale = 0.6f) + fadeOut(tween(durationMillis = 150)),
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                title?.let {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = mainTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                text?.let {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides subTextColor
                    ) {
                        Box(modifier = Modifier.padding(bottom = 24.dp)) { text() }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.let { Box(modifier = Modifier.weight(1f, fill = false)) { dismissButton() } }
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton?.let { Box(modifier = Modifier.weight(1f, fill = false)) { confirmButton() } }
                }
            }
        }
    }
}

@Composable
fun DialogTextButton(
    icon: Any? = null,
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    TextButton(
        onClick = onClick,
        shape = ButtonDefaults.shape,
        colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                when (icon) {
                    is ImageVector -> Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color
                    )
                    is Painter -> Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = color
                    )
                }
            }
            Text(text = text, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}
